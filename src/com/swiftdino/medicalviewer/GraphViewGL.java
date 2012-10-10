package com.swiftdino.medicalviewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class GraphViewGL extends GLSurfaceView{
	
	private final GraphRendererGL mRenderer;
	
	public GraphViewGL(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new GraphRendererGL();
		setRenderer(mRenderer);
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GraphViewGL(Context context, AttributeSet attrs) {
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new GraphRendererGL();
		setRenderer(mRenderer);
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GraphViewGL(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new GraphRendererGL();
		setRenderer(mRenderer);
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        
        //Log.d("","" + e.getX());
        
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
            	
            	if(mPreviousX != 0 && mPreviousY != 0){
	                float dx = x - mPreviousX;
	                float dy = mPreviousY - y;
	                
	                if(Math.abs(dx) > Math.abs(dy)){
	                	mRenderer.offset.x -= dx;//(getWidth()/2);
	                	//mRenderer.offset.y += dy/(getHeight()/2);
	                }
	                else{
	                	mRenderer.scale.x += dy/((float)getHeight());
	                	mRenderer.scale.y += dy/((float)getHeight());
	                }
	                
	                requestRender();
            	}
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
	
}
