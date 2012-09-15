package com.swiftdino.medicalviewer;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class MasterListActivity extends FragmentActivity implements MasterListFragment.OnListItemSelectedListener {

	final String TAG = "MasterListActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_list_fragment);

        //info display container in activity_info_display.xml
      /*  if (findViewById(R.id.info_display_container) != null) {
            mTwoPane = true;
            ((MasterListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }*/
        
        
    }


public void onListItemSelected(String id) {
		
		// Load the Fragment in an activity if its not present,
		// otherwise just update the fragment.
	Log.d(TAG, "Loading the detail view fragment.");
	
      
	DetailViewFragment detailView = (DetailViewFragment) getSupportFragmentManager()
			.findFragmentById(R.id.fragment_detailview);
		 
		if (detailView == null || !detailView.isInLayout()) {
			 Log.d(TAG, "DetailViewFragment is null. Starting new DetailViewActivity");


			Intent detailIntent = new Intent(this, DetailViewActivity.class);
			detailIntent.setData(Uri.parse(id)); 
			startActivity(detailIntent); 
			Log.d(TAG, "DetailViewActivity has been started.");

		} else {
			//update fragment
			//right now im just starting a new intent
			 Log.d(TAG, "DetailViewFragment exists. Updating graph.");
		    	
			detailView.updateGraph(id);
		}
	}




}
