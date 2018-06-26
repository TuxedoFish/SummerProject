package com.example.harry.umbrellafindr.utils;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class User {
    private String mFirstName, mEmail;
    private Long mAge;
    private String mimageURI;
    private Constants.Gender mGender;
    private String mUserID, mPartnerID;
    private GeoPoint mLocation;

    private int mStatus = Constants.STATUS_ONLINE;
    private boolean sentRequest = false;

    private EventListener mSendRequestListene = new EventListener() {
        @Override
        public void onEvent(@Nullable Object o, @Nullable FirebaseFirestoreException e) {

        }
    };

    public User(String userID, String firstname, String email, Long age, String imageURI, String gender, GeoPoint location) {
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

    public void search(final FirebaseFirestore db) {
        //Query database for nearby users where the distance is close and the status is 1 i.e. searching
        final Utilities utils = new Utilities();

        utils.isRequests(db, mUserID, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()) {
                            //Already requested
                            if(mStatus==Constants.STATUS_SEARCHING) {
                                setmStatus(db, Constants.STATUS_DECISION_A);
                            }
                        } else {
                            //no request found
                            Query query = utils.getUsersSearching(db, mLocation.getLatitude(), mLocation.getLongitude());
                            //check if there is any users currently online searching
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<DocumentSnapshot> result = task.getResult().getDocuments();

                                    for(int i=0; i<task.getResult().getDocuments().size() && !sentRequest; i++) {
                                        if(mStatus==Constants.STATUS_SEARCHING && !result.get(i).getId().equals(mUserID)) {
                                            Log.d("helper", "I tried to find a match");
                                            final String potentialID = result.get(i).getId();

                                            utils.isRequests(db, result.get(i).getId(), new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                    if(!documentSnapshot.exists() && !sentRequest) {
                                                        sentRequest = true;
                                                        setmStatus(db, Constants.STATUS_DECISION_B);
                                                        mPartnerID = potentialID;
                                                        utils.sendRequest(db, mPartnerID
                                                                , mUserID);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
        });
    }

    public String getMyId() {
        return mUserID;
    }

    public String getFirstName() { return mFirstName; }
    public void setFirstName(String firstName) { this.mFirstName = firstName; }

    public String getEmail() { return mEmail; }
    public void setEmail(String email) { this.mEmail = email; }

    public Long getAge() { return mAge; }
    public void setAge(Long age) { this.mAge = age; }

    public String getImageURI() { return mimageURI; }
    public void setImageURI(String imageURI) { this.mimageURI = imageURI; }

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
