package com.example.harry.umbrellafindr.utils;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String mFirstName, mEmail;
    private int mAge;
    private Uri mimageURI;
    private Constants.Gender mGender;
    private String mUserID, mPartnerID;
    private GeoPoint mLocation;
    private int mStatus = Constants.STATUS_ONLINE;

    public User(String userID, String firstname, String email, int age, Uri imageURI, String gender, GeoPoint location) {
        setFirstName(firstname); setEmail(email); setAge(age); setImageURI(imageURI); setGender(gender); mUserID = userID; mLocation = location;
    }

    public void beginSearching(FirebaseFirestore db) {
        setmStatus(db, Constants.STATUS_SEARCHING);
    }

    public String getPotentialPartnerId() {
        return mPartnerID;
    }

    public void setmStatus(FirebaseFirestore db, int new_status) {
        this.mStatus = new_status;

        Map<String, Object> data = new HashMap<>();
        data.put("location", mLocation);
        data.put("status", new_status);

        db.collection("strollers").document(mUserID).set(data);
    }
    public int getmStatus() {
        return mStatus;
    }

    public int search(FirebaseFirestore db) {
        //Query database for nearby users where the distance is close and the status is 1 i.e. searching
        Utilities utils = new Utilities();

        if(utils.isRequests(db, mUserID)) {
            //request found
            setmStatus(db, Constants.STATUS_DECISION_A);
            return Constants.RESULT_REQUESTED;
        } else {
            //no request found
            Query query = utils.getUsersSearching(db, mLocation.getLatitude(), mLocation.getLongitude());
            //check if there is any users currently online searching
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    mStatus = Constants.STATUS_PENDING;
                    mPartnerID = task.getResult().getDocuments().get(0).getId();
                }
            });
        }

        if(mStatus == Constants.STATUS_PENDING) {
            setmStatus(db, mStatus);
            //Check if in the time it took another user has sent you or your potential partner a request
            if(utils.isRequests(db, mUserID)) {
                //request received
                setmStatus(db, Constants.STATUS_DECISION_B);
                //go to decision tree page
                return Constants.RESULT_REQUESTED;
            } else if (utils.isRequests(db, mPartnerID)) {
                //other user has received a request hence will return to searching again
                setmStatus(db, Constants.STATUS_SEARCHING);
                mPartnerID = null;
            } else {
                //free to send a request to the other user
                setmStatus(db, Constants.STATUS_DECISION_A);
                //go to decision tree page
                return Constants.RESULT_SEND_REQUEST;
            }
        }

        return Constants.RESULT_NO_USERS;
    }

    public String getMyId() {
        return mUserID;
    }

    public String getFirstName() { return mFirstName; }
    public void setFirstName(String firstName) { this.mFirstName = firstName; }

    public String getEmail() { return mEmail; }
    public void setEmail(String email) { this.mEmail = email; }

    public int getAge() { return mAge; }
    public void setAge(int age) { this.mAge = age; }

    public Uri getImageURI() { return mimageURI; }
    public void setImageURI(Uri imageURI) { this.mimageURI = imageURI; }

    public Constants.Gender getGender() { return mGender; }
    public void setGender(String gender) {
        if(gender.equals("MALE")) {
            this.mGender = Constants.Gender.MALE;
        } else if(gender.equals("FEMALE")) {
            this.mGender = Constants.Gender.FEMALE;
        } else {
            this.mGender = Constants.Gender.UNKNOWN;
        }
    }
}
