package com.example.psycb3.coursework;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * The main activity of the application
 */
public class MainActivity extends AppCompatActivity {
    private MusicPlayerService.MusicPlayerBinder myService = null;

    private ServiceConnection serviceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = (MusicPlayerService.MusicPlayerBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

    /**
     * Method to create activity and clear any saved preferences from the previous session
     * and bind to the service to always keep the service running
     * @param savedInstanceState
     *      A bundle for saved state content. Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        this.bindService(new Intent(this, MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Method to destroy the service and unbind the connected service
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(serviceConnection != null){
            this.myService.stopMusic();
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    /**
     * Method that is called when the button is clicked to load the new ListSongsActivity
     * @param view
     *      The view related to the event. Not used
     */
    public void viewSongs(View view){
        Intent intent = new Intent(this, ListSongsActivity.class);
        startActivity(intent);
    }

    /**
     * Method called when button is clicked to get just the albums
     * @param view
     *      The view related to the event. Not used
     */
    public void viewAlbums(View view){
        this.callListActivity("albums");
    }

    /**
     * Method called when button is clicked to get just the artists
     * @param view
     *      The view related to the event. Not used
     */
    public void viewArtists(View view){
        this.callListActivity("artists");
    }

    /**
     * Method to call the next activity and pass it the requested query option
     * @param searchOption
     *      The search option to query in the list items activity
     */
    private void callListActivity(String searchOption){
        Intent intent = new Intent(this.getApplicationContext(), ListAlbumsOrArtistsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("searchOption", searchOption);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
