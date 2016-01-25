package com.example.psycb3.coursework;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 *  Class that creates a service to play, pause and change music
 */
public class MusicPlayerService extends Service {
    private final IBinder binder = new MusicPlayerBinder();
    private MediaPlayer player = new MediaPlayer();
    private MusicPlayerListener serviceListener;

    /**
     * Method to create the service and initialise the media player
     */
    @Override
    public void onCreate(){
        super.onCreate();
        this.initMusicPlayer();
    }

    private void initMusicPlayer(){
        this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    /**
     * Method to create a new media player, set its source and start it
     * @param songName
     *      The uri of the song to play
     */
    private void changeSource(Uri songName){
        try {
            this.player = new MediaPlayer();
            this.initMusicPlayer();
            this.player.setDataSource(getApplicationContext(), songName);
            this.player.prepare();
        } catch (IOException e) {
            Log.d("Exception", e.toString());
        }
    }

    /**
     * When first playing the song it will destroy the old one and call the change source method to
     * create a new one.
     * @param songName
     *      The name of the song to be played
     */
    public void playMusicFirstTime(Uri songName){
        this.player.stop();
        this.player.release();
        this.changeSource(songName);
        this.player.start();
        this.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                serviceListener.songEnded();
            }
        });
    }

    /**
     * method to play the music
     */
    public void playMusic(){
        this.player.start();
    }

    /**
     * Method to stop the music
     */
    public void stopMusic(){
        this.player.pause();
    }

    /**
     * Method to skip the song to a certain position
     * @param position
     *      The requested position
     */
    public void skipMusic(int position){
        this.player.seekTo(position);
    }

    public boolean getPlaying(){
        return this.player.isPlaying();
    }

    /**
     * The binder class for the service
     */
    public class MusicPlayerBinder extends Binder{
        public void playMusicFirstTime(Uri songName){
            MusicPlayerService.this.playMusicFirstTime(songName);
        }

        public void playMusic(){
            MusicPlayerService.this.playMusic();
        }

        public void stopMusic(){
            MusicPlayerService.this.stopMusic();
        }

        public void changeSource(Uri songPath){
            MusicPlayerService.this.changeSource(songPath);
        }

        public void skipMusic(int position){
            MusicPlayerService.this.skipMusic(position);
        }

        public void setListener(MusicPlayerListener listener){
            serviceListener = listener;
        }

        public boolean getPlaying(){
            return MusicPlayerService.this.getPlaying();
        }
    }
}
