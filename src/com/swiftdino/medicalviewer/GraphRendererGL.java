package com.swiftdino.medicalviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class GraphRendererGL implements GLSurfaceView.Renderer {
	
	private static final String TAG = "MyGLRenderer";
	
	// context passed down from the view
	private Context ctx;
	
	// flag for initial zoom call
	private boolean _initialized = false;
	
	private float[] _bgColor;
	
	// current graph focus (null if zoomed to all)
	private CGraph _currentZoomed = null;
	
	// drawn objects
    private CGraph[] _dataSets;
    private float[][] _colors = {
    		MyColors.BLUE_SOLID, 
    		MyColors.RED_SOLID
    };
    
    // ui elements
    private int cOrientation;
    public static final int ORIENTATION_LANDSCAPE = 0;
    public static final int ORIENTATION_PORTRAIT = 1;
    private Rectangle[][] uiRects = new Rectangle[2][1];
    
    // fps display
    private static float fps = 0;
    private int frameCount = 0;
    private long lastCheck;
    
    // transform matrices
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];
    
    private final float[] mMVPMatrixUI = new float[16];

    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;
    public volatile PointF offset;
    public volatile PointF scale;
    
    // spacing between graphs and from edge of screen (percentage of graph size where 1 = 100%)
    private float _uiBuffer = 0.4f;
    
    // current screen width and height
    private int cWidth,cHeight;
    
    private GLText _text;
    
    public GraphRendererGL(Context context, float[] bgColor){
    	super();
    	ctx = context;
    	_bgColor = bgColor;
    }
    
    // new surface is created
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	
        // Set the background frame color
        GLES20.glClearColor(_bgColor[0],_bgColor[1],_bgColor[2],_bgColor[3]);
        
        // initialize camera position and scale
        offset = new PointF(-50.0f,0f);
        scale = new PointF(1f,1f);
        
        // set first time for fps check
        lastCheck = System.currentTimeMillis();
        
        // create graph data from csv files
        try{
        	_dataSets = csv2DataSet();
        } catch (Exception e){
        	Log.d(TAG,e.toString());
        }
        
        _text = new GLText(gl,ctx.getAssets());
        _text.load( "Roboto-Regular.ttf", 14, 2, 2 );
        
    }
    
    // every frame
    public void onDrawFrame(GL10 gl) {
    	
    	// fps calculations
    	frameCount ++;
    	if(System.currentTimeMillis() - lastCheck > 1000.0f){
    		fps = frameCount;
    		frameCount = 0;
    		DetailViewFragment.fps = fps;
    		lastCheck = System.currentTimeMillis();
    	}
    	
    	
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 1.0f, 0, 0, 0f, 0f, 1.0f, 0f);
        //Matrix.setLookAtM(mVMatrix, 0, offset.x, offset.y, 1.0f, offset.x, offset.y, 0f, 0f, 1.0f, 0f);
        
        // View matrix with scale and transforms
        Matrix.translateM(mMVMatrix, 0, mVMatrix, 0, -offset.x, -offset.y, 0f);
        Matrix.scaleM(mMVMatrix, 0, scale.x, scale.y, 1.0f);
        
        // MVP Matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);
        
        // MVP Matrix for ui (no scale and transform)
        Matrix.multiplyMM(mMVPMatrixUI, 0, mProjMatrix, 0, mVMatrix, 0);

        
        // set current rotation
        //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

        // Draw objects  
        if(_dataSets != null){
        	int i = 0;
        	for(CGraph g : _dataSets){
        		g.draw(mMVPMatrix, _colors[i++]);
        	}
        }
        
        // draw ui elements
        for(Rectangle r : uiRects[cOrientation]){
        	r.draw(mMVPMatrixUI, _bgColor);
        }
        
        //Log.d("","" + offset.x + " : " + offset.y);
        
    }
    
    // when surface changes (orientation change etc)
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    	
    	if(width > height) cOrientation = ORIENTATION_LANDSCAPE;
    	else cOrientation = ORIENTATION_PORTRAIT;
    	
    	if(!_initialized){
    		zoomAll(width, height);
    		_initialized = true;
    	}
    	
    	// set current screen dimensions
    	cWidth = width;
    	cHeight = height;
    	
    	// create ui elements if none exist for current orientation
    	if(uiRects[cOrientation][0] == null){
        	uiRects[cOrientation][0] = new Rectangle(new float[] {
            		cWidth*.75f, cHeight, 0.0f,
            		cWidth, cHeight, 0.0f,
            		cWidth, 0.0f, 0.0f,
            		cWidth*.75f, 0.0f, 0.0f});
        }
    	
    	// Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1.0f, 100.0f);
        Matrix.orthoM(mProjMatrix, 0, 0, width, 0, height, 1.0f, 100.0f);
        //Matrix.orthoM(mProjMatrix, 0, -1, 1, -1, 1, 1.0f, 100.0f);
        
        //Log.d("",width + " : " + height);

    }
    
    // load shader from objects shader code
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
    
    // load in data from csv file
    private CGraph[] csv2DataSet() throws IOException{
    	
    	InputStream _csvRaw = ctx.getAssets().open("samples.csv");
		BufferedReader csv = new BufferedReader(new InputStreamReader(_csvRaw));
		
		String line = csv.readLine();
		String[] v = line.split(",");
		
		int sets = v.length - 1;
		int valCount = 0;
		float[] extremeVals = new float[sets*2];
		for(int i = 0;i<extremeVals.length;i++){
			if(i%2 == 0) extremeVals[i] = Float.MIN_VALUE;
			else extremeVals[i] = Float.MAX_VALUE;
		}
		
		CGraph[] _dataSets = new CGraph[sets];
		Object[] pts = new Object[sets];
		for(int i = 0; i < sets; i++){
			pts[i] = new ArrayList<PointF>();
		}
		
		String[] names = new String[sets];
		for(int i = 1; i < sets+1; i++){
			names[i-1] = v[i].replaceAll("'", "");
		}
		
		line = csv.readLine();
		v = line.split(",");
		
		String[] units = new String[sets];//(line.split(",")[1]).replace("'", "");
		
		for(int i = 1; i < sets+1; i++){
			units[i-1] = v[i].replaceAll("'", "");
		}
				
		line = csv.readLine();
		
		float startTime = -1;
		float[] mults = new float[] {360.0f, 60.0f, 1.0f};
		
		while(line != null){
			
			//Log.d("", "A");
			
			String[] values = line.split(",");
			
			values[0] = values[0].replaceAll("'", "");
			values[0] = values[0].replaceAll("\\[", "");
			values[0] = values[0].replaceAll("\\]", "");
			float time = 0;
			int index = 0;
			for(String s : values[0].split(":")){
				time += (new Float(s))*mults[index++];
			}
			
			if(startTime < 0) startTime = time;
			time -= startTime;
			
			//Log.d("","B");
			
			for(int i = 0; i < sets; i++){
				ArrayList<PointF> current = (ArrayList<PointF>)pts[i];
				float pt = new Float(values[i+1]);
				current.add(new PointF(time,pt));
				if(pt > extremeVals[i*2]) extremeVals[i*2] = pt;
				if(pt < extremeVals[i*2+1]) extremeVals[i*2+1] = pt;
			}
			
			valCount++;
			
			line = csv.readLine();
		}
		
		for(int i = 0; i < extremeVals.length; i+=2){
			extremeVals[i] = Math.abs(extremeVals[i] - extremeVals[i+1]);
		}
		
		float[][] ranges = new float[sets][2];
		for (int i = 0; i<sets;i++){
			float offset = 0.0f;
			System.arraycopy(extremeVals, i*2, ranges[i], 0, 2);
			if(i>0) offset = _uiBuffer + _dataSets[i-1].getMax();
			_dataSets[i] = new CGraph((ArrayList<PointF>)pts[i], i, ranges[i], offset, startTime);
		}
		
		return _dataSets;	
    	
    }
	
    // zoom to a single graph
    public void zoomTo(CGraph graph, int width, int height){
    	scale.x = ((float)width)/graph.getTime();
    	scale.y = ((float)height)/(graph.getRange() + _uiBuffer);
    	offset.y = scale.y * (graph.getMin() - _uiBuffer/2.0f);
    	_currentZoomed = graph; 
    }
    
    // zoom to all current graphs
    public void zoomAll(int width, int height){
    	float num = _dataSets.length;
    	scale.x = ((float)width)/_dataSets[0].getTime();
    	scale.y = ((float)height)/(_dataSets.length + _uiBuffer*(num+1.0f));
    	offset.y = scale.y * (_dataSets[0].getMin() - _uiBuffer);
    	_currentZoomed = null;
    }
    
    // zoom to a single graph based on index
    public void zoomTo(int i){
    	zoomTo(_dataSets[i],cWidth, cHeight);
    }
    
    public CGraph getFocusedGraph(){
    	return _currentZoomed;
    }
    
    // return ui buffer
    public float getBuffer(){
    	return _uiBuffer;
    }
    
    // return data sets
    public CGraph[] getSets(){
    	return _dataSets;
    }
    
    public static float getFps(){
    	return fps;
    }
    
    // just testing performance
    public void testStuff(){
    	float[] test = new float[9000];
		for(int i = 0; i < test.length; i++){
			test[i] = (float)i;
		}
		for(float f : test){
			f = f * 2.4f - 1.2f;
		}
		
		Log.d("","Calculation Done!");
    }
    
}
