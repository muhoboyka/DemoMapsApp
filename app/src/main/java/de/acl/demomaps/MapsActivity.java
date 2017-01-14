package de.acl.demomaps;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.value;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;

    GPSTracker mGPSTracker;
    Geocoder geocoder;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfekt!", Toast.LENGTH_LONG).show();         // Text, if everything is fine
            setContentView(R.layout.activity_maps);
            initMap();
        } else {
            // No Google Maps Layout
            Toast.makeText(this, "Karte konnte nicht geladen werden!", Toast.LENGTH_LONG).show();
        }

        mGPSTracker = new GPSTracker(this);                   // Schnapp dir die Werte aus GPSTracker.java
        geocoder = new Geocoder(this);                       // Klasse um Geocoding & Reverse Geocoding zu ermöglichen
    }

    // Fragment Map -> show in mapFragment
    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        // That is what I will access
        mapFragment.getMapAsync(this);
    }

    // able to AccessGPlay?
    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to play services!", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (mGoogleMap != null) {
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MapsActivity.this.setMarker("Local", latLng.latitude, latLng.longitude);
                }
            });
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                    Geocoder gc = new Geocoder(MapsActivity.this);
                    LatLng ll = marker.getPosition();
                    double lat = ll.latitude;
                    double lng = ll.longitude;
                    List<Address> list = null;
                    try {
                        list = gc.getFromLocation(lat, lng, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.showInfoWindow();
                }
            });


            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {             // here is the point to implement your infoWindow

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });
        }

      //  goToLocationZoom(50.828170, 12.920495, 15);
    }

    private void goToLocation(double lat, double lng) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mGoogleMap.moveCamera(update);
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    public void geoLocate(View view) throws IOException {

        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();

        if(et.hasFocus())
        {
            et.clearFocus();
        }

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);

        if(list.isEmpty())
        {
            return;
        }

        boolean isNumber = true;
        Address address = list.get(0);
        String number = address.getFeatureName();

        if(number.matches("[0-9]+"))
            isNumber = true;
        else
            isNumber = false;

        String locality = null;
                /*address.getAddressLine(0)+"\n"+
                address.getAddressLine(1)+"\n"+     // Straße
                address.getAddressLine(2);          //PLZ Stadt*/
        // !!!! Achtung AdressLine kann auch leer sein !!!!!

                if(isNumber == false)
                {
                    locality =
                    address.getFeatureName()+"\n"+
                            address.getThoroughfare()+" "+address.getSubThoroughfare() +"\n"+
                    address.getPostalCode()+" "+address.getLocality() ;                                 // Postalcode
                }
                else
                {
                    locality =
                            address.getThoroughfare() +" "+ address.getSubThoroughfare() +"\n"+
                    address.getPostalCode()+" "+address.getLocality() ;                                 // Postalcode
                }


        System.out.print("----------------------------------------------------"+locality+"-------------------------------------------------------");

        //Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocationZoom(lat, lng, 15);

        setMarker(locality, lat, lng);
    }

    Marker marker;

    private void setMarker(String locality, double latitude, double longitude)
    {
        // Write Current Location into TextView
        ((TextView) findViewById(R.id.coordinatesArea)).setText(locality);      //setText("Lat: " + latitude + "; Lon: " + longitude);

        if(marker != null){marker.remove();}

        marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(locality));
        marker.showInfoWindow();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu); // Menu on the to right of the app
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.MapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);// STATIC_OBJECTS
                break;
            case R.id.MapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.MapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.MapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.MapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    LocationRequest mLocationRequest;                                                               //Request user location

    @Override                                                                                       //this takes care of ConnectionCallback
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // Go to
    @Override
    public void onLocationChanged(Location location) {                                                                      // your own Location changed
        if (location == null) {
            Toast.makeText(this, "Unfortunately, we cannot get your current location!", Toast.LENGTH_LONG).show();
        } else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mGoogleMap.animateCamera(update);// to move button:location point
        }

    }

    public void showCoordinates(View view)
    {
        if(mGPSTracker.canGetLocation)
        {
            mGPSTracker.getLocation();                                                                  // currentLoc

            double lat = mGPSTracker.getLatitude();
            double lng = mGPSTracker.getLongitude();

            Geocoder gc = new Geocoder(MapsActivity.this);
            List<Address> list = null;

            try
            {
                list = gc.getFromLocation(lat, lng, 1);                                                 // Lat, Lng, maxResults
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            goToLocation(lat, lng);                                                                     // + gehe zu Location
/* List empty, if position not found -> getting lat lng & showing map on request current location -> Warum?
* geolocator leer?*/

            if(list != null && !list.isEmpty())
            {
                boolean isNumber = true;
                Address address = list.get(0);
                String number = address.getFeatureName();

                if(number.matches("[0-9]+"))
                    isNumber = true;
                else
                    isNumber = false;

                String locality = null;
                /*address.getAddressLine(0)+"\n"+
                address.getAddressLine(1)+"\n"+     // Straße
                address.getAddressLine(2);          //PLZ Stadt*/
                //!!!! Achtung AdressLine kann auch leer sein

                if(isNumber == false)
                {
                    locality =
                            address.getFeatureName()+"\n"+                                              // Name Location
                            address.getThoroughfare()+" "+ address.getSubThoroughfare()+"\n"+           // Straße && Nummer
                            address.getPostalCode()+" "+address.getLocality() ;                         // PostalCode && Ort
                }
                else
                {
                    locality =
                            address.getThoroughfare()+" "+ address.getSubThoroughfare()+"\n"+           // Straße && Nummer
                            address.getPostalCode()+" "+address.getLocality() ;                         // PostalCode && Ort
                }
                setMarker( locality, lat, lng);
            }
            else
            {
                setMarker("position not found by google", lat, lng);
            }
        }
        else
        {
            ((TextView) findViewById(R.id.coordinatesArea)).setText("cannot get location");
            mGPSTracker.showSettingsAlert();
        }

    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

}
