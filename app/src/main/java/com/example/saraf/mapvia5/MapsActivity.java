package com.example.saraf.mapvia5;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapClickListener {
    //View Attributes
    RelativeLayout layout = null;
    TextView distanceText = null;
    TextView durationText = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //Google Maps Direction API helpers
    String uri = null;
    String maps_API_url = "http://maps.googleapis.com/maps/api/directions/json?";
    String myPosCoordinates = "45.7739883,4.8204955";
    String destCoordinates = "45.778783,4.8395901";
    String response;

    LatLng myPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = (RelativeLayout) RelativeLayout.inflate(this, R.layout.activity_maps, null);
        distanceText = (TextView) layout.findViewById(R.id.distance);
        durationText = (TextView) layout.findViewById(R.id.duration);

        //Get the current location and add a marker at this place
        setUpMapIfNeeded();
        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onMapClick(LatLng point) {
        mMap.clear();
        setUpMapIfNeeded();
        //Add a marker where the user has clicked
        mMap.addMarker(new MarkerOptions().position(point));
        //Get coordinates of the marker and call Google Maps Direction API to get duration and distance from current Location
        destCoordinates = point.latitude + "," + point.longitude;
        uri = maps_API_url + "origin=" + myPosCoordinates + "&destination=" + destCoordinates + "&mode=walking";
        Log.e("uri",uri);
        try {
            response = new callGoogleDirectionAPI().execute(uri).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //Parse the JSON response to get duration and distance in sec and meters
        JSONObject base;
        int durationValue = 0;
        int distanceValue = 0;
        try {
            JSONObject json = new JSONObject(response);
            base = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
            durationValue = base.getJSONObject("duration").getInt("value");
            distanceValue = base.getJSONObject("distance").getInt("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("distance ", "" + distanceValue);
        //Modify the view to make them appear on the screen
        distanceText.setText("Distance: " + distanceValue);
        durationText.setText("Duration: " + durationValue);
        setContentView(layout);
    }

    //Use asynchronous task to call the google Direction API with the uri we build
    private class callGoogleDirectionAPI extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urlToRead) {
            URL url;
            HttpURLConnection conn;
            BufferedReader rd;
            String line;
            String res = "";
            try {
                url = new URL(urlToRead[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    res += line;
                }
                rd.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        //Get the current position
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        Log.e("locationLatitude","###"+(location.getLatitude()));
        Log.e("locationLongitude","###"+(location.getLongitude()));
        Log.e("locationProvider","###"+location.getProvider());
        if(location!=null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            myPosCoordinates = latitude + "," + longitude;
            myPosition = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(myPosition).title("Start"));
        }
        mMap.setOnMapClickListener(this);
    }
}
