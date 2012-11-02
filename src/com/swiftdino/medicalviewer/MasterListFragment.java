package com.swiftdino.medicalviewer;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class MasterListFragment extends ListFragment {

	private static final String TAG = "MasterListFragment";
	// private OnListItemSelectedListener listItemSelectedListener;
	private StringLoader loader = null;
	private StringAdapter adapter = null;
	private List<Patient> patientData = new ArrayList<Patient>(); // patient
																	// data
	private List<Patient> listData = new ArrayList<Patient>(); // list data.
																// updates
																// depending on
																// search
	private String query;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setRetainInstance(true);
		
		View view = super.onCreateView(inflater, container, savedInstanceState);
		// listItemSelectedListener = (OnListItemSelectedListener)
		// getActivity();
		createListData();
		
		loader = new StringLoader(getActivity(), this);
		adapter = new StringAdapter(listData);
		setListAdapter(adapter);

		getLoaderManager().initLoader(0, null, new LoaderCallBacks());
		loader.forceLoad();
		setHasOptionsMenu(true);
		return view;
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		
		// Load the Fragment in an activity if its not present,
		// otherwise just update the fragment.
		Log.d(TAG, "Loading the detail view fragment.");

		DetailViewFragment detailView = (DetailViewFragment) this.getActivity()
				.getSupportFragmentManager()
				.findFragmentById(R.id.fragment_detailview);

		if (detailView == null || !detailView.isInLayout()) {
			Log.d(TAG,
					"DetailViewFragment is null. Starting new DetailViewActivity");

			Intent detailIntent = new Intent(this.getActivity(),
					DetailViewActivity.class);
			detailIntent.setData(Uri.parse("" + id));
			this.getActivity().startActivity(detailIntent);
			Log.d(TAG, "DetailViewActivity has been started.");

		} else {
			// update fragment
			// right now im just starting a new intent
			Log.d(TAG, "DetailViewFragment exists. Updating graph.");
			
			openQueryDialog((Patient)l.getItemAtPosition(position));
			detailView.updateGraph("" + id, position, l);
			
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.master_list_menu, menu);
		System.out.println("inflating menu");

		final SearchView searchView = (SearchView) menu.findItem(
				R.id.patient_list_search).getActionView();
		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

			public boolean onQueryTextChange(String newText) {
				showFilteredItems(newText);
				return true;
			}

			public boolean onQueryTextSubmit(String query) {
				return true;
			}
		};

		searchView.setOnQueryTextListener(queryTextListener);

		return;
	}// met

	/*
	 * ############ FILTERING ITEMS IN SEARCH [start]############# also includes
	 * code to add items
	 */
	private void showFilteredItems(String query) {
		this.query = query;
		loader.onContentChanged();
	}

	private void createListData() {
		for (int i = 0; i < 100; i++) {
			listData.add(new Patient("Patient " + i));
			patientData.add(new Patient("Patient " + i));
		}
	}

	public List<Patient> getData() {
		List<Patient> listFilteredData = new ArrayList<Patient>();
		for (Patient string : patientData) {
			// Log.d(TAG, string);
			if (query == null || string.getName().contains(query)) {
				listFilteredData.add(string);
			}
		}
		return listFilteredData;
	}// met

	private void openQueryDialog(final Patient p){
		
		float baseTime = 0;
		
		final Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(R.layout.patient_query_dialog);
		dialog.setTitle(p.getName());
		
		final TextView tv = (TextView) dialog.findViewById(R.id.query_text_view);
		final Spinner spin = (Spinner) dialog.findViewById(R.id.data_type_spinner);
		final SeekBar bar = (SeekBar) dialog.findViewById(R.id.timeframe_seekbar);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.algorithms, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(adapter);
		
		tv.setText(spin.getSelectedItem() + " at Time: " + bar.getProgress());
		
		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				tv.setText(spin.getSelectedItem() + " at Time: " + bar.getProgress());
			}
			public void onStartTrackingTouch(SeekBar seekBar) {	
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View v,
					int pos, long id) {
				String item = (String) parent.getItemAtPosition(pos);
				tv.setText(item + " at Time: " + bar.getProgress());
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		Button cancelButton = (Button) dialog.findViewById(R.id.button_query_cancel);
		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		Button continueButton = (Button) dialog.findViewById(R.id.button_query_continue);
		continueButton.setText("Continue");
		continueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
		
	}
	
	private class LoaderCallBacks implements LoaderCallbacks<List<Patient>> {

		public void onLoadFinished(Loader<List<Patient>> loader,
				List<Patient> listData) {
			adapter.setListData(listData);
		}// met

		public void onLoaderReset(Loader<List<Patient>> listData) {
			adapter.setListData(new ArrayList<Patient>());
		}// met

		public Loader<List<Patient>> onCreateLoader(int arg0, Bundle arg1) {
			return loader;
		}// met
	}// class

	private class StringAdapter extends ArrayAdapter<Patient> {

		private List<Patient> listDataToDisplay = new ArrayList<Patient>();
		private LayoutInflater mInflater;

		public StringAdapter(List<Patient> listData) {
			super(getActivity(), android.R.layout.simple_list_item_1,
					android.R.id.text1, listData);
			listDataToDisplay = listData;
			mInflater = (LayoutInflater) getActivity().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}// cons

		private void setListData(List<Patient> newListData) {
			this.listDataToDisplay.clear();
			this.listDataToDisplay.addAll(newListData);
			notifyDataSetChanged();
		}// met

		/**
		 * Populate new items in the list.
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;

			if (convertView == null) {
				view = mInflater.inflate(android.R.layout.simple_list_item_1,
						parent, false);
			} else {
				view = convertView;
			}

			((TextView) view.findViewById(android.R.id.text1))
					.setText(listDataToDisplay.get(position).getName());

			return view;
		}
	}// inner class
}// class

class StringLoader extends AsyncTaskLoader<List<Patient>> {

	MasterListFragment fragment = null;

	public StringLoader(Context context, MasterListFragment fragment) {
		super(context);
		this.fragment = fragment;
	}// cons

	@Override
	public List<Patient> loadInBackground() {
		return fragment.getData();
	}// met
}// class

/*
 * ############ FILTERING ITEMS IN SEARCH [finish]#############
 */

/*
 * @SuppressWarnings("unchecked")
 * 
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * 
 * setListAdapter(ArrayAdapter.createFromResource(getActivity()
 * .getApplicationContext(), R.array.strip_dates, R.layout.master_list_item));
 * 
 * setHasOptionsMenu(true); listItemSelectedListener =
 * (OnListItemSelectedListener) getActivity();
 * 
 * }
 * 
 * public void onListItemClick(ListView l, View v, int position, long id) {
 * final String[] links = getResources() .getStringArray(R.array.strip_refs);
 * String strip = links[position];
 * listItemSelectedListener.onListItemSelected(strip); }
 * 
 * public interface OnListItemSelectedListener { public void
 * onListItemSelected(String comicID); }
 * 
 * }
 */