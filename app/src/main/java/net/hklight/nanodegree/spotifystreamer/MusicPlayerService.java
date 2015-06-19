package net.hklight.nanodegree.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MusicPlayerService extends Service {

    private final static String LOG_TAG = MusicPlayerService.class.getSimpleName();
    private int ONGOING_NOTIFICATION_ID = 100;
    private Notification notification;
    //private RemoteViews notificationView;
    private NotificationManager mNotificationManager;

    // variables
    private MediaPlayer mMediaPlayer;
    private boolean isAudioLoaded = false;
    private boolean isAudioPreparing = false;
    private Uri currentPlayingUri;
    private IMusicPlayerListener mMusicPlayerListener;

    // Play list data store here
    private Hashtable<String, String> artist = null;
    private ArrayList<Hashtable<String, String>> dataset = null;
    private int currentMusicPosition = -1;

    // for temperaory store
    private String currentTrackName = "";
    private String currentArtistName = "";
    private Bitmap largeBitmap = null;



    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                // create notification
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notification = createNotification(android.R.drawable.ic_media_play, currentTrackName, currentArtistName);
                mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("net.hklight.nanodegree.spotifystremer.ACTION_PLAYPAUSE")) {
                // pause music
                try {
                    // check if the music is playing
                    if (mBinder.isAudioLoaded()) {
                        if (mBinder.isPlaying()) {
                            mBinder.pause();
                        } else {
                            mBinder.play();
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("net.hklight.nanodegree.spotifystremer.ACTION_PREVIOUS")) {
                try {
                    // check if the music is playing
                    mBinder.prepareTrack(-1, mBinder.isPlaying());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("net.hklight.nanodegree.spotifystremer.ACTION_NEXT")) {
                try {
                    // check if the music is playing
                    mBinder.prepareTrack(+1, mBinder.isPlaying());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public MusicPlayerService() {

    }

    @Override
    public void onCreate() {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // create broad receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("net.hklight.nanodegree.spotifystremer.ACTION_PLAYPAUSE");
        intentFilter.addAction("net.hklight.nanodegree.spotifystremer.ACTION_PREVIOUS");
        intentFilter.addAction("net.hklight.nanodegree.spotifystremer.ACTION_NEXT");

        registerReceiver(mBroadcastReceiver, intentFilter);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        //notificationView = new RemoteViews(getPackageName(), R.layout.remoteview_musicplayer);
        notification = createNotification(android.R.drawable.ic_media_play, currentTrackName, currentArtistName);


        startForeground(ONGOING_NOTIFICATION_ID, notification);


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    public Notification createNotification(int playPauseDrawable, String title, String content) {

        if (title.length() == 0) {
            title = getString(R.string.app_name);
        }

        if (content.length() == 0) {
            content = getString(R.string.musicPlayer);
        }

        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Listeners
        Intent pauseIntent = new Intent("net.hklight.nanodegree.spotifystremer.ACTION_PLAYPAUSE");
        PendingIntent pendingPauseIntent = PendingIntent.getBroadcast(this, 100, pauseIntent, 0);
        //notificationView.setOnClickPendingIntent(R.id.button_play, pendingPauseIntent);

        Intent previousIntent = new Intent("net.hklight.nanodegree.spotifystremer.ACTION_PREVIOUS");
        PendingIntent pendingPreviousIntent = PendingIntent.getBroadcast(this, 100, previousIntent, 0);
        //notificationView.setOnClickPendingIntent(R.id.button_previous, pendingPreviousIntent);

        Intent nextIntent = new Intent("net.hklight.nanodegree.spotifystremer.ACTION_NEXT");
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 100, nextIntent, 0);
        //notificationView.setOnClickPendingIntent(R.id.button_next, pendingNextIntent);




        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)

                //.addAction(android.R.drawable.ic_media_previous, getString(R.string.previous), pendingPreviousIntent) // #0
                //.addAction(android.R.drawable.ic_media_play, getString(R.string.play), pendingPauseIntent)  // #1
                //.addAction(android.R.drawable.ic_media_next, getString(R.string.next), pendingNextIntent)   // #2
                //.setContent(notificationView);
                .addAction(android.R.drawable.ic_media_previous, "", pendingPreviousIntent) // #0
                .addAction(playPauseDrawable, "", pendingPauseIntent)  // #1
                .addAction(android.R.drawable.ic_media_next, "", pendingNextIntent)   // #2
                //.setStyle(new NotificationCompat.MediaStyle().setMediaSession(mMediaPlayer.getAudioSessionId()))
                //.setShowActionsInCompactView(1);
                //.setContentTitle("Wonderful music")
                //.setContentText("My Awesome Band")
                //.setLargeIcon(albumArtBitmap)
                //.build();
                ;

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showNotificationOnLock", true)) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        } else {
            mBuilder.setVisibility(Notification.VISIBILITY_SECRET);
        }

        if (largeBitmap != null) {
            mBuilder.setLargeIcon(largeBitmap);
        }

        Notification notification = mBuilder.build();
        return notification;
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
        if (!mMediaPlayer.isPlaying() && !isAudioPreparing) {
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
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        unregisterReceiver(mBroadcastReceiver);
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
            notification = createNotification(android.R.drawable.ic_media_pause, currentTrackName, currentArtistName);
            //notificationView.setImageViewResource(R.id.button_play, android.R.drawable.ic_media_pause);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
            mMediaPlayer.start();
        }

        @Override
        public void pause() throws RemoteException {
            notification = createNotification(android.R.drawable.ic_media_play, currentTrackName, currentArtistName);
            //notificationView.setImageViewResource(R.id.button_play, android.R.drawable.ic_media_play);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
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
        public void prepareTrack(int musicPositionOffset, final boolean playAfterLoaded) throws RemoteException {

            // no error checking on currentMusicPosition
            if (dataset == null || (currentMusicPosition + musicPositionOffset)  < 0 || (currentMusicPosition + musicPositionOffset) > dataset.size() - 1 ) {
                // no more move...
                return;
            }


            // get the audio
            String audioUrl = dataset.get(currentMusicPosition + musicPositionOffset).get("previewUrl");
            Uri audioUri = Uri.parse(audioUrl);

            if (audioUri.equals(currentPlayingUri)) {
                // try to open the same audio.. ignore it...
                return;
            }

            // ok , so things get real, increase
            currentMusicPosition  += musicPositionOffset;

            try {
                isAudioLoaded = false;
                isAudioPreparing = true;
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                }
                mMediaPlayer = null;
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(MusicPlayerService.this, audioUri);

                // set the callback
                if (mMusicPlayerListener != null) {
                    mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            Log.d(LOG_TAG, "setOnErrorListener");
                            isAudioPreparing = false;
                            try {
                                mMusicPlayerListener.onError(what, extra);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });

                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.d(LOG_TAG, "setOnCompletionListener");

                            // update the view
                            //notificationView.setImageViewResource(R.id.button_play, android.R.drawable.ic_media_play);
                            //notification.actions[1].icon = android.R.drawable.ic_media_play;
                            notification = createNotification(android.R.drawable.ic_media_play, currentTrackName, currentArtistName);
                            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);

                            try {
                                mMusicPlayerListener.onCompletion();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        isAudioLoaded = true;
                        isAudioPreparing = false;

                        if (playAfterLoaded) {

                            //notificationView.setImageViewResource(R.id.button_play, android.R.drawable.ic_media_pause);
                            //notification.actions[1].icon = android.R.drawable.ic_media_pause;
                            notification = createNotification(android.R.drawable.ic_media_pause, currentTrackName, currentArtistName);
                            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);

                            mMediaPlayer.start();
                        }
                    }
                });
                currentPlayingUri = audioUri;
                mMediaPlayer.prepareAsync();

                //notificationView.setTextViewText(R.id.textview_text1, dataset.get(currentMusicPosition).get("trackName"));
                //notificationView.setTextViewText(R.id.textview_text2, artist.get("artistName"));
                //notificationView.setImageViewResource(R.id.button_play, android.R.drawable.ic_media_play);
                currentTrackName =  dataset.get(currentMusicPosition).get("trackName");
                currentArtistName = artist.get("artistName");
                if (dataset.get(currentMusicPosition).get("smallAlbumImage").length() > 0) {

                    // start thread to download...
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url = dataset.get(currentMusicPosition).get("smallAlbumImage");
                                largeBitmap = Picasso.with(MusicPlayerService.this).load(url).get();
                                mHandler.sendEmptyMessage(1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                } else {
                    largeBitmap = null;
                    mHandler.sendEmptyMessage(1);
                }



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

        @Override
        public void setArtist(Map artist) throws RemoteException {
            MusicPlayerService.this.artist = (Hashtable<String, String>) artist;
        }

        @Override
        public void setDataset(List dataset, int currentMusicPosition) throws RemoteException {
            MusicPlayerService.this.dataset = (ArrayList<Hashtable<String, String>>) dataset;
            MusicPlayerService.this.currentMusicPosition = currentMusicPosition;
        }

        @Override
        public Map getArtist() throws RemoteException {
            return MusicPlayerService.this.artist;
        }

        @Override
        public List getDataset() throws RemoteException {
            return MusicPlayerService.this.dataset;
        }

        @Override
        public int getCurrentMusicPosition() throws RemoteException {
            return MusicPlayerService.this.currentMusicPosition;
        }

        @Override
        public String getCurrentPlayingUriStr() throws RemoteException {
            return currentPlayingUri.toString();
        }
    };

    // copy from stackoverflow
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageTask() {

        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

        }
    }


}
