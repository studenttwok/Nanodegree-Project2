package net.hklight.nanodegree.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerDialogFragment extends DialogFragment implements  View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final String LOG_TAG = MusicPlayerDialogFragment.class.getSimpleName();

    //private MediaPlayer mMediaPlayer;
    //private Hashtable<String, String> selectedTrack;

    // temp hold the data
    private Hashtable<String, String> artist = null;
    private ArrayList<Hashtable<String, String>> dataset = null;
    private int currentMusicPosition = -1;


    private SeekBar progressSeekbar;
    private TextView artistNameTextView;
    private TextView albumNameTextView;
    private TextView trackNameTextView;
    private TextView currentDurationTextView;
    private TextView totalDurationTextView;
    private ImageView albumImageView;
    private ImageButton previousImageButton;
    private ImageButton playImageButton;
    private ImageButton nextImageButton;
    private TextView loadingTextView;

    private IMusicPlayerService mIMusicPlayerService;
    private boolean mBound = false;
    private Timer timer;
    private TimerTask timerTask;
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent;

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                // update progress
                if (mIMusicPlayerService != null) {
                    try {

                        if (mIMusicPlayerService.isAudioLoaded()) {
                            playImageButton.setEnabled(true);
                            loadingTextView.setVisibility(View.INVISIBLE);
                            // get duration
                            totalDurationTextView.setText(convertMSToHMmSs(mIMusicPlayerService.getDuration()));
                            currentDurationTextView.setText(convertMSToHMmSs(mIMusicPlayerService.getCurrentPosition()));

                            int percentage = (int)Math.floor(((float)mIMusicPlayerService.getCurrentPosition() / (float)mIMusicPlayerService.getDuration()) * 100.0f);
                            progressSeekbar.setProgress(percentage);

                            // update the button for playing status
                            if(mIMusicPlayerService.isPlaying()) {
                                playImageButton.setImageResource(android.R.drawable.ic_media_pause);
                            } else {
                                playImageButton.setImageResource(android.R.drawable.ic_media_play);
                            }

                        } else {
                            playImageButton.setEnabled(false);
                            loadingTextView.setVisibility(View.VISIBLE);
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            } else if (msg.what == 2) {
                if (msg.arg1 == 1) {
                    // show pause  button
                    playImageButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    // show pause button
                    playImageButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            } else if (msg.what == 3) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.mediaPlayerError, Toast.LENGTH_SHORT).show();
                    loadingTextView.setText(R.string.error);
                }
            } else if (msg.what == 4){
                // no toast, error
                if (getActivity() != null) {
                    loadingTextView.setText(R.string.error);
                }
            }

        }
    };



    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mIMusicPlayerService = IMusicPlayerService.Stub.asInterface(service);
            // resume the status
            mBound = true;

            doWhenServiceBinded();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(LOG_TAG, "Service has unexpectedly disconnected");
            mIMusicPlayerService = null;
            mBound = false;
        }

    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    private void setPlayPauseButtonSrc(boolean displayPlay) {
        Message msg = mHandler.obtainMessage();
        msg.what = 2;
        if (displayPlay) {
            msg.arg1 = 1;
        } else {
            msg.arg1 = 0;
        }
        mHandler.sendMessage(msg);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentMusicPosition", currentMusicPosition);

        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_musicplayer, container, false);

        artistNameTextView = (TextView) rootView.findViewById(R.id.textview_artistName);
        albumNameTextView = (TextView) rootView.findViewById(R.id.textview_albumName);
        trackNameTextView = (TextView) rootView.findViewById(R.id.textview_trackName);
        currentDurationTextView = (TextView) rootView.findViewById(R.id.textview_currentDuration);
        totalDurationTextView = (TextView) rootView.findViewById(R.id.textview_totalDuration);
        albumImageView = (ImageView)rootView.findViewById(R.id.imageview_album);
        previousImageButton = (ImageButton)rootView.findViewById(R.id.imagebutton_previous);
        playImageButton = (ImageButton)rootView.findViewById(R.id.imagebutton_play);
        nextImageButton = (ImageButton)rootView.findViewById(R.id.imagebutton_next);
        progressSeekbar = (SeekBar)rootView.findViewById(R.id.seekbar_progress);
        loadingTextView = (TextView)rootView.findViewById(R.id.textview_loading);
        progressSeekbar.setOnSeekBarChangeListener(this);
        progressSeekbar.setMax(100);

        playImageButton.setOnClickListener(this);
        previousImageButton.setOnClickListener(this);
        nextImageButton.setOnClickListener(this);


        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().getSerializable("artist") != null && getArguments().getSerializable("dataset") != null) {
            //selectedTrack = (Hashtable<String, String>) getArguments().getSerializable("selectedTrack");
            artist = (Hashtable<String, String>) getArguments().getSerializable("artist");
            dataset = (ArrayList<Hashtable<String, String>>) getArguments().getSerializable("dataset");
            currentMusicPosition = getArguments().getInt("position");
        }

        /*
        currentMusicPosition = getArguments().getInt("position");
        if (savedInstanceState != null) {
            currentMusicPosition = savedInstanceState.getInt("currentMusicPosition");
        }
        */

        // start service
        Intent musicPlayerService = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().startService(musicPlayerService);

    }

    private void updateTrackViewInfo() {

        Hashtable<String, String> selectedTrack = dataset.get(currentMusicPosition);

        // update uri

        artistNameTextView.setText(artist.get("artistName"));
        trackNameTextView.setText(selectedTrack.get("trackName"));
        albumNameTextView.setText(selectedTrack.get("albumName"));
        //currentDurationTextView.setText(convertMSToHMmSs(0));
        //totalDurationTextView.setText(convertMSToHMmSs(0));
        currentDurationTextView.setText(R.string.initTime);
        totalDurationTextView.setText(R.string.initTime);


        if (selectedTrack.get("largeAlbumImage").length() > 0) {
            // load the art
            String albumUrl = selectedTrack.get("largeAlbumImage");
            Picasso.with(getActivity()).load(albumUrl).placeholder(R.drawable.loading).fit().into(albumImageView);
        } else {
            // clear
            Picasso.with(getActivity()).load(R.drawable.no_image).fit().into(albumImageView);
        }
    }

    private void doWhenServiceBinded() {
        // get the player status

        // debug
        // try to play the music
        try {

            // check if the new data pass to this fragment
            // so we use the data from service...
            if (artist == null || dataset == null) {
                Log.d(LOG_TAG, "Load form Service");
                // obtain from service
                dataset = (ArrayList<Hashtable<String, String>>) mIMusicPlayerService.getDataset();
                artist = (Hashtable<String, String>) mIMusicPlayerService.getArtist();
                currentMusicPosition = mIMusicPlayerService.getCurrentMusicPosition();

            } else {
                Log.d(LOG_TAG, "Data Sent to Service");
                // submit to service
                mIMusicPlayerService.setDataset(dataset, currentMusicPosition);
                mIMusicPlayerService.setArtist(artist);
            }

            // some song is loaded
            if (dataset != null) {
                // load the data if it is not yet loaded
                mIMusicPlayerService.setOnErrorListener(new IMusicPlayerListener.Stub() {
                    @Override
                    public void onError(int what, int extra) throws RemoteException {
                        Log.d(LOG_TAG, "onErrorListener in Fragment");
                        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                            mHandler.sendEmptyMessage(3);
                        } else {
                            mHandler.sendEmptyMessage(4);
                        }
                    }

                    @Override
                    public void onCompletion() throws RemoteException {

                    }

                });
                // save the current music position
                loadingTextView.setText(R.string.loading);

                String audioUrl = dataset.get(currentMusicPosition).get("previewUrl");
                Log.d(LOG_TAG, "audioUrl: " + audioUrl);
                //mIMusicPlayerService.setDataSource(Uri.parse(audioUrl), true);
                shareIntent = createShareIntent();
                if (mShareActionProvider != null && shareIntent != null) {
                    mShareActionProvider.setShareIntent(shareIntent);
                }
                mIMusicPlayerService.prepareTrack(0, true);


                //mIMusicPlayerService.play();

                // register timertask to update data
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        // invoke ui thread handler to update ui
                        mHandler.sendEmptyMessage(1);
                    }
                };

                timer.scheduleAtFixedRate(timerTask, 250, 250);

                if (mIMusicPlayerService.isPlaying()) {
                    setPlayPauseButtonSrc(false);
                } else {
                    setPlayPauseButtonSrc(true);
                }

                // update music view
                updateTrackViewInfo();

            } else {
                // no song is loaded
                //Toast.makeText(getActivity(), R.string.noSongIsLoaded, Toast.LENGTH_SHORT).show();
                loadingTextView.setText(R.string.noSongIsLoaded);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart()");
        super.onStart();

        timer = new Timer();

        // connect to service again
        Intent musicPlayerService = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().bindService(musicPlayerService, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "onStop()");
        super.onStop();

        // disconnect from the service
        getActivity().unbindService(mConnection);

        if (timerTask != null) {
            timerTask.cancel();
        }
        timer.cancel();
        timer = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    // obtain from internet
    public static String convertMSToHMmSs(long seconds) {
        seconds = seconds / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h,m,s);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        /*
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        */

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imagebutton_play) {
            if (dataset == null) {
                return;
            }
            doPlayPauseMusic();
        } else if (v.getId() == R.id.imagebutton_previous) {
            // back to first song
            if (dataset == null) {
                return;
            }
            if (currentMusicPosition > 0) {
                currentMusicPosition--;
                updateTrackViewInfo();
                try {

                    loadingTextView.setText(R.string.loading);

                    //String audioUrl = dataset.get(currentMusicPosition).get("previewUrl");

                    //mIMusicPlayerService.setDataSource(Uri.parse(audioUrl), mIMusicPlayerService.isPlaying());
                    shareIntent = createShareIntent();
                    mShareActionProvider.setShareIntent(shareIntent);
                    mIMusicPlayerService.prepareTrack(-1, mIMusicPlayerService.isPlaying());

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), R.string.noMorePreviousTrack, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.imagebutton_next) {
            // go to next song
            if (dataset == null) {
                return;
            }
            if (currentMusicPosition < dataset.size()-1) {
                currentMusicPosition++;
                updateTrackViewInfo();
                try {

                    //String audioUrl = dataset.get(currentMusicPosition).get("previewUrl");
                    loadingTextView.setText(R.string.loading);
                    //mIMusicPlayerService.setDataSource(Uri.parse(audioUrl), mIMusicPlayerService.isPlaying());
                    shareIntent = createShareIntent();
                    mShareActionProvider.setShareIntent(shareIntent);
                    mIMusicPlayerService.prepareTrack(+1, mIMusicPlayerService.isPlaying());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getActivity(), R.string.noMoreNextTrack, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doPlayPauseMusic() {
        if (mIMusicPlayerService != null) {

            try {
                // if audio is not loaded...
                if (!mIMusicPlayerService.isAudioLoaded()) {
                    return;
                }

                if (mIMusicPlayerService.isPlaying()) {
                    mIMusicPlayerService.pause();
                    setPlayPauseButtonSrc(true);
                } else {
                    mIMusicPlayerService.play();
                    setPlayPauseButtonSrc(false);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mIMusicPlayerService == null) {
            return;
        }

        try {
            if (mIMusicPlayerService.isAudioLoaded()) {

                if (fromUser) {
                    int progressInt = progressSeekbar.getProgress();
                    int positionMs = (int) Math.floor(mIMusicPlayerService.getDuration() * (progressInt / 100.0f));

                    mIMusicPlayerService.seekTo(positionMs);

                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        shareIntent = createShareIntent();

        inflater.inflate(R.menu.menu_fragment_musicplayerdialog, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            if (shareIntent != null) {
                mShareActionProvider.setShareIntent(shareIntent);
            }
        } else {
            Log.d(LOG_TAG, "Share Provider is null");
        }
    }

    private Intent createShareIntent() {
        String trackId = "";
        try {
            if (mIMusicPlayerService == null || mIMusicPlayerService.getDataset() == null) {
                return null;
            }
            Hashtable<String, String> selectedTrack = (Hashtable<String, String>) mIMusicPlayerService.getDataset().get(currentMusicPosition);
            trackId = selectedTrack.get("trackId");
            Log.d(LOG_TAG, trackId);
        } catch (RemoteException e) {
            e.printStackTrace();

        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://open.spotify.com/track/" + trackId);
        return shareIntent;

    }
}
