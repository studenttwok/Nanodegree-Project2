package net.hklight.nanodegree.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class MusicPlayerService extends Service {

    private final static String LOG_TAG = MusicPlayerService.class.getSimpleName();
    private int ONGOING_NOTIFICATION_ID = 100;
    private Notification notification;

    // variables
    private MediaPlayer mMediaPlayer;
    private boolean isAudioLoaded = false;
    private Uri currentPlayingUri;
    private IMusicPlayerListener mMusicPlayerListener;

    public MusicPlayerService() {

    }

    @Override
    public void onCreate() {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getText(R.string.app_name))
                        .setContentText(getText(R.string.playing));
        notification = mBuilder.build();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        startForeground(ONGOING_NOTIFICATION_ID, notification);



        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind()");
        Log.d(LOG_TAG, "mMediaPlayer is playing: " + mMediaPlayer.isPlaying());
        if (!mMediaPlayer.isPlaying()) {
            Log.d(LOG_TAG, "mMediaPlayer is not Playing");
            // can stop forground
            // can stop self
            stopForeground(true);
            stopSelf();
        }
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind()");
        super.onRebind(intent);

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

                // set the callback
                if (mMusicPlayerListener != null) {
                    mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            Log.d(LOG_TAG, "setOnErrorListener");
                            try {
                                mMusicPlayerListener.onErrorListener(what, extra);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                }

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

        @Override
        public void setOnErrorListener(final IMusicPlayerListener musicPlayerListener) throws RemoteException {
            mMusicPlayerListener = musicPlayerListener;

        }
    };


}
