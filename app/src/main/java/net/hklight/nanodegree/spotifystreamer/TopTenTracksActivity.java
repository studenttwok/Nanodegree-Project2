package net.hklight.nanodegree.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


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
        getMenuInflater().inflate(R.menu.menu_top_ten_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
