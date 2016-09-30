package edu.ap.mapsaver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private TextView searchField;
    private Button searchButton;
    private MapView mapView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue mRequestQueue;
    private String urlSearch = "http://nominatim.openstreetmap.org/search?q=";
    private String urlBibliotheken = "http://datasets.antwerpen.be/v4/gis/bibliotheekoverzicht.json";
    ArrayList<Bibliotheek> allBibliotheken = new ArrayList<Bibliotheek>();
    MapSQLiteHelper helper;
    final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new MapSQLiteHelper(this);

        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(18);

        // http://code.tutsplus.com/tutorials/an-introduction-to-volley--cms-23800
        mRequestQueue = Volley.newRequestQueue(this);

        if(!getPreferences()) {
            // A JSONObject to post with the request. Null is allowed and indicates no parameters will be posted along with request.
            JSONObject obj = null;
            // haal alle bibliotheken op
            JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, urlBibliotheken, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideSoftKeyBoard();
                    try {
                        helper.saveBibliotheken(response.getJSONArray("data"));
                        setPreferences(true);
                        allBibliotheken = helper.getAllBibliotheken();
                        Log.d("edu.ap.BibSaver", "Bibliotheken saved to DB");
                        addAllMarkers();

                    }
                    catch (JSONException e) {
                        Log.e("edu.ap.BibSaver", e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("edu.ap.Bibsaver", error.getMessage());
                }
            });
            mRequestQueue.add(jr);
        }

        else {
            allBibliotheken = helper.getAllBibliotheken();
            Log.d("edu.ap.mapsaver", "Bibliotheken retrieved from DB");
            addAllMarkers();
        }


        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Toast.makeText(getApplicationContext(), "GPS not enabled!", Toast.LENGTH_SHORT).show();
            // default = meistraat
            mapView.getController().setCenter(new GeoPoint(51.2244, 4.38566));
        }
        else {
            locationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            mapView.getController().setCenter(new GeoPoint(51.2244, 4.38566));
        }
    }


    private void setPreferences(boolean b) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("db_filled", b);
        editor.commit();
    }

    private boolean getPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("db_filled", false);
    }

    // http://codetheory.in/android-ontouchevent-ontouchlistener-motionevent-to-detect-common-gestures/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int actionType = ev.getAction();
        switch (actionType) {
            case MotionEvent.ACTION_UP:
                // A Projection serves to translate between the coordinate system of
                // x/y on-screen pixel coordinates and that of latitude/longitude points
                // on the surface of the earth. You obtain a Projection from MapView.getProjection().
                // You should not hold on to this object for more than one draw, since the projection of the map could change.
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint)proj.fromPixels((int) ev.getX(), (int) ev.getY());
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    private void addMarker(GeoPoint g, String naam) {
        OverlayItem myLocationOverlayItem = new OverlayItem(naam, "Current Position", g);
        Drawable myCurrentLocationMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.marker, null);
        myLocationOverlayItem.setMarker(myCurrentLocationMarker);

        items.add(myLocationOverlayItem);
        DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().add(currentLocationOverlay);
        this.mapView.invalidate();
    }
    private void addAllMarkers(){

        for (int i = 0; i < allBibliotheken.size(); i++) {
            addMarker(new GeoPoint(allBibliotheken.get(i).getLongitude(),allBibliotheken.get(i).getLatidude()),allBibliotheken.get(i).getNaam());
            Log.d("markers", "marker " + i + "toegevoegd");
        }

    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            mapView.getController().setCenter(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
        }
    }
}