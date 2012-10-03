package com.swiftdino.medicalviewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class GraphViewGL extends GLSurfaceView{
	
	private final GraphRendererGL mRenderer;
	
	public GraphViewGL(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new GraphRendererGL();
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GraphViewGL(Context context, AttributeSet attrs) {
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new GraphRendererGL();
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GraphViewGL(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new GraphRendererGL();
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
	                float dy = y - mPreviousY;
	                
	                //mRenderer.offset.x += dx/getWidth();
	               	//mRenderer.offset.y += dy/getHeight();
	                
	                // reverse direction of rotation above the mid-line
	                if (y > getHeight() / 2) {
	                  dx = dx * -1 ;
	                }
	
	                // reverse direction of rotation to left of the mid-line
	                if (x < getWidth() / 2) {
	                  dy = dy * -1 ;
	                }
	
	                mRenderer.mAngle -= (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
	                requestRender();
            	}
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
	
}
