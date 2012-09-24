package com.swiftdino.medicalviewer;

import android.graphics.Point;
import android.graphics.PointF;

public class DataSet {
	
	//data type
	private String _name;
	
	//unit label
	private String _units;
	
	//data
	private Iterable<PointF> _data;
	
	//maximum data value
	public float maxVal;
	
	//minimum data value
	public float minVal;
	
	//data value range
	public float range; 
	
	//takes in data type, unit label and data as well as sets the max/min/range of data
	public DataSet(String name, String units, Iterable<PointF> data){
		_name = name;
		_units = units;
		_data = data;
		setExtremeVals();
	}
	
	public Iterable<PointF> getData(){
		return _data;
	}
	
	public String getName(){
		return _name;
	}
	
	public String getUnits(){
		return _units;
	}
	
	public void setData(Iterable<PointF> data){
		_data = data;
	}
	
	public void setName(String name){
		_name = name;
	}
	
	public void setUnits(String units){
		_units = units;
	}
	
	private void setExtremeVals(){
		maxVal = Float.MIN_VALUE;
		minVal = Float.MAX_VALUE;
		for(PointF p : _data){
			if(p.y > maxVal) maxVal = p.y;
			if(p.y < minVal) minVal = p.y;
		}
		range = maxVal - minVal;
	}
	
}
