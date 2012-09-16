package com.swiftdino.medicalviewer;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DisplayPanel extends SurfaceView implements SurfaceHolder.Callback {
	
	//debug values
	private int _debug = 1;
	private final int DEBUG_MULTI_TOUCH = 0;
	private final int DEBUG_PLOT_POINTS = 1;
	
	//thread for canvas
	private CanvasThread _thread;
	
	//test lists for drawn objects
	private ArrayList<GraphicObject> _graphics = new ArrayList<GraphicObject>();
	private ArrayList<Point> _plotPoints = new ArrayList<Point>(10); 
	
	//input data points
	Iterable<Point> _points = null;
	
	//offset for frame panning
	private Point _frameOffset = new Point();
	private boolean pannable = true;
	private int panTimer = 0;
	private float _zoomValue = 1;
	
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
	
	public void setData(Iterable<Point> data){
		_points = data;
	}

	
	public int distance(float x1, float y1, float x2, float y2){
		return (int)Math.sqrt(Math.pow(x2- x1, 2) + Math.pow(y2-y1, 2));
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
					
					// get the diff between the pointer locations
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
			canvas.drawColor(Color.WHITE);
			
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
					
					paint.setColor(Color.BLACK);
					
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
					paint.setAlpha(120);
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
    	
		_frameOffset.set(0 + _uiBuffer,getHeight() - _uiBuffer);

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
		
//		_points = new ArrayList<Point>();
//        for(int i = 0; i < 10; i++){
//        	((ArrayList<Point>)_points).add(new Point(i*100,i*50));
//        }
		
	}
	
}