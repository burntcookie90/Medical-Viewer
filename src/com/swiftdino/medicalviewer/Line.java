package com.swiftdino.medicalviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * drawn openGL line
 * @author SwiftDino
 *
 */
public class Line {

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

	// renderer program
	private final int mProgram;

	// handles for attrs
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int DIM_2D = 2;
	static final int DIM_3D = 3;
	private int currentDim = DIM_3D;

	// default size if no coords are passed in (3000 data points)
	private float plotCoords[] = new float[9000];

	// number of points and bytes per point
	private final int vertexCount;
	private final int vertexStride;
	
	/**
	 * new line
	 * @param coords endpoints of line {x1,y1,z1,x1,y2,z2}
	 */
	public Line(float[] coords) {

		plotCoords = coords;

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
	
	/**
	 * draw line with model view projection matrix and color
	 * @param mvpMatrix model view projection matrix
	 * @param color color to draw in
	 */
	public void draw(float[] mvpMatrix, float[] color) {

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

}
