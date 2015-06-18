// IMusicPlayer.aidl
package net.hklight.nanodegree.spotifystreamer;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean isPlaying();
    void play();
    void pause();
    void stop();
    int getDuration();
    int getCurrentPosition();
    void seekTo(in int positionMs);
    void setDataSource(in Uri datasource, in boolean playAfterLoaded);
    boolean isAudioLoaded();
}
