package com.swiftdino.medicalviewer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class DetailViewActivity extends FragmentActivity {
final String TAG = "DetailViewActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Setting Content View");
        setContentView(R.layout.detail_view_fragment);

        Log.d(TAG, "Getting Intent");
        Intent launchingIntent = getIntent();
        String content = launchingIntent.getData().toString();

        Log.d(TAG, "Content: "+content);
        Log.d(TAG, "Starting detail view fragment");
        DetailViewFragment detailView = (DetailViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_detailview);
        
    //    Bundle arguments = new Bundle();
      //  arguments.putString(DetailViewFragment.ARG_ITEM_ID,
        //        getIntent().getStringExtra(DetailViewFragment.ARG_ITEM_ID));
        //detailView.setArguments(arguments);
        
        Log.d(TAG, "Updating detail view fragment");
        detailView.updateGraph(content);
        
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, MasterListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
