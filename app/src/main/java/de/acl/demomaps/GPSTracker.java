package de.acl.demomaps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public final class GPSTracker implements LocationListener {

    private final Context mContext;                                                                         // current state of the object
    public boolean isGPSEnabled = false;                                                                    // flag for GPS status
    boolean isNetworkEnabled = false;                                                                       // flag for network status
    boolean canGetLocation = false;                                                                         // flag for GPS status
    Location location;                                                                                      // geo -Position ( lat & long)
    double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;                                          // min distance to change Updates in meters
    private static final long MIN_TIME_BW_UPDATES = 1;                                                      // minimum time between updates in milliseconds
    protected LocationManager locationManager;                                                              // Declaring a Location Manager
    protected LocationListener listener;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();                                                                                      // current Position
    }

    /**
     * Function to get the user's current location
     *
     * @return
     */
    public Location getLocation() {

        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);                 // getting GPS status
          //  Log.v("isGPSEnabled", "=" + isGPSEnabled);                                                      // Protokolleintrag für GPSE erreichbar

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);         // getting network status
          //  Log.v("isNetworkEnabled", "=" + isNetworkEnabled);                                              // Protokolleintrag für Netzwerk erreichbar

            if (isGPSEnabled == false && isNetworkEnabled == false)                                         //
            {
              //  Log.v("isNetworkEnabled", "=" + isNetworkEnabled);
              //  Log.v("isGPSEnabled", "=" + isGPSEnabled);
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // first ask for GPS position
                if (isGPSEnabled && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                else if (isNetworkEnabled && (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    // Netzewerk, ACCESS && Coarse , not recieving GPSs?
                    Log.v("isGPSEnabled", "=" + isGPSEnabled);
                    location = null;
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    /* locationManager wird mit verändertern Potionsdaten gefüttert, wenn Netzwerk an,
                    * Access_fine_location = granted
                    * Access_coarse Location granted
                    * Wenn Position sich geändert hat, schreib auf*/

                    Log.d("Network", "Network");
                    if (locationManager != null)                                                            // Es sind bereits Daten vorhanden
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);  // schnapp dir letzte bekannte Pos von Netzwerkanbieter
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // GPS an, setze Daten auf LocationManager

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app
     * */
    public void stopUsingGPS() {
        if (locationManager != null) {
            try {
                /*Failure, cannot apply GPS Tracker? anstatt this -> required Content Context? Warum?*/
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(GPSTracker.this);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     * */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        /*Intent i = new Intent("location_update");
                i.putExtra("coordinates", location.getLongitude()+" "+ location.getLatitude());
                sendBroadcast(i);*/

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /*
        * Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                */
    }

}