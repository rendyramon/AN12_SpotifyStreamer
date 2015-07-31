package com.huhx0015.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.huhx0015.spotifystreamer.audio.SSMusicEngine;
import com.huhx0015.spotifystreamer.interfaces.OnMusicPlayerListener;

/** -----------------------------------------------------------------------------------------------
 *  [SSMusicService] CLASS
 *  PROGRAMMER: Michael Yoon Huh (Huh X0015)
 *  DESCRIPTION: SSMusicService is a service class that handles the playback of streaming Spotify
 *  tracks in the background.
 *  -----------------------------------------------------------------------------------------------
 */
public class SSMusicService extends Service implements MediaPlayer.OnPreparedListener {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // AUDIO VARIABLES
    private SSMusicEngine ss_music; // SSMusicEngine class object that is used for music functionality.
    private String currentSong = "NONE"; // Sets the default song for the activity.
    private Boolean musicOn = true; // Used to determine if music has been enabled or not.
    private Boolean isPlaying = false; // Indicates that a song is currently playing in the background.

    // FRAGMENT VARIABLES
    private Fragment playerFragment; // References the player fragment attached to this service.

    // LOGGING VARIABLES:
    private static final String LOG_TAG = SSMusicService.class.getSimpleName(); // Used for logging output to logcat.

    // SERVICE VARIABLES
    private final IBinder audioBind = new SSMusicBinder(); // IBinder object that is used to bind this service to an activity.

    // THREADING VARIABLES
    private Handler seekHandler = new Handler(); // Handler for the seekbar update thread.

    /** SERVICE LIFECYCLE METHODS ______________________________________________________________ **/

    // onCreate(): The system calls this method when the service is first created, to perform
    // one-time setup procedures (before it calls either onStartCommand() or onBind()).
    @Override
    public void onCreate() {
        super.onCreate();

        // AUDIO CLASS INITIALIZATION:
        ss_music.getInstance().initializeAudio(getApplicationContext());
    }

    /** SERVICE EXTENSION METHODS ______________________________________________________________ **/

    @Override
    public void onPrepared(MediaPlayer mp) {}

    // onBind(): Runs when this service is successfully bound to the application.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return audioBind;
    }

    // onUnbind(): Runs when this service is unbound from the application.
    @Override
    public boolean onUnbind(Intent intent){

        // If a song is currently playing in the background, the song playback is stopped.
        if (ss_music.getInstance().isSongPlaying()) {
            ss_music.getInstance().stopSong(); // Stops the playback of the song.
        }

        // Releases all audio-related instances if the service is unbound.
        ss_music.getInstance().releaseMedia();

        return false;
    }

    /** SERVICE METHODS ________________________________________________________________________ **/

    // attachPlayerFragment(): Attaches the SSPlayerFragment to the SSMusicEngine class.
    public void attachPlayerFragment(Fragment fragment) {
        this.playerFragment = fragment;
        ss_music.getInstance().attachFragment(fragment);
    }

    // pauseTrack(): Accesses the SSMusicEngine instance to pause the streaming song track.
    public void pauseTrack() {

        // Pauses the song if a song is currently playing in the background.
        if (ss_music.getInstance().isSongPlaying()) {
            ss_music.getInstance().pauseSong();
            seekHandler.removeCallbacks(seekbarThread); // Stops the seekbar update thread.
        }
    }

    // playTrack(): Accesses the SSMusicEngine instance to play the streaming song track.
    public void playTrack(String songUrl, Boolean loop){
        ss_music.getInstance().playSongUrl(songUrl, loop);
        seekHandler.postDelayed(seekbarThread, 1000); // Begins the seekbar update thread.
    }

    /** THREADING METHODS ______________________________________________________________________ **/

    // seekbarThread(): A threaded function which updates the player seekbar in the
    // SSPlayerFragment.
    private Runnable seekbarThread = new Runnable() {

        public void run() {
            seekbarStatus(ss_music.getInstance().getSongPosition()); // Retrieves the current song position.
            seekHandler.postDelayed(this, 1000); // Updates the thread per second.
        }
    };

    /** SUBCLASSES _____________________________________________________________________________ **/

    /**
     * --------------------------------------------------------------------------------------------
     * [SSMusicBinder] CLASS
     * DESCRIPTION: This is a Binder-type subclass that is used to bind the SSMusicService to an
     * activity.
     * --------------------------------------------------------------------------------------------
     */

    public class SSMusicBinder extends Binder {

        // getService(): Returns the SSMusicBinder service.
        public SSMusicService getService() {
            return SSMusicService.this;
        }
    }

    /** INTERFACE METHODS ______________________________________________________________________ **/

    // seekbarStatus(): Signals the SSPlayerFragment on the current song position for updating the
    // player seekbar.
    private void seekbarStatus(int position) {

        if (playerFragment != null) {
            try { ((OnMusicPlayerListener) playerFragment).seekbarStatus(position); }
            catch (ClassCastException cce) {} // Catch for class cast exception errors.
        }
    }
}