package edu.ap.mapsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapSQLiteHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "bibsaver.db";
  private static final String TABLE_BIBLIOTHEKEN = "BIBLIOTHEKEN";
  private static final int DATABASE_VERSION = 15;

  public MapSQLiteHelper(Context context) {
	  super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
	  String CREATE_BIBLIOTHEKEN_TABLE = "CREATE TABLE " + TABLE_BIBLIOTHEKEN + "(_id INTEGER PRIMARY KEY, naam STRING, longitude DOUBLE, latitude DOUBLE)";
      db.execSQL(CREATE_BIBLIOTHEKEN_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	  db.execSQL("DROP TABLE IF EXISTS " + TABLE_BIBLIOTHEKEN);
      onCreate(db);
  }

    public ArrayList<Bibliotheek> getAllBibliotheken() {
        ArrayList allBibliotheken = new ArrayList<Bibliotheek>();
        SQLiteDatabase db = this.getReadableDatabase();
        //int count = db.rawQuery("select * from " + TABLE_ZONES, null).getCount();
        //Log.d("edu.ap.mapsaver", "Count : " + count);
        Cursor cursor = db.rawQuery("select * from " + TABLE_BIBLIOTHEKEN, null);
        if (cursor.moveToFirst()) {
            do {
                String naam = cursor.getString(1);
                Double longitude = Double.parseDouble(cursor.getString(2));
                Double latitude = Double.parseDouble(cursor.getString(3));
                allBibliotheken.add(new Bibliotheek(naam, longitude, latitude));
            } while (cursor.moveToNext());
        }

        return allBibliotheken;
    }
  
  public void saveBibliotheken(JSONArray allBibliotheken) {
      SQLiteDatabase db = this.getWritableDatabase();
          for (int i = 0; i < allBibliotheken.length(); i++) {
              try {
                  JSONObject obj = (JSONObject) allBibliotheken.get(i);
                  String naam = obj.getString("naam");
                  Double longitude = Double.parseDouble(obj.getString("point_lng"));
                  Double latitude = Double.parseDouble(obj.getString("point_lat"));

                  ContentValues values = new ContentValues();
                  values.put("naam", naam);
                  values.put("longitude", longitude);
                  values.put("latitude", latitude);

                  db.insert(TABLE_BIBLIOTHEKEN, null, values);
              }
              catch(Exception ex) {
                  Log.e("edu.ap.mapsaver", ex.getMessage());
              }
          }
          db.close();
      }
  }