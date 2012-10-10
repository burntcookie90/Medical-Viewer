package com.swiftdino.medicalviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Triangle {

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

	static float triangleCoords[] = { // in counterclockwise order:
			448.0f, 628.0f, 0.0f, // top
			0.0f, 0.0f, 0.0f, // bottom left
			896.0f, 0.0f, 0.0f // bottom right
	};
	
	private final int vertexCount = triangleCoords.length / currentDim;
	private final int vertexStride = currentDim * 4; // 4 bytes per vertex

	public Triangle() {
		
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (number of coordinate values * 4 bytes per float)
				triangleCoords.length * 4);
		
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(triangleCoords);
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
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);

	}

}