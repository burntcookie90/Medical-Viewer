package com.swiftdino.medicalviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class InfoDisplayActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_info_display);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
//            Bundle arguments = new Bundle();
//            arguments.putString(InfoDisplayFragment.ARG_ITEM_ID,
//                    getIntent().getStringExtra(InfoDisplayFragment.ARG_ITEM_ID));
//            InfoDisplayFragment fragment = new InfoDisplayFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.info_display_container, fragment)
//                    .commit();
        }
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, InfoDisplayActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
}
