package com.swiftdino.medicalviewer;

import com.swiftdino.medicalviewer.dummy.DummyContent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailViewFragment extends Fragment {

	final String TAG = "DetailViewFragment";
    public static final String ARG_ITEM_ID = "item_id";
    private TextView viewer;
    DummyContent.DummyItem mItem;

    public DetailViewFragment()
    {
    
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.d(TAG, "Creating view from detail_view_content. Finding TextView.");
        View view = (View) inflater.inflate(R.layout.detail_view_content, container, true); 
        viewer =  ((TextView) view.findViewById(R.id.detail_textview));

   	    Log.d(TAG, "Found TextView and Returning View");
	    return view;
    }
    
	public void updateGraph(String content) {
	    if (viewer != null) {
	    	Log.d(TAG, "Updating graph.");
	     //Log.d(TAG, "Arguments: "+getArguments().getString(ARG_ITEM_ID));
         //   mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
	      //	Log.d(TAG, "Setting TextView to content.");
	    // Log.d(TAG, "mItem.content: "+mItem.content);
	    	//viewer.setText(mItem.content);
	    	viewer.setText("graph");
	    }
	    else
	    {
	    	Log.d(TAG, "TextView is NULL!");
	    }
	}
}
