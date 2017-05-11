package arab_open_university.com.bususer;

import android.*;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location mLastLocation;
    Marker myMarker;
    FirebaseDatabase database;


    Spinner spinnerChooseBus;
    List<String> busesName;
    List<BusStation> buses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* shows map fragment in the activity and asunc download the maps on the activity */
        final MapFragment mapFragment = new MapFragment();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction().replace(R.id.map_view, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);


        // Inflate the layout for this fragment
        spinnerChooseBus = (Spinner) findViewById(R.id.spinner_chooseBus);


        busesName = new ArrayList<>();
        buses = new ArrayList<>();

        /* if the user wants to filter to track only one bus or all the buses ,
         * he choose from the drop-down list and if he chooses the bus the application will get location of the bus form the
          * firebase database and locations of bus stations  and draws them on the map */
        spinnerChooseBus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,final int i, long l) {

                    DatabaseReference myRef = database.getReference("buses_Info");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mMap.clear();
                            if (mLastLocation != null)
                                myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.guidemarker)));
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if(i != 0) {
                                    if (snapshot.getKey().equals(busesName.get(i))) {
                                        BusStation bus = snapshot.getValue(BusStation.class);
                                        for(int i = 0; i < bus.getBusStationsLat().size(); i++){
                                            mMap.addMarker(new MarkerOptions().position(new LatLng(bus.getBusStationsLat().get(i), bus.getBusStationsLong().get(i))).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop24)).title("Station Name: " + bus.getBusStationsNames().get(i)));
                                        }
                                        for (String id : bus.getBusesIDs()) {
                                            getBusesLcations(snapshot.getKey(), id);
                                        }

                                    }
                                }else {
                                    BusStation bus = snapshot.getValue(BusStation.class);
                                    for(int i = 0; i < bus.getBusStationsLat().size(); i++){
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(bus.getBusStationsLat().get(i), bus.getBusStationsLong().get(i))).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop24)).title("Station Name: " + bus.getBusStationsNames().get(i)));
                                    }
                                    for (String id : bus.getBusesIDs()) {
                                        getBusesLcations(snapshot.getKey(), id);
                                    }
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Intialize firebase database
        database = FirebaseDatabase.getInstance();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu filer
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // If user press settings menu he will open BusEstimation Activity so he can add
            case R.id.settings:
                Intent intent = new Intent(this, BusEstimationActiviy.class);
                Bundle extras = new Bundle();
                List<String> buses_numbers = busesName;
                buses_numbers.remove(0);
                extras.putStringArrayList("buses_numbers", ((ArrayList<String>) buses_numbers));
                extras.putSerializable("buses", (Serializable) buses);
                intent.putExtras(extras);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //CallBack after user accept or reject permissions worked for android 6.0 and upper only
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case 101: {
                /* check if user accepts Location permission  if yes set my location enabled true and request
                 * location from google services */
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
        /* called after connected to google location services and request  location updates every 60 seconds
         * and get last konown location and show me marker and zoom to my location
          * and check if user enable location or not if not enabled request from user to enable location
           * and get all buses locations from firebase databse and add their locations on the map and
           * locations of the bus stations */
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
                busesName.clear();
                busesName.add("All Buses");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    BusStation bus  = snapshot.getValue(BusStation.class);
                    bus.setBusNumber(snapshot.getKey());
                    buses.add(bus);
                    busesName.add(snapshot.getKey());
                    for(int i = 0; i < bus.getBusStationsLat().size(); i++){
                        mMap.addMarker(new MarkerOptions().position(new LatLng(bus.getBusStationsLat().get(i), bus.getBusStationsLong().get(i))).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop24)).title("Station Name: " + bus.getBusStationsNames().get(i)));
                    }
                    for(String id : bus.getBusesIDs()){
                        getBusesLcations(snapshot.getKey(), id);
                    }
                }

                if(busesName.size() > 0){
                    spinnerChooseBus.setVisibility(View.VISIBLE);
                    spinnerChooseBus.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, busesName));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /* called after downloading map  and drawing it on the activity
         * check if user accept location permission on android 6.0 and heigher
          * if no request permission
          * if yes allow my location on map and show button my location on google map and connect to google location services  */
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
        /* this method is called to check if user grant specific permissions for android 6.0 and heigher  */
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
        /* the method request permission on android 6.0 and heigher */
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
        /* the method is called to show my location marker and zoom to my location */
        mMap.clear();
        if (mLastLocation != null) {
         myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.guidemarker)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 16), 200, null);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        /* this method is called when location changed so it created new maker to my location */
        if(myMarker != null)
            myMarker.remove();
        mLastLocation = location;
        myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.guidemarker)));
    }

    private void getBusesLcations(final String busNum, String busID){
        /* get bus location from firebase database by bus number and bus id then add their markers to the map  */
        DatabaseReference ref = database.getReference("bus_location");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.getLocation(busID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if(location != null){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("Bus Number: " + busNum).icon(BitmapDescriptorFactory.fromResource(R.drawable.busmarker)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public  static Intent makeNotificationIntent(Context geofenceService, String msg)
    {
        Log.d("Test",msg);
        return new Intent(geofenceService,MainActivity.class);
    }
}


