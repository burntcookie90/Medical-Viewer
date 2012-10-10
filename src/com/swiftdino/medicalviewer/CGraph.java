package com.swiftdino.medicalviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import android.opengl.GLES20;

public class CGraph {

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

	private final FloatBuffer vertexBuffer;
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int DIM_2D = 2;
	static final int DIM_3D = 3;
	private int currentDim = DIM_3D;

	private float plotCoords[] = new float[9000];

	private final int vertexCount = plotCoords.length / currentDim;
	private final int vertexStride = currentDim * 4; // 4 bytes per vertex

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

		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, currentDim,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		// Set color for drawing the triangle
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GraphRendererGL.checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		GraphRendererGL.checkGlError("glUniformMatrix4fv");

		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);

	}
}
