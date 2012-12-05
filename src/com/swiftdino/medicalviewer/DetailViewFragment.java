package com.swiftdino.medicalviewer;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.swiftdino.medicalviewer.dummy.DummyContent;

import android.graphics.PointF;
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
    
    private TextView[][] tVs;
    
    private static TextView fpsView;
    public static float fps = 0;
    public static PointF offset;
	public static PointF scale;
	public static CGraph[] graphs = null;
	public static int sHeight;
	public static int sWidth;
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

        }, 0, 50);

    }
    
    private void TimerMethod(){
    	myHandler.post(myRunnable);
    }
    
    final Runnable myRunnable = new Runnable() {
    	public void run() {
    		fpsView.setText("" + fAcc(fps,2));
    		updateTextViews();
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
    	
    	tVs = new TextView[2][14];
    	
    	for(int i=1; i<=NUMBER_OF_GRAPHS; i++){
	    	for(int x=0; x<11; x++){
	    		TextView tx=new TextView(getActivity());
	    		tVs[i-1][x] = tx;
	    		tx.setText("");
	        	tx.setY(GRAPH_OFFSET*i);
	        	tx.setX(x*80);
	        	layout.addView(tx);
	    	}
	    	for(int y=0; y<3; y++){
	    		TextView ty=new TextView(getActivity());
	    		tVs[i-1][11+y] = ty;
	    		ty.setText("");
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
	
	private void updateTextViews(){
		
		if(graphs != null){
			
			for(int i = 0; i < 2; i++){
				
				for(int j = 0; j < 11; j++){
					tVs[i][j].setX(j * scale.x - offset.x - 20.0f);
					tVs[i][j].setY(sHeight - (graphs[i].getMin() * scale.y - offset.y) + 10);
					tVs[i][j].setText(j + ".0s");
					if(tVs[i][j].getX() > ((float)sWidth)*.72f) tVs[i][j].setX(Float.MAX_VALUE);
				}
				
				float[] yVals = new float[] {graphs[i].getMin(),graphs[i].getZero(),graphs[i].getMax()};
				
				for(int j = 11; j < 14; j++){
					tVs[i][j].setX(-80.0f - offset.x);
					tVs[i][j].setY(sHeight - (yVals[j-11] * scale.y - offset.y) - 18);
					tVs[i][j].setText(decPrecision((yVals[j-11] - graphs[i].getZero())/graphs[i].getFactor(),2) + "");
					if(tVs[i][j].getX() > ((float)sWidth)*.72f) tVs[i][j].setX(Float.MAX_VALUE);
					else if(tVs[i][j].getX() < 0.0f) tVs[i][j].setX(0.0f);
				}
				
				
			}
		
		}
		
	}
	
	private float decPrecision(float f, int prec) {
		
		int temp = (int)(f * Math.pow(10, prec));
		return (float)(((float)(temp)) / Math.pow(10, prec));
	}
	
}
