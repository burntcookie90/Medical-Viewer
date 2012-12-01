package com.swiftdino.medicalviewer;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.SearchView;
import android.widget.TextView;

public class MasterListActivity extends FragmentActivity {// implements
	// MasterListFragment.OnListItemSelectedListener {

	final String TAG = "MasterListActivity";

	private SearchView mSearchView;

	private static TextView fpsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.master_list_fragment);
		fpsView = (TextView) findViewById(R.id.fpsView);

		// info display container in activity_info_display.xml
		/*
		 * if (findViewById(R.id.info_display_container) != null) { mTwoPane =
		 * true; ((MasterListFragment) getSupportFragmentManager()
		 * .findFragmentById(R.id.item_list)) .setActivateOnItemClick(true); }
		 */

	}

	public static void setFps(int fps) {
		fpsView.setText("" + fps);
	}

	/*
	 * public boolean onCreateOptionsMenu(Menu menu) { MenuInflater inflater =
	 * getMenuInflater(); inflater.inflate(R.menu.master_list_menu, menu);
	 * 
	 * Log.d(TAG, "1"); // SearchManager searchManager = (SearchManager) //
	 * getSystemService(Context.SEARCH_SERVICE); // SearchView searchView =
	 * (SearchView) menu.findItem( // R.id.patient_list_search).getActionView();
	 * // searchView.setSearchableInfo(searchManager //
	 * .getSearchableInfo(getComponentName())); //
	 * searchView.setIconifiedByDefault(true); // Do not iconify the widget; //
	 * expand it by default
	 * 
	 * return true; }
	 */

	// public void onListItemSelected(String id) {
	//
	// // Load the Fragment in an activity if its not present,
	// // otherwise just update the fragment.
	// Log.d(TAG, "Loading the detail view fragment.");
	//
	// DetailViewFragment detailView = (DetailViewFragment)
	// getSupportFragmentManager()
	// .findFragmentById(R.id.fragment_detailview);
	//
	// if (detailView == null || !detailView.isInLayout()) {
	// Log.d(TAG,
	// "DetailViewFragment is null. Starting new DetailViewActivity");
	//
	// Intent detailIntent = new Intent(this, DetailViewActivity.class);
	// detailIntent.setData(Uri.parse(id));
	// startActivity(detailIntent);
	// Log.d(TAG, "DetailViewActivity has been started.");
	//
	// } else {
	// // update fragment
	// // right now im just starting a new intent
	// Log.d(TAG, "DetailViewFragment exists. Updating graph.");
	//
	// detailView.updateGraph(id);
	// }
	// }

}
