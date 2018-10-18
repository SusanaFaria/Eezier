package susana.faria.eezier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import susana.faria.eezier.Data.EezierContract.EezierEntry;

import static susana.faria.eezier.Data.EezierProvider.LOG_TAG;

public class EezierEditCard extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PERSON_LOADER = 0;
    Uri mCurrentPersonUri;
    private Long personID;
    private EditText name;
    private ImageView photo;
    private Uri mPhotoUri;
    private boolean mPersonHasChanged;
    private Button choosePhoto;
    private Button save;
    private static final int PICK_IMAGE_REQUEST = 0;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPersonHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eezier_edit_card);

        Intent personEdit = getIntent();
        mCurrentPersonUri = personEdit.getData();
        setTitle("Edit Person");
        getLoaderManager().initLoader(PERSON_LOADER, null, this);

        name = findViewById(R.id.edit_person_name);
        photo = findViewById(R.id.changePhoto);
        choosePhoto = findViewById(R.id.choose_photo);
        save = findViewById(R.id.save);
        name.setOnTouchListener(mTouchListener);
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPhoto();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePerson();
            }
        });
    }

    private void selectPhoto() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mPhotoUri = resultData.getData();
                photo.setImageBitmap(getBitmapFromUri(mPhotoUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int width = photo.getWidth();
        int height = photo.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;


            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;

            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


    private void savePerson() {
        String personName = name.getText().toString().trim();
        String photoUriString = String.valueOf(photo);
        mPhotoUri = Uri.parse(photoUriString);

        ContentValues values = new ContentValues();
        values.put(EezierEntry.COLUMN_NAME, personName);
        values.put(EezierEntry.COLUMN_IMAGE, String.valueOf(mPhotoUri));

        int rowsAffected = getContentResolver().update(mCurrentPersonUri, values, null, null);



        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, getString(R.string.edit_fail),
                    Toast.LENGTH_SHORT).show();
            Log.v("EezierEdit", rowsAffected + " saved to db");
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.edit_success),
                    Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                EezierEntry._ID,
                EezierEntry.COLUMN_NAME,
                EezierEntry.COLUMN_IMAGE,};
        return new CursorLoader(
                this,
                mCurrentPersonUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        ViewTreeObserver viewTreeObserver = photo.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    photo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                photo.setImageBitmap(getBitmapFromUri(mPhotoUri));
            }
        });

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int person_id = cursor.getColumnIndex(EezierEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(EezierEntry.COLUMN_NAME);
            int photoColumnIndex = cursor.getColumnIndex(EezierEntry.COLUMN_IMAGE);


            // Extract out the value from the Cursor for the given column index
            personID = cursor.getLong(person_id);
            String person_name = cursor.getString(nameColumnIndex);
            String person_photo = cursor.getString(photoColumnIndex);
            mPhotoUri = Uri.parse(person_photo);


            // Update the views on the screen with the values from the database
            name.setText(person_name);
            photo.setImageBitmap(getBitmapFromUri(mPhotoUri));

        }

        }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        name.setText("");
        photo.setImageBitmap(null);

    }
}
