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

import java.util.Hashtable;


public class MainActivity extends ActionBarActivity implements ArtistSearchFragment.ArtistSelectedCallback {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String MUSICPLAYER_FRAGMENT = "MUSICPLAYER_FRAG";

    private boolean mTwoPane = false;
    private Hashtable<String, String> selectedArtist;

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
        } else {
            selectedArtist = (Hashtable<String, String>) savedInstanceState.getSerializable("selectedArtist");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("selectedArtist", selectedArtist);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // check the notificifcation if it is checked
        boolean showNotificationOnLcok = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showNotificationOnLock", true);
        menu.findItem(R.id.action_showNotificationOnLock).setChecked(showNotificationOnLcok);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_nowPlaying) {
            // show now playing window

            if (mTwoPane) {
                // ths is in two pane mode
                FragmentManager fm = getSupportFragmentManager();
                MusicPlayerDialogFragment musicPlayerDialogFragment = new MusicPlayerDialogFragment();
                musicPlayerDialogFragment.show(fm, MUSICPLAYER_FRAGMENT);
            } else {
                // this is in one pane mode
                Intent musicPlayerActivity = new Intent(this, MusicPlayerActivity.class);
                startActivity(musicPlayerActivity);

            }
            return true;

        } else if (item.getItemId() == R.id.action_changeCountryCode) {
            // change country code

            // get curretn code
            String countryCode = PreferenceManager.getDefaultSharedPreferences(this).getString("countryCode", getString(R.string.default_country));

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setSingleLine(true);
            input.setText(countryCode);


            // show dialog
            new AlertDialog.Builder(this)
                    .setTitle(R.string.changeCountryCode)
                    .setMessage(R.string.inputCountryCode)
                    .setCancelable(false)
                    .setView(input)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            // save
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("countryCode", value.toString().toUpperCase().trim()).commit();

                            // notify reload if any...
                            if (mTwoPane && selectedArtist != null) {
                                onArtistSelected(selectedArtist);
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();

            return true;
        } else if (item.getItemId() == R.id.action_showNotificationOnLock) {
            boolean newValue = !item.isChecked();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("showNotificationOnLock", newValue).commit();
            // change checkbox
            item.setChecked(newValue);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Callback for fragment
    @Override
    public void onArtistSelected(Hashtable<String, String> selectedArtist) {
        // save , prepare for later recall...
        this.selectedArtist = selectedArtist;

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
