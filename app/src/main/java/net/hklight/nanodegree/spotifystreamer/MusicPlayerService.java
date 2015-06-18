package net.hklight.nanodegree.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import java.io.IOException;

public class MusicPlayerService extends Service {
    // variables
    private MediaPlayer mMediaPlayer;
    private boolean isAudioLoaded = false;
    private Uri currentPlayingUri;

    public MusicPlayerService() {

    }

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }


    private final IMusicPlayerService.Stub mBinder = new IMusicPlayerService.Stub() {
        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public void play() throws RemoteException {
            mMediaPlayer.start();
        }

        @Override
        public void pause() throws RemoteException {
            mMediaPlayer.pause();
        }

        @Override
        public void stop() throws RemoteException {
            mMediaPlayer.stop();
        }

        @Override
        public int getDuration() throws RemoteException {
            return mMediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mMediaPlayer.getCurrentPosition();
        }

        @Override
        public void seekTo(int positionMs) throws RemoteException {
            mMediaPlayer.seekTo(positionMs);
        }

        @Override
        public void setDataSource(Uri dataSource, final boolean playAfterLoaded) {
            if (dataSource.equals(currentPlayingUri)) {
                // try to open the same audio.. ignore it...
                return;
            }
            try {
                isAudioLoaded = false;
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                }
                mMediaPlayer = null;
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(MusicPlayerService.this, dataSource);
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        isAudioLoaded = true;

                        if (playAfterLoaded) {
                            mMediaPlayer.start();
                        }
                    }
                });
                currentPlayingUri = dataSource;
                mMediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isAudioLoaded(){
            return isAudioLoaded;
        }
    };


}
