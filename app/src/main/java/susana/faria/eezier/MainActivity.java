package susana.faria.eezier;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import susana.faria.eezier.Data.EezierContract.EezierEntry;

import static susana.faria.eezier.Data.EezierProvider.LOG_TAG;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PERSON_LOADER = 0;
    private GridCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertNewPerson();
            }
        });
        
        int numberOfColumns = 2;
        RecyclerView gridRecyclerView = findViewById(R.id.gridrv);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mCursorAdapter = new GridCursorAdapter(this, null);
        gridRecyclerView.setAdapter(mCursorAdapter);



        getLoaderManager().initLoader(PERSON_LOADER, null, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void insertNewPerson() {

        // Get Uri for example photo from drawable resource
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.outline_face_black_18dp)
                + '/' + getResources().getResourceTypeName(R.drawable.outline_face_black_18dp) + '/' + getResources().getResourceEntryName(R.drawable.outline_face_black_18dp));

        Log.v(LOG_TAG, "Example photo uri: " + String.valueOf(imageUri));

        // Create a ContentValues object to insert a new blank person card.
        ContentValues values = new ContentValues();
        values.put(EezierEntry.COLUMN_NAME, getString(R.string.name));
        values.put(EezierEntry.COLUMN_IMAGE, String.valueOf(imageUri));

        getContentResolver().insert(EezierEntry.CONTENT_URI, values);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {EezierEntry._ID,
                EezierEntry.COLUMN_NAME, EezierEntry.COLUMN_IMAGE};
        return new CursorLoader(this, EezierEntry.CONTENT_URI, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}

