package com.swiftdino.medicalviewer;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class SearchableActivity extends ListActivity {
	private final String TAG = "SearchableActivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchable);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Log.d(TAG, "Loaded searchable.xml");
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		handleIntent(intent);

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpTo(this, new Intent(this,
					MasterListActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			// doMySearch(query);
			Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT)
					.show();

			Log.d(TAG, "Searching for " + query);
		}
	}

	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}
}
