package com.swiftdino.medicalviewer;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.swiftdino.medicalviewer.dummy.DummyContent;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class DetailViewFragment extends Fragment {

	final String TAG = "DetailViewFragment";
    public static final String ARG_ITEM_ID = "item_id";
    private View _view;
    private GraphViewGL display;
    DummyContent.DummyItem mItem;
    Random rand = new Random();
    
    private static TextView fpsView;
    public static float fps = 0;
    private Timer myTimer;
    final Handler myHandler = new Handler();
    
    public DetailViewFragment()
    {
    	myTimer = new Timer();
        myTimer.schedule(new TimerTask() {          
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 1000);

    }
    
    private void TimerMethod(){
    	myHandler.post(myRunnable);
    }
    
    final Runnable myRunnable = new Runnable() {
    	public void run() {
    		fpsView.setText("" + fAcc(fps,2));
    	}
    };
    
    private float fAcc(float orig, int acc){
    	float temp = (float)(orig * Math.pow(10, acc));
    	float fixed = (int)temp;
    	return (float)(fixed/Math.pow(10, acc));
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	setRetainInstance(true);
    	Log.d(TAG, "Creating view from detail_view_content. Finding TextView.");
        View view = (View) inflater.inflate(R.layout.detail_view_content, container, true);
        _view = view;
        
        // custom display panel that draws the graph
        display =  (GraphViewGL) view.findViewById(R.id.graphViewGL1);
        fpsView = (TextView)view.findViewById(R.id.fpsView);
        
   	    Log.d(TAG, "Found TextView and Returning View");
	    return view;
	    
    }
    
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	final RelativeLayout layout=(RelativeLayout)getActivity().findViewById(R.id.RelativeLayout1);
    	final int NUMBER_OF_GRAPHS = 2;
    	final int GRAPH_OFFSET = 450;
    	
    	for(int i=1; i<=NUMBER_OF_GRAPHS; i++){
	    	for(int x=0; x<8; x++){
	    		TextView tx=new TextView(getActivity());
	    		tx.setText(x*10+"");
	        	tx.setY(GRAPH_OFFSET*i);
	        	tx.setX(x*80);
	        	layout.addView(tx);
	    	}
	    	for(int y=0; y<7; y++){
	    		TextView ty=new TextView(getActivity());
	    		ty.setText(y*3+"");
	        	ty.setY(GRAPH_OFFSET*i-(y*50));
	        	ty.setX(0);
	        	layout.addView(ty);
	    	}
    	}
    	//LayoutParams params = (LayoutParams)tx.getLayoutParams()
    	
    	
    	//Animation a = AnimationUtils.loadAnimation(getActivity(), R.xml.rotate);
        ///a.reset();
        //ty.startAnimation(a);
    }
    
	public void updateGraph(String content, int position) {
	    if (display != null) {
	    	// zoom to the graph at index - position%2
	    	display.zoomTo(position%2);
	    	Log.d(TAG, "Updating graph.");
	    	//display.doStuff();
	    //Log.d(TAG, "Arguments: "+getArguments().getString(ARG_ITEM_ID));
         //   mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
	      //	Log.d(TAG, "Setting TextView to content.");
	    // Log.d(TAG, "mItem.content: "+mItem.content);
	    	//viewer.setText(mItem.content);
	    	//display.setText("graph");
	    	
	    	// sets zoom value of graph to a random value between .5 and 2.0
	    	//float zoom = (rand.nextInt(20) + 5.0f) / 10.0f;
	    	//display.setZoom(zoom);
	    	//Object p = lV.getItemAtPosition(position);
	    	//display.setCurrentPatient(p);
	    	//display.changeActiveSets();
	    }
	    else
	    {
	    	Log.d(TAG, "View is NULL!");
	    	display = (GraphViewGL) _view.findViewById(R.id.graphViewGL1);
	    }
	}

	public void updateGraph(String content, int position, ListView l){
		updateGraph(content, position);
		//Patient p = (Patient)l.getItemAtPosition(position);
		//display.setCurrentPatient(p);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		display.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		display.onResume();
	}
	
}
