package com.suzhaomin.voice_recorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Comparator;

public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String LOG_TAG = "DBHelper";

    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    public static final String databasename = "saved_recordings.db";
    private static final int databaseversion = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String table_name = "saved_recordings";

        public static final String name = "recording_name";
        public static final String filepath = "file_path";
        public static final String record_length = "length";
        public static final String time_added = "time_added";
    }


    private static final String TEXT_TYPE = " TEXT";


    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBHelperItem.table_name + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.name + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.filepath + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.record_length + " INTEGER " + COMMA_SEP +
                    DBHelperItem.time_added + " INTEGER " + ")";

    @SuppressWarnings("unused")
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBHelperItem.table_name;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, databasename, null, databaseversion);
        mContext = context;
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public RecordingItem getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.name,
                DBHelperItem.filepath,
                DBHelperItem.record_length,
                DBHelperItem.time_added
        };
        Cursor c = db.query(DBHelperItem.table_name, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
            item.setName(c.getString(c.getColumnIndex(DBHelperItem.name)));
            item.setFilePath(c.getString(c.getColumnIndex(DBHelperItem.filepath)));
            item.setLength(c.getInt(c.getColumnIndex(DBHelperItem.record_length)));
            item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.time_added)));
            c.close();
            return item;
        }
        return null;
    }

    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { String.valueOf(id) };
        db.delete(DBHelperItem.table_name, "_ID=?", whereArgs);
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { DBHelperItem._ID };
        Cursor c = db.query(DBHelperItem.table_name, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<RecordingItem> {
        public int compare(RecordingItem item1, RecordingItem item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }

    public long addRecording(String recordingName, String filePath, long length) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.name, recordingName);
        cv.put(DBHelperItem.filepath, filePath);
        cv.put(DBHelperItem.record_length, length);
        cv.put(DBHelperItem.time_added, System.currentTimeMillis());
        long rowId = db.insert(DBHelperItem.table_name, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void renameItem(RecordingItem item, String recordingName, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.name, recordingName);
        cv.put(DBHelperItem.filepath, filePath);
        db.update(DBHelperItem.table_name, cv,
                DBHelperItem._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public long restoreRecording(RecordingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.name, item.getName());
        cv.put(DBHelperItem.filepath, item.getFilePath());
        cv.put(DBHelperItem.record_length, item.getLength());
        cv.put(DBHelperItem.time_added, item.getTime());
        cv.put(DBHelperItem._ID, item.getId());
        long rowId = db.insert(DBHelperItem.table_name, null, cv);
        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }
}
