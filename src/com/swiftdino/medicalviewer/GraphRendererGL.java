package com.swiftdino.medicalviewer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class GraphRendererGL implements GLSurfaceView.Renderer {
	
	private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private CGraph mcGraph;
    
    private static int fps = 0;
    private int frameCount = 0;
    private long lastCheck;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];

    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;
    
    public volatile PointF offset;
    public volatile PointF scale;
    
    public static int getFps(){
    	return fps;
    }
    
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        offset = new PointF(0f,0f);
        scale = new PointF(1f,1f);

        mTriangle = new Triangle();
        mcGraph = new CGraph();
        
        lastCheck = System.currentTimeMillis();
    }

    public void onDrawFrame(GL10 unused) {
    	
    	frameCount++;
    	
    	if(System.currentTimeMillis() - lastCheck >= 1000){
    		lastCheck = System.currentTimeMillis();
    		fps = frameCount;
    		frameCount = 0;
    		DetailViewFragment.fps = fps;
    	}
    	
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 1.0f, 0, 0, 0f, 0f, 1.0f, 0f);
        //Matrix.setLookAtM(mVMatrix, 0, offset.x, offset.y, 1.0f, offset.x, offset.y, 0f, 0f, 1.0f, 0f);
        
        Matrix.translateM(mMVMatrix, 0, mVMatrix, 0, -offset.x, -offset.y, 0f);
        Matrix.scaleM(mMVMatrix, 0, scale.x, 1.0f, 1.0f);
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);

        // Create a rotation for the triangle
        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        
        // set current rotation
        //Matrix.translateM(mRotationMatrix, 0, offset.x, 0, 0);
        Matrix.setRotateM(mRotationMatrix, 0, 0, 0, 0, -1.0f);
        //Matrix.translateM(mRotationMatrix, 0, offset.x, 0, 0);

        // Combine the rotation matrix with the projection and camera view
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

        // Draw objects
        mTriangle.draw(mMVPMatrix, MyColors.BLUE_SOLID);
        mcGraph.draw(mMVPMatrix,MyColors.RED_SOLID);
        
        //Log.d("","" + offset.x + " : " + offset.y);
        
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1.0f, 100.0f);
        Matrix.orthoM(mProjMatrix, 0, 0, width, 0, height, 1.0f, 100.0f);
        //Matrix.orthoM(mProjMatrix, 0, -1, 1, -1, 1, 1.0f, 100.0f);
        
        Log.d("",width + " : " + height);

    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
	
}
