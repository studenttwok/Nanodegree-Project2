package net.hklight.nanodegree.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class TopTenTracksFragment extends Fragment implements AdapterView.OnItemClickListener {

    private final String LOG_TAG = TopTenTracksFragment.class.getSimpleName();
    private final String MUSICPLAYER_FRAGMENT = "MUSICPLAYER_FRAG";

    private ListView topTenTracksListView;
    private ArrayList<Hashtable<String, String>> dataset = new ArrayList<Hashtable<String, String>>();
    private Hashtable<String, String> artist;
    private String artistName = "";
    private TrackAdapter trackAdapter;

    private Bundle selectedData = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create view for fragment
        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        topTenTracksListView = (ListView) rootView.findViewById(R.id.listview_toptentracks);
        topTenTracksListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        topTenTracksListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // read the arguments

        // init it
        dataset = new ArrayList<Hashtable<String, String>>();

        // get the artist list back, so we don't need to query again if the screen rotate...
        if (savedInstanceState != null && savedInstanceState.getSerializable("dataset") != null) {
            // triggle by activity destroy and recreate
            dataset = (ArrayList<Hashtable<String, String>>)savedInstanceState.getSerializable("dataset");

            artist = (Hashtable<String, String>)savedInstanceState.getSerializable("artist");
            artistName = savedInstanceState.getString("artistName");

            // recreate the listview adapter
            trackAdapter = new TrackAdapter(getActivity(), dataset);
            topTenTracksListView.setAdapter(trackAdapter);

        } else {
            // from previous activity intent

            artist = (Hashtable<String, String>) getArguments().getSerializable("selectedArtist");
            artistName = getArguments().getString("artistName");

            // start async task to get the top 10 track
            TopTenTracksAsyncTask topTenTracksAsyncTask = new TopTenTracksAsyncTask();
            topTenTracksAsyncTask.execute(artist.get("artistId"));

        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        //super.onSaveInstanceState(outState);
        // save the search result
        // avoid research
        outState.putSerializable("dataset", dataset);
        outState.putSerializable("artist", artist);
        outState.putSerializable("artistName", artistName);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // pass to music player
        // if it is in two pane mode, open dialog
        // elase open activity
        //Hashtable<String, String> selectedTrack = dataset.get(position);
        selectedData = new Bundle();
        //selectedData.putSerializable("selectedTrack", selectedTrack);
        selectedData.putSerializable("artist", artist);
        selectedData.putSerializable("dataset", dataset);
        selectedData.putSerializable("position", position);

        // save the current position


        Log.d(LOG_TAG, "Selected Data: " + selectedData);

        // check if we want to use activity or dialog
        if (getActivity() instanceof MainActivity) {
            // ths is in two pane mode
            FragmentManager fm = getActivity().getSupportFragmentManager();
            MusicPlayerDialogFragment musicPlayerDialogFragment = new MusicPlayerDialogFragment();
            musicPlayerDialogFragment.setArguments(selectedData);
            musicPlayerDialogFragment.show(fm, MUSICPLAYER_FRAGMENT);
        } else {
            // this is in one pane mode
            Intent musicPlayerActivity = new Intent(getActivity(), MusicPlayerActivity.class);
            musicPlayerActivity.putExtras(selectedData);
            startActivity(musicPlayerActivity);

        }


    }


    public class TopTenTracksAsyncTask extends AsyncTask<String,Void, List<Track>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<Track> doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }

            // incoke the spotify api
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Hashtable<String, Object> spotifyParams = new Hashtable<String, Object>();
            spotifyParams.put("country", "HK");

            try {
                Tracks tracks = spotify.getArtistTopTrack(params[0], spotifyParams);
                return tracks.tracks;
            } catch (RetrofitError e) {
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(List<Track> tracks) {

            if (tracks == null) {
                // Error occure
                Toast.makeText(getActivity(), R.string.errorOccurs, Toast.LENGTH_SHORT).show();
                return;
            }

            // convert it into Hashtable
            dataset.clear();

            if (tracks.size() == 0) {
                // let the user know
                Toast.makeText(getActivity(), R.string.trackNotFound, Toast.LENGTH_LONG).show();
            }


            for (Track track : tracks) {
                String trackId = track.id;
                String trackName = track.name;
                String albumName = track.album.name;
                String smallAlbumImage = "";
                String largeAlbumImage = "";

                long durationMs = track.duration_ms;

                if (track.album.images.size() > 0) {
                    for (int i=0; i < track.album.images.size(); i++) {
                        Image image = track.album.images.get(i);

                        if (image.width == 640) {
                            // This is large
                            largeAlbumImage = image.url;
                        } else if (image.width == 200) {
                            smallAlbumImage = image.url;
                        }
                    }

                    // if image is not in 640 or 200, pick first one...
                    smallAlbumImage = track.album.images.get(0).url;
                    largeAlbumImage = track.album.images.get(0).url;
                }

                // and then save them
                Hashtable<String, String> eachTrack = new Hashtable<String, String>();
                eachTrack.put("trackId", trackId);
                eachTrack.put("trackName", trackName);
                eachTrack.put("albumName", albumName);
                eachTrack.put("smallAlbumImage", smallAlbumImage);
                eachTrack.put("largeAlbumImage", largeAlbumImage);
                eachTrack.put("previewUrl", track.preview_url);
                eachTrack.put("durationMs", durationMs + "");


                dataset.add(eachTrack);
            }

            trackAdapter = new TrackAdapter(getActivity(), dataset);
            topTenTracksListView.setAdapter(trackAdapter);
        }
    }
}
