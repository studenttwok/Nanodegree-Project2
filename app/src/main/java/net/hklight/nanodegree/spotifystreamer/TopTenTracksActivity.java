package net.hklight.nanodegree.spotifystreamer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Hashtable;


public class TopTenTracksActivity extends ActionBarActivity {
    private final String LOG_TAG = TopTenTracksActivity.class.getSimpleName();
    private final String FRAGMENT_TAG = "FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);

        //  new intent
        if (savedInstanceState == null) {

            String artistName = "";
            Hashtable<String, String> artist = new Hashtable<String, String>();

            // from previous activity intent
            // things from serializable is changed to Hashmap...

            artist.putAll((HashMap<String, String>) getIntent().getExtras().getSerializable("selectedArtist"));

            artistName = artist.get("artistName");

            // get arguments
            Bundle data = new Bundle();
            data.putSerializable("selectedArtist", artist);
            data.putString("artistName", artistName);


            TopTenTracksFragment topTenTracksFragment = new TopTenTracksFragment();
            topTenTracksFragment.setArguments(data);

            // pass to fragment
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.framelayout_fragmentContainer, topTenTracksFragment);
            ft.commit();

            // set the title
            getSupportActionBar().setTitle(R.string.title_activity_top_ten_tracks);
            getSupportActionBar().setSubtitle(artistName);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_toptentracks, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_nowPlaying) {
            // show now playing window
            // this is in one pane mode
            Intent musicPlayerActivity = new Intent(this, MusicPlayerActivity.class);
            startActivity(musicPlayerActivity);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

}
