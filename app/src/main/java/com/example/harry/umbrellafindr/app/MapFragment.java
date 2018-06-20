package com.example.harry.umbrellafindr.app;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.setup.RegisterActivity;
import com.example.harry.umbrellafindr.utils.Constants;
import com.example.harry.umbrellafindr.utils.User;
import com.example.harry.umbrellafindr.utils.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment{
    MapView mMapView;
    private GoogleMap googleMap;

    private DatabaseLogic databaseLogic;
    private Utilities utils;

    private Double mlongitude;
    private Double mlatitude;

    private LocationManager mLocationManager;

    private User mUser;
    private ArrayList<User> fakeUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(getActivity(),
                    permissions, Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        utils = new Utilities();

        databaseLogic = new DatabaseLogic();
        databaseLogic.addFakeUsers();
        //add position information
        handleLocation();

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

                databaseLogic.addMarkers(getActivity(), googleMap, mlatitude, mlongitude);

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mlatitude, mlongitude)).zoom(14).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
            }
        });

        return rootView;
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
                ArrayList<User> users = databaseLogic.makeUseOfNewLocation(location.getLongitude(), location.getLatitude());
                updateUserData(users);
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
        ArrayList<User> users = databaseLogic.makeUseOfNewLocation(-0.1268, 51.5407);
        updateUserData(users);
        mlatitude = 51.5407;
        mlongitude = -0.1268;
    }

    public void searchLogic() {
        int RESULT_CODE = Constants.STATUS_ERROR;

        if(mUser.getmStatus() == Constants.STATUS_SEARCHING) {
            RESULT_CODE = mUser.search(databaseLogic.getDb());
            if(RESULT_CODE == Constants.STATUS_DECISION_A) {
                utils.sendRequest(databaseLogic.getDb(), mUser.getPotentialPartnerId(), mUser.getMyId());
                //start up decision activity FOR RESULT
                //SENT REQUEST

            }
            if(RESULT_CODE == Constants.STATUS_DECISION_B) {
                //start up decision activity FOR RESULT
                //RECIEVED REQUEST

            }
        }
        for(int i=0; i<fakeUsers.size(); i++) {
            if (fakeUsers.get(i).getmStatus() == Constants.STATUS_SEARCHING) {
                RESULT_CODE = fakeUsers.get(i).search(databaseLogic.getDb());
                if(RESULT_CODE == Constants.STATUS_DECISION_A) {
                    utils.sendRequest(databaseLogic.getDb(), fakeUsers.get(i).getPotentialPartnerId(), fakeUsers.get(i).getMyId());
                    //start up decision activity FOR RESULT
                    //SENT REQUEST

                }
                if(RESULT_CODE == Constants.STATUS_DECISION_B) {
                    //start up decision activity FOR RESULT
                    //RECIEVED REQUEST

                }
            }
        }
    }

    public void updateUserData(ArrayList<User> users) {
        mUser = users.get(0);
        fakeUsers.clear();
        for(int i=1; i<users.size(); i++) {
            fakeUsers.add(users.get(i));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        databaseLogic.clearFakeUsers();
    }
}
