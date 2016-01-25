package com.example.psycb3.coursework;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Class to display all the albums or artists on the device
 */
public class ListAlbumsOrArtistsActivity extends AppCompatActivity {
    static final int ARTIST = 1;
    static final int ALBUM = 1;
    private Cursor databaseCursor;

    /**
     *  Method to create the activity and get the requested search option
     * @param savedInstanceState
     *      Saved instance bundle. Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);
        Bundle bundle = this.getIntent().getExtras();
        String search = bundle.getString("searchOption");
        this.createView(search);
    }

    private void createView(String search){
        if(Util.checkExternalStorage()){
            this.getAlbumsFromMediaStore(search);
        } else {
            Util.createError(this, "No external storage", "Your external storage appears to be missing. Please check if it is inserted or mounted properly");
        }
    }

    /**
     * Method to call the media store and get the artists or albums of songs
     * currently on the device
     * @param searchOption
     *      The requested search option
     */
    private void getAlbumsFromMediaStore(final String searchOption){
        String[] projection;
        String[] columnsToDisplay;
        TextView text = (TextView) this.findViewById(R.id.header);
        if(searchOption.equals("albums")) {
            text.setText(String.format("%s", "Albums"));
            projection = new String[]{
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM
            };
            this.databaseCursor = this.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, null);
            columnsToDisplay = new String[]{
                    MediaStore.Audio.Albums.ALBUM
            };
        } else {
            text.setText(String.format("%s", "Artists"));
            projection = new String[]{
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.ARTIST
            };
            this.databaseCursor = this.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, null, null, null);
            columnsToDisplay = new String[]{
                    MediaStore.Audio.Albums.ARTIST
            };
        }
        this.displayInList(searchOption, columnsToDisplay);
    }

    /**
     * Method to display the search query in a list view in the activity
     * @param searchOption
     *      The search query
     * @param columnsToDisplay
     *      The columns to display in the list view
     */
    private void displayInList(String searchOption, String[] columnsToDisplay){
        if(this.databaseCursor == null){
            Util.createError(this, "No Music", "There is no music on your SD card. Please add some");
        } else {
            if (this.databaseCursor.moveToFirst()) {
                int[] colsResIds = new int[]{R.id.albumName};
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_items_layout, this.databaseCursor, columnsToDisplay, colsResIds, 0);
                final ListView listView = (ListView) findViewById(R.id.albumView);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnclickClass(searchOption));
            } else {
                Util.createError(this, "No Music", "There is no music on your SD card. Please add some");
            }
        }
    }

    /**
     *  Method to destroy the activity and release the cursor
     *  containing the requested query
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.databaseCursor.close();
    }

    /**
     * Class to extend onclickListener for list view item clicks
     */
    public class OnclickClass implements AdapterView.OnItemClickListener {
        private String searchOption;

        public OnclickClass(String searchOption){
            this.searchOption = searchOption;
        }

        /**
         * Method to ge the album or artist at that point in the list and create a query
         *  for the other activity to receive and put it into a bundle
         * @param parent
         *      The parent view for the list
         * @param view
         *      The list view. Not used
         * @param position
         *      The clicked list view item position
         * @param id
         *      The id of the list view position. Not used
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CursorWrapper wrapper = (CursorWrapper) parent.getItemAtPosition(position);
            Intent intent = new Intent(getApplicationContext(), ListSongsActivity.class);
            Bundle bundle = new Bundle();
            if(this.searchOption.equals("albums")) {
                bundle.putString("query", MediaStore.Audio.Media.ALBUM + " == '" + wrapper.getString(ALBUM) + "'");
                intent.putExtras(bundle);
            } else {
                bundle.putString("query", MediaStore.Audio.Media.ARTIST + " == '" + wrapper.getString(ARTIST) + "'");
                intent.putExtras(bundle);
            }
            startActivity(intent);
        }
    }
}
