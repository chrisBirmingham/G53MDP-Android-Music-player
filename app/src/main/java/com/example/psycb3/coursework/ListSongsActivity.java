package com.example.psycb3.coursework;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity to show all songs or songs based on a certain artist or album
 */
public class ListSongsActivity extends AppCompatActivity {
    static final int ARTIST = 1;
    static final int TITLE = 2;
    static final int ALBUM = 3;
    static final int DATA = 4;
    private Cursor databaseCursor;

    /**
     * Method to create the activity and get the query from the intent bundle
     * @param savedInstanceState
     *      The saved instance bundle. Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_list_items);
        Bundle bundle = this.getIntent().getExtras();
        String query;
        try {
            query = bundle.getString("query");
        } catch(Exception e){
            query = null;
        }
        this.createView(query);
    }

    private void createView(String query) {
        TextView textView = (TextView) this.findViewById(R.id.header);
        if (Util.checkExternalStorage()) {
            textView.setText(String.format("%s", "Songs"));
            this.getMusicFromMediaStore(query);
        } else {
            Util.createError(this, "No external storage", "Your external storage appears to be missing. Please check if it is inserted or mounted properly");
        }
    }

    /**
     * Method to get the music files on the sd card from the media store and show them in the list view and register the on click method
     * @param query
     *      The media store query
     */
    private void getMusicFromMediaStore(final String query) {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA
        };
        this.databaseCursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, query, null, null);
        if (this.databaseCursor == null) {
            Util.createError(this, "No Music", "There is no music on your SD card. Please add some");
        } else {
            if (this.databaseCursor.moveToFirst()) {
                String[] columnsToDisplay = new String[]{
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ARTIST
                };
                int[] colsResIds = new int[]{
                        R.id.songTitle, R.id.songArtist, R.id.songAlbum
                };
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.song_items_layout, this.databaseCursor, columnsToDisplay, colsResIds, 0);
                ListView listView = (ListView) findViewById(R.id.albumView);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClick(query));
            } else {
                Util.createError(this, "No Music", "There is no music on your SD card. Please add some");
            }
        }
    }

    /**
     * method to destroy the activity and close the cursor
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.databaseCursor.close();
    }

    /**
     * Class to deal with list item clicks
     */
    public class OnItemClick implements AdapterView.OnItemClickListener{
        String query;

        public OnItemClick(String query){
            this.query = query;
        }

        /**
         * Method to register the onclick listener on the list view. Gets the content of the cursor at that point and bundles it up for the next activity
         * @param parent
         *      The adapter view
         * @param view
         *      The view related with the event
         * @param position
         *      The position of the item clicked
         * @param id
         *      An id
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CursorWrapper wrapper = (CursorWrapper) parent.getItemAtPosition(position);
            Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
            Bundle bundle = new Bundle();
            String[] songInfo = {
                    this.query,
                    wrapper.getString(ARTIST),
                    wrapper.getString(TITLE),
                    wrapper.getString(ALBUM),
                    wrapper.getString(DATA)
            };
            bundle.putInt("position", position);
            bundle.putStringArray("songData", songInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}
