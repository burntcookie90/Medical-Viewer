package com.swiftdino.medicalviewer;

import android.graphics.Bitmap;

public class GraphicObject {
	
	private Bitmap _bitmap = null;
	private int xLoc = 0;
	private int yLoc = 0;
	
	public GraphicObject(Bitmap bitmap){
		_bitmap = bitmap;
	}
	
	public int getX(){
		return xLoc;
	}
	
	public int getY(){
		return yLoc;
	}
	
	public Bitmap getGraphic(){
		return _bitmap;
	}
	
	public void setX(int x){
		xLoc = x;
	}
	
	public void setY(int y){
		yLoc = y;
	}
	
	public void setGraphic(Bitmap bitmap){
		_bitmap = bitmap;
	}
	
}

