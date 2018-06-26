package com.example.harry.umbrellafindr.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.harry.umbrellafindr.R;
import com.example.harry.umbrellafindr.setup.LogInActivity;
import com.example.harry.umbrellafindr.utils.Constants;
import com.example.harry.umbrellafindr.utils.User;
import com.example.harry.umbrellafindr.utils.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseLogic {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public DatabaseLogic() {
        db = FirebaseFirestore.getInstance();
        if(!db.getFirestoreSettings().areTimestampsInSnapshotsEnabled()) {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        }
        mAuth = FirebaseAuth.getInstance();
    }
    public void makeUseOfNewLocation(double longitude, double latitude, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        DocumentReference docref = db.collection("strollers").document(mAuth.getCurrentUser().getUid());
        GeoPoint user_loc = new GeoPoint(latitude, longitude);

        getUser(mAuth.getCurrentUser().getUid(), user_loc, onCompleteListener);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("location", user_loc);
        locationData.put("status", Constants.STATUS_ONLINE);

        docref.set(locationData);

        //ONLY FOR DEBUGGING
        populateArea(longitude, latitude, onCompleteListener);
    }

    public void addMarkers(final Context context, final GoogleMap googleMap, double mlatitude, double mlongitude) {
        final Utilities utils = new Utilities();
        Query query = utils.getUsersNearby(db, mlatitude, mlongitude);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    List<DocumentSnapshot> results = task.getResult().getDocuments();
                    if(task.getResult().size()!=0) {
                        BitmapDescriptor img = utils.bitmapDescriptorFromVector(context, R.drawable.ic_coffee_image_white);
                        //query worked
                        //there was people in the vicinity of you
                        for(int i=0; i<results.size(); i++) {
                            if(results.get(i).getId()!=mAuth.getUid()) {
                                DocumentSnapshot snapshot = results.get(i);
                                GeoPoint user_loc = (GeoPoint) snapshot.get("location");

                                //add a marker for each person in your vicinity
                                LatLng user = new LatLng(user_loc.getLatitude(), user_loc.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(user).icon(img));
                            }
                        }
                    } else {
                        //nobody could be found near you
                        Log.d("notify", "nobody could be found in your area");
                    }
                } else {
                    //query failed
                    Log.d("notify", "nobody could be found in your area");
                }
            }
        });
    }

    public void attemptLogIn(final LogInActivity activity, String email, String password) {
        boolean result = false;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("notify", "log in : success");
                            if(checkIfEmailVerified()) { activity.completeLogIn(); } else { activity.signOut(); }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("error", "log in : failure", task.getException());
                            Toast.makeText(activity, "Log in failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void getUser(String userId, GeoPoint location, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        DocumentReference docref = db.collection("users_info").document(userId);
        docref.get().addOnCompleteListener(onCompleteListener);
    }

    public String getCurrentUserId() {
        return mAuth.getCurrentUser().getUid();
    }

    public void signOut() {
        mAuth.signOut();
    }

    private boolean checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.isEmailVerified()) { return true; } else { return false; }
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser()!=null;
    }

    /*
    DEBUGGING DELETE FOR FINAL VERSION:

    LINE: populateArea(longitude, latitude); above
    addFakeUsers()
    getFakeUser()
    clearFakeUsers()
    populateArea()
     */
    public void addFakeUsers() {
        for (int i = 0; i < Constants.NUMBER_FAKE_USERS; i++) {
            db.collection("users_info").document("user_" + i).set(getFakeUser("user_"+i));
        }
    }
    private Map<String, Object> getFakeUser(String mName) {
        Map<String, Object> mFakeData = new HashMap<>();

        mFakeData.put("first_name", mName);
        mFakeData.put("age", -1);
        mFakeData.put("gender", "UNKNOWN");
        mFakeData.put("bio", "please enter a bio");
        mFakeData.put("profile_picture", "icon-profile.png");
        mFakeData.put("email", "someone@ucl.ac.uk");

        return mFakeData;
    }
    public void clearFakeUsers() {
        for(int i=0; i<Constants.NUMBER_FAKE_USERS; i++) {
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
    public void populateArea(double longitude, double latitude, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        //all in miles
        double degree_lat = 69;
        double degree_long = Math.cos(Math.toRadians(latitude)) * 69.172;

        double theta = 0;
        double radius = 0;

        double x = 0;
        double y = 0;
        double longitude_user = 0;
        double latitude_user = 0;

        ArrayList<User> users = new ArrayList<User>();

        for(int i = 0; i< Constants.NUMBER_FAKE_USERS; i++) {
            //FUNCTION THAT POPULATES A CIRCLE UNIFORMLY
            theta = Math.PI*2 * Math.random();
            radius = Constants.QUERY_RADIUS * Math.sqrt(Math.random());

            x=radius*Math.cos(theta);
            y=radius*Math.sin(theta);

            longitude_user=y/degree_long;
            latitude_user=x/degree_lat;

            //SET LOCATION IN DATABASE
            DocumentReference docref = db.collection("strollers").document("user_"+i);
            GeoPoint user_loc = new GeoPoint(latitude+latitude_user, longitude+longitude_user);

            getUser("user_"+i, user_loc, onCompleteListener);

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("location", user_loc);
            locationData.put("status", Constants.STATUS_ONLINE);

            docref.set(locationData);
        }
    }
}
