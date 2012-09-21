package com.swiftdino.medicalviewer;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DisplayPanel extends SurfaceView implements SurfaceHolder.Callback {
	
	//debug values
	private int _debug = 2;
	private final int DEBUG_MULTI_TOUCH = 0;
	private final int DEBUG_PLOT_POINTS = 1;
	
	//Actual data is used
	private final int PLOT_DATA = 2;
	
	//DataSet Colors
	private final int[] _colors = {Color.BLUE, Color.YELLOW, Color.GREEN};
	
	//thread for canvas
	private CanvasThread _thread;
	
	//test lists for drawn objects
	private ArrayList<GraphicObject> _graphics = new ArrayList<GraphicObject>();
	private ArrayList<Point> _plotPoints = new ArrayList<Point>(10);
	
	//Data input from queries
	private DataSet[] _data = new DataSet[5];
	private int _activeSets = 0;
	
	//input data points
	Iterable<Point> _points = null;
	
	//offset for frame panning
	private Point _frameOffset = new Point();
	private boolean pannable = true;
	private int panTimer = 0;
	private float _zoomValue = 1;
	
	//double tap time holder and threshold
	private long _lastRelease = 0;
	private final long DOUBLE_TAP_THRESHOLD = 500;
	
	//graph point size
	private int _pointSize = 4;
	
	//flag for displaying circles on points
	private boolean showPoints = false;
	
	//paint object for primitives drawn on canvas
	Paint paint = new Paint();
	
	//temporary points for drag tracking
	private int lastX = 0;
	private int lastY = 0;
	
	// historical pointer difference for pinch zoom
	private int pointDiff; 
	
	// distance from edge of panel to graph axis
	private int _uiBuffer = 60;
	
	public DisplayPanel(Context context) {
        
		super(context);
        
		getHolder().addCallback(this);
        _thread = new CanvasThread(getHolder(), this);
        setFocusable(true);
        
        //use dummy points
        addTestPoints();
        
    }
	
	public DisplayPanel(Context context, AttributeSet attrs){
		
		super(context, attrs);
		
		getHolder().addCallback(this);
        _thread = new CanvasThread(getHolder(), this);
        setFocusable(true);
        
        //use dummy points
        addTestPoints();
		
	}
	
	public DisplayPanel(Context context, AttributeSet attrs, int defStyle){
		
		super(context, attrs, defStyle);
		
		getHolder().addCallback(this);
        _thread = new CanvasThread(getHolder(), this);
        setFocusable(true);
        
        //use dummy points
        addTestPoints();
        
	}
	
	public void setZoom(float zoom){
		_zoomValue = zoom;
	}
	
	public void setTestData(Iterable<Point> data){
		_points = data;
	}

	public void setData(Iterable<DataSet> data){
		int i = 0;
		for(DataSet ds : data){
			_data[i++] = ds;
		}
		_activeSets = i;
	}
	
	public void setData(DataSet[] data){
		int i = 0;
		for(DataSet ds : data){
			_data[i++] = ds;
		}
		_activeSets = i;
	}
	
	public int distance(float x1, float y1, float x2, float y2){
		return (int)Math.sqrt(Math.pow(x2- x1, 2) + Math.pow(y2-y1, 2));
	}
	
	public float decAcc(float num, int acc){
		return ((int)(num * (float)Math.pow(10, acc))) / ((float)Math.pow(10, acc));
	}
	
	private void resetView(){
		_frameOffset.set(0,0);
		_zoomValue = 1;
	}
	
	//when a touch event occurs
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		synchronized (_thread.getSurfaceHolder()) {
	        
			switch(_debug){
				
				case DEBUG_MULTI_TOUCH: 
				
					if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
						GraphicObject graphic = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
						graphic.setX((int) event.getX() - graphic.getGraphic().getWidth() / 2);
						graphic.setY((int) event.getY() - graphic.getGraphic().getHeight() / 2);
						_graphics.add(graphic);
					}
					
					if(event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
						GraphicObject graphic = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
						graphic.setX((int) event.getX(1) - graphic.getGraphic().getWidth() / 2);
						graphic.setY((int) event.getY(1) - graphic.getGraphic().getHeight() / 2);
						_graphics.add(graphic);
					}
					
					break;
					
				case DEBUG_PLOT_POINTS:
					
					// let the graph pan again
					if(panTimer == 0){
						pannable = true;
					}
					
					// panning disabled for 1 less frame
					if(!pannable){
						panTimer -= 1;
					}
					
					// set panning false for 1 frame after pinch to let the pointer locations update
					if(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
						pannable = false;
						panTimer = 1;
					}
					
					// get the difference between the pointer locations
					if(event.getPointerCount() == 2 && event.getAction() != MotionEvent.ACTION_MOVE){
						pointDiff = distance(event.getX(), event.getY(), event.getX(1), event.getY(1));
						//Log.d("","difference = " + pointDiff);
					}
					
					// when a pointer has moved (any pointer)
					if(event.getAction() == MotionEvent.ACTION_MOVE) {
						
						// if one pointer and panning is enabled then pan
						if(event.getPointerCount() < 2 && pannable){
							_frameOffset.offset((int)event.getX() - lastX, (int)event.getY() - lastY);
						}
						
						// if two pointers then change zoom proportional to pointer difference  / pointer difference form the previous frame
						else if(event.getPointerCount() == 2){
							int newPD = distance(event.getX(), event.getY(), event.getX(1), event.getY(1));
							_zoomValue *= (float)newPD/(float)pointDiff;
							pointDiff = newPD;
						}
					}
					
					// keep track of the last frames pointer locations for panning
					if(event.getPointerCount() < 2){
						lastX = (int) event.getX();
						lastY = (int) event.getY();
					}
					
					break;
					
				case PLOT_DATA:
					
					// let the graph pan again
					if(panTimer == 0){
						pannable = true;
					}
					
					// panning disabled for 1 less frame
					if(!pannable){
						panTimer -= 1;
					}
					
					// set panning false for 1 frame after pinch to let the pointer locations update
					if(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
						pannable = false;
						panTimer = 1;
					}
					
					if(event.getActionMasked() == MotionEvent.ACTION_UP){
						long current = System.currentTimeMillis();
						if(_lastRelease == 0) _lastRelease = current;
						else if(current - _lastRelease < DOUBLE_TAP_THRESHOLD){
							resetView();
							_lastRelease = 0;
						}
						else _lastRelease = 0;
					}
					
					if(event.getAction() == MotionEvent.ACTION_DOWN && event.getPointerCount() > 1){
						_lastRelease = 0;
					}
					
					if(event.getPointerCount() == 3){
						resetView();
					}
					
					// get the difference between the pointer locations
					if(event.getPointerCount() == 2 && event.getAction() != MotionEvent.ACTION_MOVE){
						pointDiff = distance(event.getX(), event.getY(), event.getX(1), event.getY(1));
						//Log.d("","difference = " + pointDiff);
					}
					
					// when a pointer has moved (any pointer)
					if(event.getAction() == MotionEvent.ACTION_MOVE) {
						
						// if one pointer and panning is enabled then pan
						if(event.getPointerCount() < 2 && pannable){
							_frameOffset.offset((int)event.getX() - lastX, (int)event.getY() - lastY);
						}
						
						// if two pointers then change zoom proportional to pointer difference  / pointer difference form the previous frame
						else if(event.getPointerCount() == 2){
							int newPD = distance(event.getX(), event.getY(), event.getX(1), event.getY(1));
							_zoomValue *= (float)newPD/(float)pointDiff;
							pointDiff = newPD;
						}
					}
					
					// keep track of the last frames pointer locations for panning
					if(event.getPointerCount() < 2){
						lastX = (int) event.getX();
						lastY = (int) event.getY();
					}
					
					break;
			
			}
			
		}
		
		return true;
		
	}
	
	//draw/redraw
	@Override
	public void onDraw(Canvas canvas){
		
		if(_thread._run){
		
			//background
			canvas.drawColor(Color.BLACK);
			
			switch(_debug){
			
				case DEBUG_MULTI_TOUCH:
					
			        Bitmap bitmap;
			        
			        for (GraphicObject graphic : _graphics) {
			            bitmap = graphic.getGraphic();
			            canvas.drawBitmap(bitmap, graphic.getX(), graphic.getY(), null);
			            //canvas.drawCircle(coords.getX(), coords.getY(),bitmap.getWidth()/2, paint);
			        }
			        
			        break;
			     
				case DEBUG_PLOT_POINTS:
					
					paint.setColor(Color.WHITE);
					paint.setAlpha(160);
					paint.setStrokeWidth(2);
					
					//Log.d("" + getHeight(),"" + ((getHeight() - (_uiBuffer * (3 + 1))) / 3));
					
					Point last = null;
					
					// distances between axis marks
					int wInterval = (getWidth() - _uiBuffer * 2)/10;
					int hInterval = (getHeight() - _uiBuffer * 2)/10;
					
					//draw axis
					paint.setAlpha(255);
					canvas.drawLine(_uiBuffer, getHeight() - _uiBuffer, getWidth() - _uiBuffer, getHeight() - _uiBuffer, paint);
					canvas.drawLine(_uiBuffer, _uiBuffer, _uiBuffer, getHeight() - _uiBuffer, paint);
					for(int i = 0; i < 11; i++){
						canvas.drawLine(_uiBuffer + wInterval*i, getHeight() - _uiBuffer - 10, _uiBuffer + wInterval * i, getHeight() - _uiBuffer + 10, paint);
						String label = "" + (_uiBuffer + (wInterval * i) - _frameOffset.x)/_zoomValue;
						canvas.drawText(label,_uiBuffer + (wInterval * i) - 3 * label.length(), getHeight() - _uiBuffer/2, paint);
						canvas.drawLine(_uiBuffer - 10, getHeight() - _uiBuffer - hInterval * i, _uiBuffer + 10, getHeight() - _uiBuffer - hInterval * i, paint);
						label = "" + -(getHeight() - _uiBuffer - (hInterval * i) - _frameOffset.y)/_zoomValue;
						canvas.drawText(label,_uiBuffer/2 - (3 * label.length()), getHeight() - _uiBuffer - (hInterval * i) + 3, paint);
					}
					
					Iterable<Point> pointData;
					if(_points == null){
						pointData = _plotPoints;
					}
					else{
						pointData =  _points;
					}
					
					//plot the stored point and lines between them
					paint.setAlpha(255);
					paint.setColor(Color.BLUE);
					paint.setStrokeWidth(3);
					for (Point p : pointData){
						
						if(p != null){
							
							if(showPoints){
								canvas.drawCircle((p.x * _zoomValue) - _pointSize/2 + _frameOffset.x, (-p.y * _zoomValue) - _pointSize/2 + _frameOffset.y, _pointSize, paint);
							}
							
							if(last != null) {
								canvas.drawLine((last.x * _zoomValue) + _frameOffset.x, (-last.y * _zoomValue) + _frameOffset.y, (p.x * _zoomValue) + _frameOffset.x, (-p.y * _zoomValue) + _frameOffset.y, paint);
							}
							
							last = p;
						
						}
						
					}
					
					break;
		        
				case PLOT_DATA:
					
					if(_activeSets > 0){
					
						int _buffer = _uiBuffer;
						float _graphSize = (getHeight() - (_buffer * (_activeSets + 1f))) / _activeSets;
						
						// distances between axis marks
						float widthInterval = (getWidth() - _uiBuffer * 4)/10f;
						float heightInterval = _graphSize/2f;
						
						Point lastP = null;
						
						for(int i = 0; i < _activeSets; i++){
							//plot the stored point and lines between them
							paint.setAlpha(255);
							paint.setColor(_colors[i%_colors.length]);
							paint.setStrokeWidth(3);
							for (Point p : _data[i].getData()){
								
								if(p != null){
									
									float locX = (p.x * _zoomValue) + _uiBuffer + _frameOffset.x;
									float locY = (p.y-_data[i].minVal)*(_graphSize/_data[i].range) + _uiBuffer*(i+1) + _graphSize*i;
									
									if(showPoints){
										//canvas.drawCircle((p.x * _zoomValue) - _pointSize/2 + _frameOffset.x, (-p.y * _zoomValue) - _pointSize/2 + _frameOffset.y, _pointSize, paint);
										canvas.drawCircle(locX-_pointSize/2,locY - _pointSize/2, _pointSize, paint);
									}
									
									if(lastP != null) {
										//canvas.drawLine((lastP.x * _zoomValue) + _frameOffset.x, (-lastP.y * _zoomValue) + _frameOffset.y, (p.x * _zoomValue) + _frameOffset.x, (-p.y * _zoomValue) + _frameOffset.y, paint);
										canvas.drawLine(lastP.x, lastP.y, locX, locY, paint);
									}
									
									lastP = new Point((int)locX, (int)locY);
								
								}
							}
							
							lastP = null;
							
						}
						
						paint.setColor(Color.BLACK);
						canvas.drawRect(new Rect(0,0,_uiBuffer,getHeight()), paint);
						canvas.drawRect(new Rect(getWidth()-(_uiBuffer*3),0,getWidth(),getHeight()), paint);
						
						//draw axis
						paint.setAlpha(255);
						paint.setColor(Color.WHITE);
						paint.setStrokeWidth(2);
						
						for(int j = 1; j <= _activeSets; j++){
							
							String label;
							canvas.drawLine(_uiBuffer, (_uiBuffer+_graphSize)*j, getWidth() - _uiBuffer*3, (_uiBuffer+_graphSize)*j, paint);
							canvas.drawLine(_uiBuffer, _uiBuffer*j + _graphSize*(j-1), _uiBuffer, (_uiBuffer + _graphSize)*j, paint);
							
							for(int i = 0; i < 11; i++){
								
								canvas.drawLine(_uiBuffer + widthInterval*i, (_uiBuffer+_graphSize)*j - 10, _uiBuffer + widthInterval * i, (_uiBuffer+_graphSize)*j + 10, paint);
								label = "" + decAcc(((widthInterval * i) - _frameOffset.x)/_zoomValue,2);
								canvas.drawText(label,_uiBuffer + (widthInterval * i) - 3 * label.length(), getHeight() - _uiBuffer/2 - (_uiBuffer + _graphSize)*(j-1), paint);
								if(i < 3){
									canvas.drawLine(_uiBuffer - 10, getHeight() - _uiBuffer*j - _graphSize*(j-1) - heightInterval * i, _uiBuffer + 10, getHeight() - _uiBuffer*j - _graphSize*(j-1) - heightInterval * i, paint);
									//label = "" + decAcc(-(getHeight() - _uiBuffer - (heightInterval * i) - _frameOffset.y)/_zoomValue,2);
									label = "" + decAcc(_data[j-1].range * (i+1)/3 + _data[j-1].minVal, 2);
									Log.d("","" + _data[j-1].minVal);
									canvas.drawText(label,_uiBuffer/2 - (3 * label.length()), getHeight() - _uiBuffer*j - _graphSize*(j-1) - (heightInterval * i) + 3, paint);
								}
								
							}
							
						}
					
					}
					
					break;
			
			}
		
		}
		//Log.d("","" + _frameOffset.y);
		
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try{
			_thread.setRunning(true);
	    	_thread.start();
	    	_frameOffset.set(0 + _uiBuffer,getHeight() - _uiBuffer);
		} catch(IllegalThreadStateException e){
			_thread = new CanvasThread(getHolder(), this);
			_thread.setRunning(true);
			_thread.start();
		}
    	
		_frameOffset.set(0,0);

	    Log.d("","Surface Created");
	    
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		
		boolean retry = true;
	    _thread.setRunning(false);
	    while (retry) {
	        try {
	            _thread.join();
	            retry = false;
	        } catch (InterruptedException e) {
	        	// we will try it again and again...
	        }
	    }
		
	}
	
	//thread for canvas
	class CanvasThread extends Thread {
		
		private SurfaceHolder _surfaceHolder;
        private DisplayPanel _panel;
        private boolean _run = false;
        
        public CanvasThread(SurfaceHolder surfaceHolder, DisplayPanel panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }
		
        public void setRunning(boolean run) {
            _run = run;
        }
     
        public SurfaceHolder getSurfaceHolder() {
            return _surfaceHolder;
        }
        
        @Override
        public void run() {
            
        	Canvas c;
            
        	while (_run) {
                
        		c = null;
                
        		try {
                    c = _surfaceHolder.lockCanvas(null);
                    
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                    
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                    
                }
        		
            }
        	
        }
        
	}
	
	//dummy points for testing
	private void addTestPoints(){
		
		ArrayList<Point> list = _plotPoints;
		ArrayList<Point> list2 = new ArrayList<Point>();
		ArrayList<Point> list3 = new ArrayList<Point>();
		
		list.add(new Point(0, 215));
		list.add(new Point(100, 50));
		list.add(new Point(200, 215));
		list.add(new Point(300, 135));
		list.add(new Point(400, 450));
		list.add(new Point(500, 315));
		list.add(new Point(600, 585));
		list.add(new Point(700, 250));
		list.add(new Point(800, 375));
		list.add(new Point(900, 85));
		
		list2.add(new Point(0, 85));
		list2.add(new Point(100, 375));
		list2.add(new Point(200, 250));
		list2.add(new Point(300, 585));
		list2.add(new Point(400, 315));
		list2.add(new Point(500, 450));
		list2.add(new Point(600, 135));
		list2.add(new Point(700, 215));
		list2.add(new Point(800, 50));
		list2.add(new Point(900, 215));
		
		list3.add(new Point(0, 215));
		list3.add(new Point(100, 50));
		list3.add(new Point(200, 215));
		list3.add(new Point(300, 135));
		list3.add(new Point(400, 450));
		list3.add(new Point(500, 315));
		list3.add(new Point(600, 585));
		list3.add(new Point(700, 250));
		list3.add(new Point(800, 375));
		list3.add(new Point(900, 85));
		
//		_points = new ArrayList<Point>();
//        for(int i = 0; i < 10; i++){
//        	((ArrayList<Point>)_points).add(new Point(i*100,i*50));
//        }
		
		DataSet set1 = new DataSet("Test1", "TestUnits1", list);
		DataSet set2 = new DataSet("Test2", "TestUnits2", list2);
		DataSet set3 = new DataSet("Test2", "TestUnits3", list3);
		
		DataSet[] testSets = {set1,set2,set3};
		setData(testSets);
		
	}
	
}