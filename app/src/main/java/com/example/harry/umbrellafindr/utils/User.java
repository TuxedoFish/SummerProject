package com.example.harry.umbrellafindr.utils;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.StringJoiner;

import javax.annotation.Nullable;

public class User {
    private String mFirstName, mEmail;
    private Long mAge;
    private String mimageURI;
    private Constants.Gender mGender;
    private String mUserID, mPartnerID;
    private GeoPoint mLocation;

    private boolean decisionToMake = false;

    private String filename = "null";

    private int mStatus = Constants.STATUS_ONLINE;
    private boolean sentRequest = false;

    public User(String userID, String firstname, String email, Long age, String imageURI, String gender, GeoPoint location) {
        setFirstName(firstname); setEmail(email); setAge(age); setImageURI(imageURI); setGender(gender); mUserID = userID; mLocation = location;
    }

    public void beginSearching(FirebaseFirestore db) {
        sentRequest = false;
        mPartnerID = null;
        setmStatus(db, Constants.STATUS_SEARCHING);
    }

    public boolean isDecisionToMake() { return decisionToMake; }
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

    public String getFileName(String mID, String oID) {
        int compare = mID.compareTo(oID);

        if(compare<0) {
            return mID.concat("_").concat(oID);
        } else {
            return oID.concat("_").concat(mID);
        }
    }

    public void updateInformation(@Nullable QuerySnapshot queryDocumentSnapshots, FirebaseFirestore db, Utilities utils) {
        List<DocumentSnapshot> results = queryDocumentSnapshots.getDocuments();

        boolean accepted = false;
        for(int i=0; i<results.size(); i++) {
            String OtherID = (String) results.get(i).getId();

            if (mStatus == Constants.STATUS_SEARCHING && !accepted) {
                //Noticed a request from a user
                db.collection("strollers").document(OtherID)
                        .collection("info").document("info").set(getResponse(mUserID, mUserID));
                accepted=true;
                setmStatus(db, Constants.STATUS_DECISION_A);
                //File will be located at the id of the other user
                mPartnerID = OtherID;
                haveMatched(db, utils, Constants.STATUS_DECISION_A, OtherID);
            } else if (mStatus==Constants.STATUS_PENDING && mPartnerID.equals(OtherID)) {
                //Both requested eachother so alphabetic combination of the two ids
                db.collection("strollers").document(OtherID)
                        .collection("info").document("info").set(getResponse(mUserID, mUserID));
                accepted=true;
                setmStatus(db, Constants.STATUS_DECISION_B);
                //Both requested eachother so file name is an alphabetic combination of the two
                mPartnerID = OtherID;
                haveMatched(db, utils, Constants.STATUS_DECISION_B, OtherID);
            } else {
                //Removing other requests
                db.collection("strollers").document(OtherID)
                        .collection("info").document("info").set(getResponse(Constants.REQUEST_FAILED, Constants.REQUEST_FAILED));
            }
            //delete requests
            db.collection("strollers").document(mUserID).collection("requests")
                .document(OtherID).delete();
        }
    }

    public Map<String, Object> getResponse(String partner_id, String file_loc) {
        Map<String, Object> data = new HashMap<>();

        data.put("partner_id", partner_id);
        data.put("file_loc", file_loc);

        return data;
    }

    public void search(final FirebaseFirestore db) {
        //Query database for nearby users where the distance is close and the status is 1 i.e. searching
        final Utilities utils = new Utilities();

        utils.isAnyRequests(db, mUserID, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    //requested
                    updateInformation(queryDocumentSnapshots, db, utils);
                } else {
                    if (mStatus == Constants.STATUS_SEARCHING) {
                        //no request found
                        Query query = utils.getUsersSearching(db, mLocation.getLatitude(), mLocation.getLongitude());
                        //check if there is any users currently online searching
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<DocumentSnapshot> result = task.getResult().getDocuments();

                                for (int i = 0; i < task.getResult().getDocuments().size() && !sentRequest; i++) {
                                    if (mStatus == Constants.STATUS_SEARCHING && !result.get(i).getId().equals(mUserID)) {
                                        final String potentialID = result.get(i).getId();

                                        sentRequest = true;
                                        setmStatus(db, Constants.STATUS_PENDING);

                                        mPartnerID = potentialID;
                                        utils.sendRequest(db, mPartnerID
                                                , mUserID);
                                    }
                                }
                            }
                        });
                    } else if (mStatus == Constants.STATUS_PENDING) {
                        utils.checkPartner(db, mPartnerID, new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                long status = (long)documentSnapshot.get("status");
                                Log.d("HELP", Long.toString(status));

                                if (mStatus == Constants.STATUS_PENDING) {
                                    if ((int)status!=(Constants.STATUS_SEARCHING) && (int)status!=(Constants.STATUS_PENDING)) {
                                        //Commence search again
                                        resetSearch(db);
                                        db.collection("strollers").document(mPartnerID).collection("requests")
                                                .document(mUserID).delete();
                                    }
                                }
                            }
                        });
                        utils.getResponseBack(db, mUserID, new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (mStatus == Constants.STATUS_PENDING) {
                                    String result = (String)documentSnapshot.get("partner_id");
                                    if(result!=null) {
                                        Log.d("HELP", result);

                                        if (!result.equals(Constants.REQUEST_NULL)) {
                                            if (result.equals(mPartnerID)) {
                                                //Both requested each other
                                                setmStatus(db, Constants.STATUS_DECISION_B);
                                                //In case other user hasnt been notified yet
                                                db.collection("strollers").document(mPartnerID)
                                                        .collection("info").document("info").set(getResponse(mUserID, mUserID));
                                                //Both requested so file is alphabetical combination of the two
                                                haveMatched(db, utils, Constants.STATUS_DECISION_B, mPartnerID);
                                            } else if (result.equals(Constants.REQUEST_FAILED)) {
                                                resetSearch(db);
                                            } else {
                                                //Request acknowledged by other user so file located at MY ID
                                                setmStatus(db, Constants.STATUS_DECISION_A);
                                                haveMatched(db, utils, Constants.STATUS_DECISION_A, mUserID);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void resetSearch(FirebaseFirestore db) {
        setmStatus(db, Constants.STATUS_SEARCHING);
        sentRequest = false;
        db.collection("strollers").document(mUserID).collection("info")
                .document("info").delete();
    }

    public void haveMatched(FirebaseFirestore db, Utilities utils, int type, String file_name) {
        db.collection("strollers").document(mUserID).collection("info")
                .document("info").delete();
        String temp_1 = "meetup_";

        if(type==Constants.STATUS_DECISION_A) {
            //One requested the other
            filename = temp_1.concat(file_name);

        } else {
            //Both requested each other
            String temp_2 = getFileName(mUserID, mPartnerID);

            filename = temp_1.concat(temp_2);
        }

        Log.d("SUCCESS", "Match - " + mUserID + " to " + mPartnerID + " at " + filename + " : " + type);

        if(mUserID.compareTo(mPartnerID)>0) {
            //Create a meet-up form to confirm
            utils.setUpDecisionForm(db, filename, mPartnerID, mUserID);
        }

        decisionToMake = true;
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
