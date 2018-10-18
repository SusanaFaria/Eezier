package susana.faria.eezier.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import susana.faria.eezier.Data.EezierContract.EezierEntry;

public class EezierDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 1;

    public EezierDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        String SQL_CREATE_PEOPLE_TABLE = "CREATE TABLE " + EezierEntry.TABLE_NAME + " ("
                + EezierEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EezierEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + EezierEntry.COLUMN_IMAGE + " TEXT NOT NULL" + ");";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PEOPLE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
