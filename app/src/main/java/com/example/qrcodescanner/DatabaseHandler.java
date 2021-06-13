package com.example.qrcodescanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.qrcodescanner.ui.QRDetailsDAO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHandler";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "QRCodeDB";
    private static final String TABLE_Demo = "QRCodeDB";

    private static final String TEXT_DATA = "data";
    private static final String TIME_STAMP = "timestamp";


    String CREATE_DEMO_TABLE = "CREATE TABLE " + TABLE_Demo +   "("+
            TEXT_DATA + " TEXT ,"+ TIME_STAMP + " TEXT "+ ")";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DEMO_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Demo );
        onCreate(db);
    }

    public void addQRDetails(QRDetailsDAO objQRDetails) throws JSONException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEXT_DATA,  objQRDetails.getStrQRData());
        values.put(TIME_STAMP, objQRDetails.getTimeStampVal());
        db.insert(TABLE_Demo , null, values);
        db.close();
    }


    public List<QRDetailsDAO> getAllQRCodeResults() {
        try {
            List<QRDetailsDAO> historyQRCodes = new ArrayList<QRDetailsDAO>();

            // Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_Demo;

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    QRDetailsDAO qrDetailsDAO = new QRDetailsDAO();
                    qrDetailsDAO.setStrQRData(cursor.getString(0));
                    qrDetailsDAO.setTimeStampVal(cursor.getString(1));

                    historyQRCodes.add(qrDetailsDAO);
                }
                while (cursor.moveToNext());
            }

            // return contact list
            return historyQRCodes;

        }
        catch (Exception e)
        {
            Log.e(TAG,"error in getAllContacts method");
            return null;
        }
    }

}
