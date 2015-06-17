package net.hklight.nanodegree.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class ArtistSearchFragment extends Fragment implements AdapterView.OnItemClickListener {

    private final String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    private ArrayList<Hashtable<String, String>> dataset = new ArrayList<Hashtable<String, String>>();

    // view
    private boolean isSearching = false;
    private ListView searchResultListView;
    // adapter
    private ArtistAdapter artistAdapter;
    private String searchViewKeyword = "";


    public ArtistSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Log.d(LOG_TAG, "onSaveInstanceState");
        // save the search result
        outState.putSerializable("dataset", dataset);
        outState.putString("searchViewKeyword", searchViewKeyword);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_artistsearch, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.hint_artistName));
        if (searchViewKeyword.length() > 0) {
            // retain the searchview state
            searchItem.expandActionView();
            searchView.setQuery(searchViewKeyword, false);
        }


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (!isSearching) {

                    String keyword = s.trim();

                    if (keyword.length() == 0) {
                        // Toast user..
                        Toast.makeText(getActivity(), R.string.toast_enterKeywordPlease, Toast.LENGTH_SHORT).show();
                    } else {
                        // start search
                        ArtistSearchAsyncTask asat = new ArtistSearchAsyncTask();
                        asat.execute(keyword);
                    }
                }
                // event consumed
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Give up UX, avoid too many network traffic while search while user type.
                searchViewKeyword = s;
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_artistsearch, container, false);

        // get the lsit view
        searchResultListView = (ListView) rootView.findViewById(R.id.listview_searchResult);
        searchResultListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        searchResultListView.setOnItemClickListener(this);

        //rootView.findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);

        // get the artist list back, so we don't need to query again if the screen rotate...
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "recover from rotate");
            dataset = (ArrayList<Hashtable<String, String>>) savedInstanceState.getSerializable("dataset");

            searchViewKeyword = savedInstanceState.getString("searchViewKeyword");

            // reconstruct the adapter
            artistAdapter = new ArtistAdapter(getActivity(), dataset);
            searchResultListView.setAdapter(artistAdapter);


        } else {
            Log.d(LOG_TAG, "new Intent");
            dataset = new ArrayList<Hashtable<String, String>>();
        }

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        Hashtable<String, String> selectedArtist = artistAdapter.getItem(position);

        // user select the artist
        // then pass to host activity to decide the display pathway, whether by invoke activity, or pass to new fragment
        ((ArtistSelectedCallback)(getActivity())).onArtistSelected(selectedArtist);

    }

    public class ArtistSearchAsyncTask extends AsyncTask<String,Void,List<Artist>> {

        @Override
        protected void onPreExecute() {
            isSearching = true;
        }

        @Override
        protected List<Artist> doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }

            // incoke the spotify api
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();


            try {
                ArtistsPager ap = spotify.searchArtists(params[0]);
                return ap.artists.items;

            } catch (RetrofitError e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            isSearching = false;
            if (artists == null) {
                // Error occure
                Toast.makeText(getActivity(), R.string.errorOccurs, Toast.LENGTH_SHORT).show();
                return;
            }

            if (artists.size() == 0) {
                // let the user know
                Toast.makeText(getActivity(), R.string.artistNotFound, Toast.LENGTH_LONG).show();
            }

            // convert it into hashtable
            dataset.clear();

            for (Artist artist : artists) {
                Hashtable<String, String> eachArtist = new Hashtable<String, String>();
                eachArtist.put("artistName", artist.name);
                String artistImage = "";
                if (artist.images.size() > 0) {
                    artistImage = artist.images.get(0).url;
                }
                eachArtist.put("artistImage", artistImage);
                eachArtist.put("artistId", artist.id);

                dataset.add(eachArtist);
            }

            artistAdapter = new ArtistAdapter(getActivity(), dataset);
            searchResultListView.setAdapter(artistAdapter);
        }
    }


    public interface ArtistSelectedCallback {
        public void onArtistSelected(Hashtable<String, String> selectedArtist);
    }

}
