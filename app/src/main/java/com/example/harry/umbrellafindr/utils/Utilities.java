package com.example.harry.umbrellafindr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.example.harry.umbrellafindr.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import com.example.harry.umbrellafindr.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utilities {
    public boolean hasImageCaptureBug() {
        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE);
    }


    public Query getUsersNearby(FirebaseFirestore db, double latitude, double longitude) {
        GeoPoint[] bounds = getLocationBounds(latitude, longitude);

        CollectionReference colref = db.collection("strollers");
        Query query = colref.whereGreaterThan("location", bounds[0]).whereLessThan("location", bounds[1]);

        return query;
    }

    public Query getUsersSearching(FirebaseFirestore db, double latitude, double longitude) {
        GeoPoint[] bounds = getLocationBounds(latitude, longitude);

        CollectionReference colref = db.collection("strollers");
        Query query = colref.whereGreaterThan("location", bounds[0]).whereLessThan("location", bounds[1]).
                whereEqualTo("status", Constants.STATUS_SEARCHING);

        return query;
    }

    public boolean isRequests(FirebaseFirestore db, String userID) {
        DocumentReference docref = db.collection("meetups").document(userID);

        if (docref.get().getResult().exists()) {
            return true;
        } else {
            return false;
        }
    }

    public void sendRequest(FirebaseFirestore db, String targetUserID, String userID) {
        DocumentReference docref = db.collection("strollers").document(targetUserID);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requester", userID);
        requestData.put("user_A", Constants.NO_REPLY);
        requestData.put("user_B", Constants.NO_REPLY);

        docref.set(requestData);
    }

    private GeoPoint[] getLocationBounds(double latitude, double longitude) {
        //all in miles
        double degree_lat = 69;
        double degree_long = Math.cos(Math.toRadians(latitude)) * 69.172;

        //bounds of query
        double lower_long = longitude - (Constants.QUERY_RADIUS/degree_long);
        double upper_long = longitude + (Constants.QUERY_RADIUS/degree_long);

        double lower_lat = latitude - (Constants.QUERY_RADIUS/degree_lat);
        double upper_lat = latitude + (Constants.QUERY_RADIUS/degree_lat);

        GeoPoint lower_point = new GeoPoint(lower_lat, lower_long);
        GeoPoint upper_point = new GeoPoint(upper_lat, upper_long);

        return new GeoPoint[]{lower_point, upper_point};
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK);
        String m_currentDateAndTime = m_sdf.format(new Date());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, m_currentDateAndTime,
                "profile picture");

        return Uri.parse(path);
    }

    public BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        double scaling_factor = 0.8f;
        //Setup background of the marker
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_marker_background_48dp);
        background.setBounds(0, 0, (int)(background.getIntrinsicWidth()*scaling_factor),(int)(background.getIntrinsicHeight()*scaling_factor));
        //Draw the icon of the day
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds((int)(30*scaling_factor),  (int)(20*scaling_factor),
                (int)((vectorDrawable.getIntrinsicWidth() + 30)*scaling_factor), (int)((vectorDrawable.getIntrinsicHeight() + 20)*scaling_factor));
        //Create a new bitmap to draw onto
        Bitmap bitmap = Bitmap.createBitmap((int)(scaling_factor*background.getIntrinsicWidth()),
                (int)(scaling_factor*background.getIntrinsicHeight()), Bitmap.Config.ARGB_8888);
        //Create a canvas to enable us to draw onto
        Canvas canvas = new Canvas(bitmap);
        //Draw the 2 images ontop of eachother
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        //Return the output
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
