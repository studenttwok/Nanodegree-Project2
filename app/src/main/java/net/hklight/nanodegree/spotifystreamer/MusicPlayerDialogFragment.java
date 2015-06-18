package net.hklight.nanodegree.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
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

public class MusicPlayerDialogFragment extends DialogFragment implements MediaPlayer.OnPreparedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final String LOG_TAG = MusicPlayerDialogFragment.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private Hashtable<String, String> selectedTrack;
    private Hashtable<String, String> artist;
    private ArrayList<Hashtable<String, String>> dataset = new ArrayList<Hashtable<String, String>>();
    private int currentMusicPosition = 0;
    private Uri previewUri;

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

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // invoke ui thread handler to update ui
            mHandler.sendEmptyMessage(1);
        }
    };

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

        timer = new Timer();


        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //selectedTrack = (Hashtable<String, String>) getArguments().getSerializable("selectedTrack");
        artist = (Hashtable<String, String>) getArguments().getSerializable("artist");

        dataset = (ArrayList<Hashtable<String, String>>) getArguments().getSerializable("dataset");
        currentMusicPosition = getArguments().getInt("position");

        if (savedInstanceState != null) {
            currentMusicPosition = savedInstanceState.getInt("currentMusicPosition");
        }


        selectedTrack = dataset.get(currentMusicPosition);



        // update music view
        updateTrackViewInfo();

        // start service
        Intent musicPlayerService = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().startService(musicPlayerService);

    }

    private void updateTrackViewInfo() {
        // update uri
        previewUri = Uri.parse(selectedTrack.get("previewUrl"));

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
            // load the data if it is not yet loaded
            mIMusicPlayerService.setDataSource(previewUri, true);
            //mIMusicPlayerService.play();

            // register timertask to update data
            timer.scheduleAtFixedRate(timerTask, 250, 250);

            if (mIMusicPlayerService.isPlaying()) {
                setPlayPauseButtonSrc(false);
            } else {
                setPlayPauseButtonSrc(true);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        // connect to service again
        Intent musicPlayerService = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().bindService(musicPlayerService, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();

        // disconnect from the service
        getActivity().unbindService(mConnection);
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
    public void onPrepared(MediaPlayer mp) {
        // if the file is prepared
        mp.start();
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
            doPlayPauseMusic();
        } else if (v.getId() == R.id.imagebutton_previous) {
            // back to first song
            if (currentMusicPosition > 0) {
                currentMusicPosition--;
                selectedTrack = dataset.get(currentMusicPosition);
                updateTrackViewInfo();
                try {
                    mIMusicPlayerService.setDataSource(previewUri, mIMusicPlayerService.isPlaying());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), R.string.noMorePreviousTrack, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.imagebutton_next) {
            // go to next song
            if (currentMusicPosition < dataset.size()-1) {
                currentMusicPosition++;
                selectedTrack = dataset.get(currentMusicPosition);
                updateTrackViewInfo();
                try {
                    mIMusicPlayerService.setDataSource(previewUri, mIMusicPlayerService.isPlaying());
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
}
