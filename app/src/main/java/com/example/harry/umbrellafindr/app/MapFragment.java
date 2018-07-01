package com.example.harry.umbrellafindr.app;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.utils.Constants;
import com.example.harry.umbrellafindr.utils.DatabaseLogic;
import com.example.harry.umbrellafindr.utils.User;
import com.example.harry.umbrellafindr.utils.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

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

    private boolean deciding = false;

    public Communicator mCommunicator;
    public interface Communicator {
        public void sendRequest(String user_id);
    }

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
        fakeUsers = new ArrayList<User>();

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mCommunicator = (HubActivity) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public void beginSearch() {
        mUser.beginSearching(databaseLogic.getDb());

        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                if(!deciding) {
                    searchLogic();
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
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
                databaseLogic.makeUseOfNewLocation(location.getLongitude(), location.getLatitude(), new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        addUserData(task);
                    }
                });
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
        databaseLogic.makeUseOfNewLocation(-0.1268, 51.5407, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                addUserData(task);
            }
        });
        mlatitude = 51.5407;
        mlongitude = -0.1268;
    }

    public void addUserData(@NonNull Task<DocumentSnapshot> task) {
        if(task.isSuccessful()) {
            User user = null;
            DocumentSnapshot result = task.getResult();

            if (result.exists()) {
                user = new User(result.getId(), (String)result.get("first_name"), (String)result.get("email"),
                   (Long)result.get("age"), (String)result.get("profile_picture"), (String)result.get("gender"), new GeoPoint(mlatitude, mlongitude));
            } else {
                Log.d("error", "no such user found");
            }
            if(user!=null) {
                if(user.getMyId().equals(databaseLogic.getCurrentUserId())) {
                    mUser = user;
                } else {
                    user.beginSearching(databaseLogic.getDb());
                    fakeUsers.add(user);
                }
            }
        } else {
            Log.d("error", "Database query looking for user failed");
        }
    }

    public void searchLogic() {
        if(mUser.getmStatus() == Constants.STATUS_SEARCHING  || mUser.getmStatus() == Constants.STATUS_PENDING) {
            mUser.search(databaseLogic.getDb());
        }
        if(mUser.isDecisionToMake()) {
            //start up decision activity FOR RESULT
            mCommunicator.sendRequest(mUser.getPotentialPartnerId());
            deciding=true;
        }

        for(int i=0; i<fakeUsers.size(); i++) {
            if (fakeUsers.get(i).getmStatus() == Constants.STATUS_SEARCHING  || fakeUsers.get(i).getmStatus() == Constants.STATUS_PENDING) {
                fakeUsers.get(i).search(databaseLogic.getDb());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        databaseLogic.clearFakeUsers();
    }
}
