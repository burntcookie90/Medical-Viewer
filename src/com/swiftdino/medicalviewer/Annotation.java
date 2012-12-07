package com.swiftdino.medicalviewer;

import android.view.Display;


/**
 * 
 * Represents an annotation that is placed on a data graph
 * 
 * @author SwiftDino
 *
 */
public class Annotation {
	
	// time stamp for annotation
	private float _timeStamp;
	
	// annotation content
	private String _content;
	
	// drawable rectangle representing the annotation on the graph
	private Rectangle _display;
	
	// extreme values
	private float _start;
	private float _end;
	private float _bot;
	private float _top;
	private float rad = .4f;
	
	/**
	 * 
	 * @param timeStamp time of the annotation
	 * @param yVal	data value of the annotation
	 * @param scale	opengl render scale
	 * @param content annotation text
	 * @param graph graph associated with the annotation
	 */
	public Annotation(float timeStamp, float yVal,float scale, String content, CGraph graph){
		_timeStamp = timeStamp;
		_content = content;
		_display = new Rectangle(new float[] {
				timeStamp - rad, yVal - rad*scale, -1.0f,
				timeStamp + rad, yVal - rad*scale, -1.0f,
				timeStamp + rad, yVal + rad*scale, -1.0f,
				timeStamp - rad, yVal + rad*scale, -1.0f
		});
		_start = timeStamp - rad;
		_end = timeStamp + rad;
		_bot = yVal - rad*scale;
		_top = yVal + rad*scale;
	}

	public float get_timeStamp() {
		return _timeStamp;
	}

	public void set_timeStamp(float timeStamp) {
		this._timeStamp = timeStamp;
	}

	public String get_content() {
		return _content;
	}

	public void set_content(String content) {
		this._content = content;
	}
	
	public Rectangle getRectangle(){
		return _display;
	}
	
	public float getStart(){
		return _start;
	}
	
	public float getEnd(){
		return _end;
	}
	
	public float getBot(){
		return _bot;
	}
	
	public float getTop(){
		return _top;
	}
	
}
