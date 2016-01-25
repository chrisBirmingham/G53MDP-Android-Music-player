package com.example.psycb3.coursework;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity to show the music player view and control clicks for the music player
 */
public class MusicPlayerActivity extends AppCompatActivity {
    static final int QUERY = 0;
    static final int SONG_ARTIST = 1;
    static final int SONG_TITLE = 2;
    static final int SONG_ALBUM = 3;
    static final int SONG_PATH = 4;
    static final int ALBUM_ARTWORK = 1;
    static final int ARTIST = 1;
    static final int TITLE = 2;
    static final int ALBUM = 3;
    static final int DATA = 4;

    private MusicPlayerService.MusicPlayerBinder myService = null;
    private Uri songPath;
    private boolean firstTime;
    private boolean playing;
    private int currentSongPosition;
    private String query;
    private String[] data;
    Handler handler = new Handler();

    private ServiceConnection serviceConnection = new ServiceConnection(){
        /**
         * method called when the server is connected. Contains methods for it to use and
         * calls the get song data activity to check if song is playing and what song is
         * going to be played
         * @param name
         *      A name. Not used
         * @param service
         *      The service binder handle.
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = (MusicPlayerService.MusicPlayerBinder) service;
            MusicPlayerActivity.this.getSongData();
            myService.setListener(new MusicPlayerListener() {
                /**
                 * Methods that is called when a song has finished to skip it to the next song
                 */
                @Override
                public void songEnded() {
                    nextSong(null);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

    /**
     * Creates activity. Gets bundle data and calls method to create gui
     * @param savedInstanceState
     *      The saved state bundle. Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_music_player);
        this.firstTime = true;
        this.playing = false;
        Bundle bundle = this.getIntent().getExtras();
        this.currentSongPosition = bundle.getInt("position");
        String[] songInfo = bundle.getStringArray("songData");
        this.bindService(new Intent(this, MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        if(songInfo != null) {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
            if (songInfo[QUERY] == null) {
                this.query = null;
            } else {
                this.query = songInfo[QUERY];
            }
            this.data = songInfo;
            this.createGUI(songInfo);
        } else {
            Util.createError(this, "Internal error", "There was an internal error with the program");
        }
    }

    /**
     * Method called to get shared preferences. If the clicked on song
     * is the one in the shared preferences it does nothing else it
     * checks if the media player is playing and if so starts playing
     * that song
     */
    private void getSongData(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String songTitle = sharedPreferences.getString("songTitle", null);
        String songArtist = sharedPreferences.getString("songArtist", null);
        String songAlbum = sharedPreferences.getString("songAlbum", null);
        if(songTitle != null && songArtist != null && songAlbum != null) {
            if (songTitle.equals(this.data[SONG_TITLE]) && songArtist.equals(this.data[SONG_ARTIST]) && songAlbum.equals(this.data[SONG_ALBUM])) {
                Log.d("Same song", "The same song was selected");
            } else {
                if (this.myService.getPlaying()) {
                    this.playPauseMusic(null);
                }
            }
        }
    }

    /**
     * method called when back button is pressed. Saves the current song data
     * into shared preferences
     */
    @Override
    public void onBackPressed(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("songTitle", this.data[SONG_TITLE]);
        editor.putString("songArtist", this.data[SONG_ARTIST]);
        editor.putString("songAlbum", this.data[SONG_ALBUM]);
        editor.apply();
        super.onBackPressed();
    }

    /**
     * Method that calls other methods to add text and images to the GUI. Uses
     * threads to change GUI
     * @param songData
     *      The bundled song data
     */
    private void createGUI(final String[] songData){
        ImageView button = (ImageView) findViewById(R.id.playPauseButton);
        button.setImageResource(R.drawable.play);
        this.songPath = Uri.parse(songData[SONG_PATH]);
        TextView songTitle = (TextView) MusicPlayerActivity.this.findViewById(R.id.songName);
        songTitle.setText(String.format("%s", songData[SONG_TITLE]));
        new Thread(new Runnable() {
            /**
             * Method to set the album artwork if it exists
             * @param uri
             *      The path to the album artwork
             */
           private void getArtwork(final String uri, final Cursor cursor){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = (ImageView) MusicPlayerActivity.this.findViewById(R.id.albumImage);
                        imageView.setImageURI(Uri.parse(uri));
                        cursor.close();
                    }
                });
            }

            /**
             * Method to set the default artwork if the song doesn't have
             * any album artwork
             */
            private void setDefaultArtwork(){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = (ImageView) MusicPlayerActivity.this.findViewById(R.id.albumImage);
                        imageView.setImageResource(R.drawable.music);
                    }
                });
            }

            @Override
            public void run() {
                String[] projection = {
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM_ART
                };
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Albums.ALBUM + " == '" + songData[SONG_ALBUM] + "'", null, null, null);
                if(cursor != null) {
                    cursor.moveToFirst();
                    if (cursor.getString(ALBUM_ARTWORK) != null) {
                        this.getArtwork(cursor.getString(ALBUM_ARTWORK), cursor);
                    } else {
                        this.setDefaultArtwork();
                        cursor.close();
                    }
                }
            }
        }).start();
    }

    /**
     * Method to check which command to send to the media service.
     * If its playing tell the media service to stop while if its
     * paused tell it to start playing
     * @param view
     *      The view related to the event. Not used
     */
    public void playPauseMusic(View view){
        TextView status = (TextView) this.findViewById(R.id.status);
        ImageView button = (ImageView) this.findViewById(R.id.playPauseButton);
        if(this.playing){
            this.playing = false;
            this.myService.stopMusic();
            status.setText(String.format("%s", "Paused"));
            button.setImageResource(R.drawable.play);
        } else {
            button.setImageResource(R.drawable.pause);
            status.setText(String.format("%s", "Playing"));
            this.playing = true;
            if (this.firstTime) {
                this.firstTime = false;
                this.myService.playMusicFirstTime(this.songPath);
                Util.createNotification(this, "Playing " + this.data[SONG_TITLE]);
            } else {
                this.myService.playMusic();
            }
        }
    }

    /**
     *  Method to change the source of the song going to
     *  be played
     */
    private void changeSongSource(){
        if(this.playing) {
            this.myService.stopMusic();
            this.myService.playMusicFirstTime(this.songPath);
            Util.createNotification(this, "Playing " + this.data[SONG_TITLE]);
        } else {
            this.myService.changeSource(this.songPath);
            this.firstTime = true;
        }
    }

    /**
     * Method to get the available rows based on the current playlist query
     * @return
     *      Cursor to the selected query
     */
    private Cursor createQuery(){
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        return getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, this.query, null, null, null);
    }

    /**
     *  On button press attempts to get the song after the current based on the given query.
     *  If the song is playing it will play the next song
     * @param view
     *      The view related to the event. Not used
     */
    public void nextSong(View view){
        Cursor cursor = this.createQuery();
        if(cursor != null) {
            cursor.move(this.currentSongPosition + 1);
            if (!cursor.isLast()) {
                cursor.moveToNext();
                String songData[] = {
                        this.query,
                        cursor.getString(ARTIST),
                        cursor.getString(TITLE),
                        cursor.getString(ALBUM),
                        cursor.getString(DATA)
                };
                this.data = songData;
                this.createGUI(songData);
                this.changeSongSource();
                this.currentSongPosition++;
            } else {
                TextView textView = (TextView) this.findViewById(R.id.status);
                textView.setText(String.format("%s", "Stopped"));
                this.myService.stopMusic();
            }
            cursor.close();
        }
    }

    /**
     *  On button press attempts to get the song before based on the given query. if the song is playing
     *  it will pay the next song
     * @param view
     *      The view related to the event. Not used
     */
    public void previousSong(View view){
        Cursor cursor = this.createQuery();
        if(cursor != null) {
            cursor.move(this.currentSongPosition + 1);
            if (!cursor.isFirst()) {
                cursor.moveToPrevious();
                String songData[] = {
                        this.query,
                        cursor.getString(ARTIST),
                        cursor.getString(TITLE),
                        cursor.getString(ALBUM),
                        cursor.getString(DATA)
                };
                this.data = songData;
                this.createGUI(songData);
                this.changeSongSource();
                this.currentSongPosition--;
            } else {
                this.myService.skipMusic(0);

            }
            cursor.close();
        }
    }

    /**
     * Method to destroy activity and disconnect the service
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(serviceConnection != null){
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    /**
     * Class that listens for phone calls and stops music when someone is calling the
     * phone. Will resume the song once the call has ended.
     */
    public class PhoneListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber){
            super.onCallStateChanged(state, incomingNumber);
            if(state == TelephonyManager.CALL_STATE_RINGING){
                MusicPlayerActivity.this.playPauseMusic(null);
            } else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
                MusicPlayerActivity.this.playPauseMusic(null);
            }
        }
    }
}
