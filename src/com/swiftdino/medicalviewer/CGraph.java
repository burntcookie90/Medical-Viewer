package com.swiftdino.medicalviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

public class CGraph {

	// shader codes
	private final String vertexShaderCode =
	// This matrix member variable provides a hook to manipulate
	// the coordinates of the objects that use this vertex shader
	"uniform mat4 uMVPMatrix;" +

	"attribute vec4 vPosition;" + "void main() {" +
	// the matrix must be included as a modifier of gl_Position
			"  gl_Position = uMVPMatrix * vPosition;" + "}";
	private final String fragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";
	
	// buffer for point values
	private final FloatBuffer vertexBuffer;
	
	//renderer program
	private final int mProgram;
	
	// handles for attrs
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	
	// associated grids/labels
	private Line[] _grids;
	
	// associated annotations
	private ArrayList<Annotation> _annotations = new ArrayList<Annotation>(); 
	
	// number of coordinates per vertex in this array
	static final int DIM_2D = 2;
	static final int DIM_3D = 3;
	private int currentDim = DIM_3D;
	
	// default size if no coords are passed in (3000 data points)
	private float plotCoords[] = new float[9000];
	
	// number of points and bytes per point
	private final int vertexCount;
	private final int vertexStride;
	
	// initial timestamp
	private float _startTime = 0.0f;
	
	// range of y values (in current implementation should always be 1)
	private float _range;
	
	// time range
	private float _time;
	
	// min value (normalized and offset)
	private float _min;
	
	// max value (normalized and offset)
	private float _max;
	
	//avg value
	private float _avg;
	
	// multiplier converting actual value to normalized value
	private float _nFactor;
	
	// y value offset based on index in _dataSet array
	private float _vOffset;

	public CGraph() {
		
		Random rand = new Random();
		
		// dummy test data
		for(int i = 0; i < plotCoords.length; i++){
			switch(i%3){
				case 0:
					plotCoords[i] = i/(plotCoords.length/896);
					break;
				case 1:
					plotCoords[i] = rand.nextFloat()*628;
					break;
				case 2:
					plotCoords[i] = 0f;
					break;
			}
		}
		
		vertexCount = plotCoords.length / currentDim;
		vertexStride = currentDim * 4; // 4 bytes per vertex
		
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (number of coordinate values * 4 bytes per float)
				plotCoords.length * 4);

		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(plotCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		// prepare shaders and OpenGL program
		int vertexShader = GraphRendererGL.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = GraphRendererGL.loadShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // create OpenGL program executables

	}
	
	// real values input
	public CGraph(ArrayList<PointF> pts, int index, float[] rangeMin, float vOffset, float startTime){
		
		_startTime = startTime;
		
		// multiplier to normalize
		_nFactor = 1.0f/rangeMin[0];
		
		// offset based on index
		_vOffset = vOffset - (rangeMin[1]*_nFactor);
		
		// set pts
		plotCoords = new float[pts.size()*3];
		int i = 0;
		float tot = 0;
		float max = Float.MIN_VALUE;
		for(PointF p : pts){
			plotCoords[i++] = p.x;
			plotCoords[i++] = p.y*_nFactor + _vOffset;
			plotCoords[i++] = 0.0f;
			if(plotCoords[i-2] > max) max = plotCoords[i-2];
			tot += p.y;
		}
		
		vertexCount = plotCoords.length / currentDim;
		vertexStride = currentDim * 4; // 4 bytes per vertex
		
		_avg = tot/((float)vertexCount);
		_min = rangeMin[1]*_nFactor + _vOffset;
		_max = max;
		_range = rangeMin[0]*_nFactor;
		
		// x values represent diff from start value
		_time = pts.get(pts.size()-1).x;
		
		//create grids
		int secs = roundUp(_time);
		int rangeVals = roundUp(_range/_nFactor);
		float start = -(rangeVals/2);
		_grids = new Line[5+secs];
		_grids[0] = new Line(new float[] {
				plotCoords[0],_min,0.0f,
				secs,_min,0.0f
		});
		for(int j = 0;j<secs+1;j++){
			_grids[j+1] = new Line(new float[] {
					plotCoords[0] + (float)j,_max,0.0f,
					plotCoords[0] + (float)j,_min - .03f,0.0f
			});
		}
		_grids[secs+2] = new Line(new float[] {
				plotCoords[0], _min, 0.0f,
				plotCoords[0], _max, 0.0f
		});
		_grids[secs+3] = new Line(new float[] {
				plotCoords[0] - .03f, _vOffset, 0.0f,
				secs, _vOffset, 0.0f
		});
		_grids[secs+4] = new Line(new float[] {
				plotCoords[0] - .03f, _max, 0.0f,
				secs, _max, 0.0f
		});
		
		
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
				// (number of coordinate values * 4 bytes per float)
				plotCoords.length * 4);

		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(plotCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		// prepare shaders and OpenGL program
		int vertexShader = GraphRendererGL.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = GraphRendererGL.loadShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // create OpenGL program executables
		
	}
	
	public void draw(float[] mvpMatrix, float[] color) {

		// draw associated grids etc
		for(Line l : _grids){
			if(l != null)l.draw(mvpMatrix, MyColors.GREY_TRANSPARENT);
		}
		
		// draw annotation markers
		for (Annotation a : _annotations){
			a.getRectangle().draw(mvpMatrix, MyColors.GREY_TRANSPARENT);
		}
		
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the graph vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the graph coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, currentDim,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		// Set color for drawing the graph
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GraphRendererGL.checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		GraphRendererGL.checkGlError("glUniformMatrix4fv");

		// Draw the data points
		GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);	
		
	}
	
	public float getRange(){
		return _range;
	}
	
	public float getTime(){
		return _time;
	}
	
	public float getMin(){
		return _min;
	}
	
	public float getZero(){
		return _vOffset;
	}
	
	public float getMax(){
		return _max;
	}
	
	public float getAvg(){
		return _avg;
	}
	
	public float getMid() {
		return (getMin() + getMax())/2.0f;
	}
	
	public float getFactor(){
		return _nFactor;
	}
	
	private int round(float num){
		if(num - (int)num < .5f) return (int)(num);
		else return (int)(num + 1);
	}
	
	private int roundUp(float num){
		if(num - (int)num > .000001f) return (int)(num + 1);
		else return (int) num;
	}
	
	public void addAnnotation(Annotation a){
		_annotations.add(a);
		Log.d("","Annotation added: " + a.get_content());
	}
	
	public ArrayList<Annotation> getAnnotations(){
		return _annotations;
	}
	
	public Annotation getAnnotation(float time, float yVal){
		Annotation found = null; 
		for(Annotation a : _annotations){
			if(a.getStart() <= time && time <= a.getEnd()){
				if(a.getBot() <= yVal && yVal <= a.getTop()) found = a;
			}
		}
		
		return found;
		
	}
	
}
