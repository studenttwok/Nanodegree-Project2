package net.hklight.nanodegree.spotifystreamer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Hashtable;

public class MusicPlayerDialogFragment extends DialogFragment implements MediaPlayer.OnPreparedListener {

    private MediaPlayer mMediaPlayer;
    private Hashtable<String, String> selectedTrack;
    private Hashtable<String, String> artist;

    private TextView artistNameTextView;
    private TextView albumNameTextView;
    private TextView trackNameTextView;
    private TextView currentDurationTextView;
    private TextView totalDurationTextView;
    private ImageView albumImageView;
    private Button previousButton;
    private Button playButton;
    private Button nextButton;

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
        previousButton = (Button)rootView.findViewById(R.id.button_previous);
        playButton = (Button)rootView.findViewById(R.id.button_play);
        nextButton = (Button)rootView.findViewById(R.id.button_next);


        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedTrack = (Hashtable<String, String>) getArguments().getSerializable("selectedTrack");
        artist = (Hashtable<String, String>) getArguments().getSerializable("artist");

        String totalDurationStr = convertMSToHMmSs(Long.parseLong(selectedTrack.get("durationMs")));

        artistNameTextView.setText(artist.get("artistName"));
        trackNameTextView.setText(selectedTrack.get("trackName"));
        albumNameTextView.setText(selectedTrack.get("albumName"));
        currentDurationTextView.setText(convertMSToHMmSs(0));
        totalDurationTextView.setText(totalDurationStr);


        if (selectedTrack.get("largeAlbumImage").length() > 0) {
            // load the art
            String albumUrl = selectedTrack.get("largeAlbumImage");
            Picasso.with(getActivity()).load(albumUrl).placeholder(R.drawable.loading).fit().into(albumImageView);
        } else {
            // clear
            Picasso.with(getActivity()).load(R.drawable.no_image).fit().into(albumImageView);
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(getActivity(), Uri.parse(selectedTrack.get("previewUrl")));
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }


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
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;

    }
}
