package susana.faria.eezier.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

import susana.faria.eezier.Data.EezierContract.EezierEntry;
import susana.faria.eezier.R;

public class EezierProvider extends ContentProvider {

    public static final String LOG_TAG = EezierProvider.class.getSimpleName();
    private static final int PEOPLE = 10;
    private static final int PEOPLE_ID = 5;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(EezierContract.CONTENT_AUTHORITY, EezierContract.PATH_NAME_CARDS, PEOPLE);
        sUriMatcher.addURI(EezierContract.CONTENT_AUTHORITY, EezierContract.PATH_NAME_CARDS + "/#", PEOPLE_ID);
    }

    private EezierDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new EezierDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PEOPLE:

                cursor = database.query(EezierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PEOPLE_ID:
                selection = EezierEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(EezierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PEOPLE:
                return EezierEntry.CONTENT_LIST_TYPE;
            case PEOPLE_ID:
                return EezierEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PEOPLE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return insertPerson(uri, contentValues);
                }
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Uri insertPerson(Uri uri, ContentValues values) {
        // Check that the values are not null
        String name = values.getAsString(EezierEntry.COLUMN_NAME);
        if ((TextUtils.isEmpty(name))) {
            Toast.makeText(getContext(), R.string.name_required, Toast.LENGTH_SHORT).show();
            return null;
        }
        String pic = values.getAsString(EezierEntry.COLUMN_IMAGE);
        if ((TextUtils.isEmpty(pic))) {
            Toast.makeText(getContext(), "Pic required", Toast.LENGTH_SHORT).show();
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new person with the given values
        long id = database.insert(EezierEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the book content URI
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PEOPLE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return updatePerson(uri, contentValues, selection, selectionArgs);
                }
            case PEOPLE_ID:
                selection = EezierEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return updatePerson(uri, contentValues, selection, selectionArgs);
                }
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int updatePerson(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        if (values.containsKey(EezierEntry.COLUMN_NAME)) {
            String name = values.getAsString(EezierEntry.COLUMN_NAME);
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), R.string.name_required, Toast.LENGTH_SHORT).show();
                return 0;
            }
           if (values.containsKey(EezierEntry.COLUMN_IMAGE)) {
                String pic = values.getAsString(EezierEntry.COLUMN_IMAGE);
                if (TextUtils.isEmpty(pic)) {
                    Toast.makeText(getContext(), "pic required", Toast.LENGTH_SHORT).show();
                }
           }
        }


        int rowsUpdated = database.update(EezierEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PEOPLE:
                // Delete all rows that match the selection and selection args
                // For  case PEOPLE
                rowsDeleted = database.delete(EezierEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PEOPLE_ID:
                // Delete a single row given by the ID in the URI
                selection = EezierEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(EezierEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;

    }
}
