package net.hklight.nanodegree.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Hashtable;


public class MainActivity extends ActionBarActivity implements ArtistSearchFragment.ArtistSelectedCallback {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // determine the pane mode
        if (findViewById(R.id.framelayout_detailFragmentContainer) == null) {
            // That is one pane mode
            mTwoPane = false;
        } else {
            mTwoPane = true;
        }

        // new intent...
        if (savedInstanceState == null) {
            ArtistSearchFragment artistSearchFragment = new ArtistSearchFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.framelayout_masterFragmentContainer, artistSearchFragment);
            ft.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    // Callback for fragment
    @Override
    public void onArtistSelected(Hashtable<String, String> selectedArtist) {
        Bundle data = new Bundle();
        data.putSerializable("selectedArtist", selectedArtist);

        if (mTwoPane) {
            // data as arguments
            // pass to fragment

            TopTenTracksFragment topTenTracksFragment = new TopTenTracksFragment();
            topTenTracksFragment.setArguments(data);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.framelayout_detailFragmentContainer, topTenTracksFragment);
            ft.commit();

        } else {
            // pass to activity
            Intent detailIntent = new Intent(this, TopTenTracksActivity.class);
            detailIntent.putExtras(data);
            startActivity(detailIntent);
        }
    }
}
