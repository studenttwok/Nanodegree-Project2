package net.hklight.nanodegree.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class MusicPlayerActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_musicplayer);

        setTitle(R.string.musicPlayer);

        if (savedInstanceState == null) {
            // create fragment

            MusicPlayerDialogFragment musicPlayerDialogFragment = new MusicPlayerDialogFragment();


            Bundle data = getIntent().getExtras();

            HashMap<String, String> artistMap = null;
            ArrayList<HashMap<String, String>> datasetMap = null;

            ArrayList<Hashtable<String, String>> dataset = null;
            Hashtable<String, String> artist = null;

            if (data != null && data.get("artist") != null && data.get("dataset") != null) {
                artistMap = (HashMap<String, String>) data.get("artist");
                datasetMap = (ArrayList<HashMap<String, String>>) data.get("dataset");

                dataset = new ArrayList<Hashtable<String, String>>();

                // convert the hashmap to hashtable
                for (int i = 0; i < datasetMap.size(); i++) {
                    Hashtable<String, String> eachEntry = new Hashtable<String, String>(datasetMap.get(i));
                    dataset.add(eachEntry);
                }
                artist = new Hashtable<String, String>(artistMap);

                data.putSerializable("dataset", dataset);
                data.putSerializable("artist", artist);

                musicPlayerDialogFragment.setArguments(data);
            }


            // add the fragment
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.framelayout_fragmentContainer, musicPlayerDialogFragment);
            ft.commit();

        }
    }
}
