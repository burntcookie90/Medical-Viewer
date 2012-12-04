package com.swiftdino.medicalviewer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GraphViewGL extends GLSurfaceView{
	
	// opengl renderer
	private final GraphRendererGL mRenderer;
	
	// release location
	private static float lastTouch = 0.0f;
	private static float lastY = 0.0f;
	
	// constructors (overloads necessary to specify instance in xml)
	public GraphViewGL(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		mRenderer = new GraphRendererGL(context, MyColors.BLACK_SOLID);
		setRenderer(mRenderer);
		
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GraphViewGL(Context context, AttributeSet attrs) {
		super(context,attrs);
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		mRenderer = new GraphRendererGL(context, MyColors.BLACK_SOLID);
		setRenderer(mRenderer);
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GraphViewGL(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		mRenderer = new GraphRendererGL(context, MyColors.BLACK_SOLID);
		setRenderer(mRenderer);
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	// previous point values for dragging
    private float mPreviousX;
    private float mPreviousY;
    
    // location of the first pointer down
    private PointF firstDown;
    private long firstTime;
    
    // flags for if the screen is zoomed and if it has been dragged
    private boolean zoomed = false;
    private boolean dragged = false;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
    	
    	// current pointer location
        float x = e.getX();
        float y = e.getY();
        
        //Log.d("","" + e.getX());
        
        switch (e.getAction()) {
            
        	// finger has touched the screen
        	case MotionEvent.ACTION_DOWN:
        		
        		if(e.getPointerCount() < 2) 
        			firstDown = new PointF(e.getX(),e.getY());
        			firstTime = System.currentTimeMillis();
        		
        		break;
        	
        	// finger has moved
        	case MotionEvent.ACTION_MOVE:
            	
        		float dist = dist(e.getX(),e.getY(),firstDown.x,firstDown.y);
        		
        		// if move is significant
        		if(dist > 10){
        			dragged = true;
        		}
        		
        		// set offsets for dragging
            	if(mPreviousX != 0 && mPreviousY != 0){
	                float dx = x - mPreviousX;
	                float dy = mPreviousY - y;
	                
	                if(Math.abs(dx) > Math.abs(dy)){
	                	mRenderer.offset.x -= dx;//(getWidth()/2);
	                	//mRenderer.offset.y += dy/(getHeight()/2);
	                }
	                else{
	                	mRenderer.scale.x += (dy/((float)getHeight()))*mRenderer.scale.x;
	                	//mRenderer.scale.y += (dy/((float)getHeight()))*mRenderer.scale.y;
	                }
	                
	                requestRender();
	                
            	}
            	
            	break;
            
            // finger released
            case MotionEvent.ACTION_UP:
            	
            	lastTouch = (e.getX() + mRenderer.offset.x)/mRenderer.scale.x;
            	lastY = (getHeight() - e.getY() + mRenderer.offset.y)/mRenderer.scale.y;	
            	
            	//Log.d("","" + ((getHeight()-e.getY())/mRenderer.scale.y - mRenderer.getBuffer()));
            	//Log.d("","" + ((getHeight()-e.getY() + mRenderer.offset.y)/mRenderer.scale.y ));
            	
            	if(System.currentTimeMillis() - firstTime > 1000 && !dragged){
            		int graphIndex = (int)(getHeight() - e.getY()) / (getHeight() / mRenderer.getSets().length);
            		openAnnotationEditDialog(lastTouch, lastY, mRenderer.scale.x/mRenderer.scale.y,graphIndex,mRenderer.getSets()[graphIndex].getAnnotation(lastTouch, lastY));
            	}
            	
            	else if(!dragged && !zoomed){
            		Annotation a;
            		int graphIndex = (int)(getHeight() - e.getY()) / (getHeight() / mRenderer.getSets().length);
            		if(mRenderer.getFocusedGraph() == null) a = mRenderer.getSets()[graphIndex].getAnnotation(lastTouch, lastY);
            		else a = mRenderer.getFocusedGraph().getAnnotation(lastTouch, lastY);
            		if(a != null) openAnnotationViewDialog(a);
            		else{
            			mRenderer.zoomTo(mRenderer.getSets()[graphIndex], getWidth(), getHeight());
            			zoomed = true;
            		}
            	}
            	
            	else if(!dragged){
            		Annotation a;
            		int graphIndex = (int)(getHeight() - e.getY()) / (getHeight() / mRenderer.getSets().length);
            		if(mRenderer.getFocusedGraph() == null) a = mRenderer.getSets()[graphIndex].getAnnotation(lastTouch, lastY);
            		else a = mRenderer.getFocusedGraph().getAnnotation(lastTouch, lastY);
            		if(a != null) openAnnotationViewDialog(a);
            		else{
            			mRenderer.zoomAll(getWidth(), getHeight());
            			zoomed = false;
            		}
            	}
            	
            	dragged = false;
            	
            	break;
            	
        }
        
        // set previous point to current for next event
        mPreviousX = x;
        mPreviousY = y;
        return true;
        
    }
    
    public static float getLastTouch(){
    	return lastTouch;
    }
    
    public float dist(float x1, float y1, float x2, float y2){
    	
    	return FloatMath.sqrt((float)Math.pow(x2-x1,2) + (float)Math.pow(y2-y1,2));
    	
    }
	
    public void doStuff(){
    	mRenderer.testStuff();
    }
    
    public GraphRendererGL getRenderer(){
    	return mRenderer;
    }
    
    // allows the list activity to set renderer's zoom
    public void zoomTo(int i){
    	mRenderer.zoomTo(i);
    	zoomed = true;
    }
    
    private void openAnnotationEditDialog(final float timeStamp, final float yVal,final float scale, final int graphIndex, final Annotation anot){
    	
    	// custom dialog
    	final Dialog dialog = new Dialog(getContext());
    	dialog.setContentView(R.layout.annotation_edit_dialog);
    	dialog.setTitle("Annotation Edit");
    	
    	// set the custom dialog components
    	final TextView text = (TextView) dialog.findViewById(R.id.annotationField);
    	if(anot != null) text.setText(anot.get_content());
    	else text.setHint("Annotation here");//text.setText("(Annotation Here)");
    	
    	Button saveButton = (Button) dialog.findViewById(R.id.buttonSave);
    	saveButton.setText("Save");
    	// if button is pressed save annotation with input timestamp and text
    	saveButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v){
    			if(anot != null) anot.set_content(text.getText().toString());
    			else mRenderer.getSets()[graphIndex].addAnnotation(new Annotation(timeStamp,yVal,scale,text.getText().toString(), mRenderer.getSets()[graphIndex]));
    			dialog.dismiss();
    		}
    	});
    	
    	Button cancelButton = (Button) dialog.findViewById(R.id.buttonCancel);
    	cancelButton.setText("Cancel");
    	// if button is clicked, close the custom dialog
    	cancelButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			dialog.dismiss();
    		}
    	});
    	
    	dialog.show();
    	
    }
    
    private void openAnnotationViewDialog(final Annotation anot){
    	
    	// custom dialog
    	final Dialog dialog = new Dialog(getContext());
    	dialog.setContentView(R.layout.annotation_view_dialog);
    	dialog.setTitle("Annotation View");
    	
    	// set the custom dialog components
    	final TextView text = (TextView) dialog.findViewById(R.id.annotationView);
    	text.setText(anot.get_content());
    	
    	Button saveButton = (Button) dialog.findViewById(R.id.buttonEdit);
    	saveButton.setText("Edit");
    	// if button is pressed save annotation with input timestamp and text
    	saveButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v){
    			openAnnotationEditDialog(0,0,0,0,anot);
    			dialog.dismiss();
    		}
    	});
    	
    	Button cancelButton = (Button) dialog.findViewById(R.id.buttonBack);
    	cancelButton.setText("Back");
    	// if button is clicked, close the custom dialog
    	cancelButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			dialog.dismiss();
    		}
    	});
    	
    	dialog.show();
    	
    }
    
}
