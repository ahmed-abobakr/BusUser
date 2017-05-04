package arab_open_university.com.bususer;

import android.*;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location mLastLocation;
    Marker myMarker;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MapFragment mapFragment = new MapFragment();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction().replace(R.id.map_view, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TEST", "request permission Result");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case 101: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    //getLastKnownLocation();
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .enableAutoManage(this, 0, this)
                            .build();

                    googleApiClient.connect();
                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    private void permissionsDenied() {
        Log.w("TEST", "permissionsDenied()");
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(60000)
                .setInterval(60000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        showMarkersAndZoom();
        LocationSettingsRequest.Builder mBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        mBuilder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, mBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        if (mLastLocation != null)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 16), 200, null);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

        DatabaseReference myRef = database.getReference("buses_Info");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    BusStation bus  = snapshot.getValue(BusStation.class);
                    for(String id : bus.getBusesIDs()){
                        getBusesLcations(snapshot.getKey(), id);
                    }
                }

               /* for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.d("TEST", "get Buses Info");
                    BusStation info = (BusStation) snapshot.getValue();
                    for(int i =0; i < info.getBusesIDs().size(); i++){
                        getBusesLcations(dataSnapshot.getKey(), info.getBusesIDs().get(i));
                    }
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*DatabaseReference myRef300 = database.getReference("buses_info").child("300");
        myRef300.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BusStation bus  = (BusStation) dataSnapshot.getValue();
                for(String id : bus.getBusesIDs()){
                    getBusesLcations(id, "300");
                }
               *//* for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.d("TEST", "get Buses Info");
                    BusStation info = (BusStation) snapshot.getValue();
                    for(int i =0; i < info.getBusesIDs().size(); i++){
                        getBusesLcations(dataSnapshot.getKey(), info.getBusesIDs().get(i));
                    }
                }*//*
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(isPermissionsGranted(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION})) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .enableAutoManage(this, 0, this)
                    .build();

            googleApiClient.connect();
        }else {
            requestGrantedPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    }

    public  boolean isPermissionsGranted(Context context, String[] grantPermissions) {
        Log.d("TEST", "checkPermission()");
        boolean accessGranted = true;
        if (grantPermissions == null || grantPermissions.length == 0) {
            accessGranted = false;
        } else {
            for (String permission : grantPermissions) {
                if (ContextCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    accessGranted = false;
                    break;
                }
            }
        }
        return accessGranted;
    }

    public  boolean requestGrantedPermissions(Context context, String[] permissions, int requestCode) {
        Log.d("TEST", "askPermission()");
        boolean requestPermission = true;
        if (!isPermissionsGranted(context, permissions)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(permissions, requestCode);
            } else {
                requestPermission = false;
            }
        } else {
            requestPermission = false;
        }
        return requestPermission;
    }

    private void  showMarkersAndZoom() {
        Log.d("TEST", "ShowMarkers and zoom");
        mMap.clear();
        if (mLastLocation != null) {
         myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 16), 200, null);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(myMarker != null)
            myMarker.remove();
        mLastLocation = location;
        myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
    }

    private void getBusesLcations(final String busNum, String busID){
        DatabaseReference ref = database.getReference("bus_location");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.getLocation(busID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if(location != null){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(busNum));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}


