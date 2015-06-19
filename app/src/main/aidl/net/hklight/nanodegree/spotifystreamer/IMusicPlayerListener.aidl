// IMusicPlayerListener.aidl
package net.hklight.nanodegree.spotifystreamer;

// Declare any non-default types here with import statements

interface IMusicPlayerListener {
    void onError(int what, int extra);
    void onCompletion();
}
