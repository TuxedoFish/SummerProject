package com.example.harry.umbrellafindr.app;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.setup.RegisterActivity;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment{
    MapView mMapView;
    private GoogleMap googleMap;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final int NUMBER_FAKE_USERS = 10;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    //IN MILES
    //IN FINAL VERSION SHOULD BE EDITABLE
    private static final double QUERY_RADIUS = 0.5;

    private Double mlongitude;
    private Double mlatitude;

    private LocationManager mLocationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(getActivity(),
                    permissions, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //add position information
        handleLocation();

        //inject fake data
        //DEBUGGING, DELETE FOR FINAL VERSION
        if(mAuth.getCurrentUser()!=null) {
            Log.d("failure", "log in: success");

            for (int i = 0; i < NUMBER_FAKE_USERS; i++) {
                db.collection("users_info").document("user_" + i).set(getFakeUser("user_"+i));
            }
        } else {
            Log.d("failure", "log in: failure");
        }

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch(SecurityException  e) {
                    Log.println(Log.DEBUG,"err","location err : " + e);
                }

                Query query = getUsersNearby(mlatitude, mlongitude);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            List<DocumentSnapshot> results = task.getResult().getDocuments();
                            if(task.getResult().size()!=0) {
                                //query worked
                                //there was people in the vicinity of you
                                for(int i=0; i<results.size(); i++) {
                                    if(results.get(i).getId()!=mAuth.getUid()) {
                                        DocumentSnapshot snapshot = results.get(i);
                                        GeoPoint user_loc = (GeoPoint) snapshot.get("location");

                                        //add a marker for each person in your vicinity
                                        LatLng user = new LatLng(user_loc.getLatitude(), user_loc.getLongitude());
                                        googleMap.addMarker(new MarkerOptions().position(user));
                                    }
                                }
                            } else {
                                //nobody could be found near you
                                Log.d("log help", "nobody could be found in your area");
                            }
                        } else {
                            //query failed
                            Log.d("log help", "nobody could be found in your area");
                        }
                    }
                });

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mlatitude, mlongitude)).zoom(14).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
            }
        });

        return rootView;
    }

    public void makeUseOfNewLocation(double longitude, double latitude) {
        DocumentReference docref = db.collection("strollers").document(mAuth.getCurrentUser().getUid());
        GeoPoint user_loc = new GeoPoint(latitude, longitude);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("location", user_loc);

        docref.set(locationData);

        //ONLY FOR DEBUGGING
        populateArea(longitude, latitude);
    }

    /*
    ONLY HERE WHILE DEBUGGING TAKE OUT FOR FINAL VERSION
     */
    public void populateArea(double longitude, double latitude) {
        //all in miles
        double degree_lat = 69;
        double degree_long = Math.cos(Math.toRadians(latitude)) * 69.172;

        double theta = 0;
        double radius = 0;

        double x = 0;
        double y = 0;
        double longitude_user = 0;
        double latitude_user = 0;

        for(int i=0; i<NUMBER_FAKE_USERS; i++) {
            //FUNCTION THAT POPULATES A CIRCLE UNIFORMLY
            theta = Math.PI*2 * Math.random();
            radius = QUERY_RADIUS * Math.sqrt(Math.random());

            x=radius*Math.cos(theta);
            y=radius*Math.sin(theta);

            longitude_user=y/degree_long;
            latitude_user=x/degree_lat;

            //SET LOCATION IN DATABASE
            DocumentReference docref = db.collection("strollers").document("user_"+i);
            GeoPoint user_loc = new GeoPoint(latitude+latitude_user, longitude+longitude_user);

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("location", user_loc);

            docref.set(locationData);
        }
    }

    public Query getUsersNearby(double latitude, double longitude) {
        //all in miles
        double degree_lat = 69;
        double degree_long = Math.cos(Math.toRadians(latitude)) * 69.172;

        //bounds of query
        double lower_long = longitude - (QUERY_RADIUS/degree_long);
        double upper_long = longitude + (QUERY_RADIUS/degree_long);

        double lower_lat = latitude - (QUERY_RADIUS/degree_lat);
        double upper_lat = latitude + (QUERY_RADIUS/degree_lat);

        GeoPoint lower_point = new GeoPoint(lower_lat, lower_long);
        GeoPoint upper_point = new GeoPoint(upper_lat, upper_long);

        CollectionReference colref = db.collection("strollers");
        Query query = colref.whereGreaterThan("location", lower_point).whereLessThan("location", upper_point);

        return query;
    }

    public void handleLocation() {
        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mlatitude =location.getLatitude();
                mlongitude = location.getLongitude();
                makeUseOfNewLocation(location.getLongitude(), location.getLatitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {
            Log.d("permission", "no permission for accessing location");
        }

        //HARDCODED FOR USE ON EMULATOR.
        makeUseOfNewLocation(-0.1268, 51.5407);
        mlatitude = 51.5407;
        mlongitude = -0.1268;
    }

    private Map<String, Object> getFakeUser(String mName) {
        Map<String, Object> mFakeData = new HashMap<>();

        mFakeData.put("first_name", mName);
        mFakeData.put("age", "unknown");
        mFakeData.put("gender", "unknown");
        mFakeData.put("bio", "please enter a bio");
        mFakeData.put("profile_picture", "somewhere");

        return mFakeData;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //delete fake data
        for(int i=0; i<NUMBER_FAKE_USERS; i++) {
            db.collection("users_info").document("user_"+i).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("help","user_x was deleted");
                    }
                }
            });
        }
    }
}
