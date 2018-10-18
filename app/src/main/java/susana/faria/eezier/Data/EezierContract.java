package susana.faria.eezier.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class EezierContract {

    public static final String CONTENT_AUTHORITY = "susana.faria.eezier";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NAME_CARDS = "People_Inventory";

    private EezierContract() {
    }

    public static final class EezierEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NAME_CARDS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NAME_CARDS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NAME_CARDS;

        public final static String TABLE_NAME = "People_Inventory";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_NAME = "Person_Name";
        public static final String COLUMN_IMAGE = "Person_Photo";

    }
}

