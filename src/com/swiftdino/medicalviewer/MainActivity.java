package com.swiftdino.medicalviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements ItemListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        //info display container in activity_info_display.xml
        if (findViewById(R.id.info_display_container) != null) {
            mTwoPane = true;
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }
        
        
    }

    public void onItemSelected(String id) {
        
    	if (mTwoPane) {
        	
            //Bundle arguments = new Bundle();
            //arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            InfoDisplayFragment fragment = new InfoDisplayFragment();
            //fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.info_display_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, InfoDisplayActivity.class);
            //detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
        
    }
    
}
