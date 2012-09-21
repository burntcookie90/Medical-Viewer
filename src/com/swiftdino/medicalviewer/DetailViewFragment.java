package com.swiftdino.medicalviewer;

import java.util.Random;

import com.swiftdino.medicalviewer.dummy.DummyContent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailViewFragment extends Fragment {

	final String TAG = "DetailViewFragment";
    public static final String ARG_ITEM_ID = "item_id";
    private DisplayPanel display;
    DummyContent.DummyItem mItem;
    Random rand = new Random();

    public DetailViewFragment()
    {
    
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "Creating view from detail_view_content. Finding TextView.");
        View view = (View) inflater.inflate(R.layout.detail_view_content, container, true); 
        
        // custom display panel that draws the graph
        display =  ((DisplayPanel) view.findViewById(R.id.displayPanel1));

   	    Log.d(TAG, "Found TextView and Returning View");
	    return view;
    }
    
	public void updateGraph(String content) {
	    if (display != null) {
	    	Log.d(TAG, "Updating graph.");
	     //Log.d(TAG, "Arguments: "+getArguments().getString(ARG_ITEM_ID));
         //   mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
	      //	Log.d(TAG, "Setting TextView to content.");
	    // Log.d(TAG, "mItem.content: "+mItem.content);
	    	//viewer.setText(mItem.content);
	    	//display.setText("graph");
	    	
	    	// sets zoom value of graph to a random value between .5 and 2.0
	    	float zoom = (rand.nextInt(20) + 5.0f) / 10.0f;
	    	display.setZoom(zoom);
	    }
	    else
	    {
	    	Log.d(TAG, "TextView is NULL!");
	    }
	}
}
