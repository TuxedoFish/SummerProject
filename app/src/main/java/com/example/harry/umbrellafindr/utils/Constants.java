package com.example.harry.umbrellafindr.utils;

public final class Constants {
    //DEBUG CONSTANTS PLEASE REMOVE
    public static final int NUMBER_FAKE_USERS = 10;
    //IN MILES
    //IN FINAL VERSION SHOULD BE EDITABLE
    public static final double QUERY_RADIUS = 0.5;

    //permission codes
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    //activity results
    public static final int RESULT_LOAD_IMAGE_TAKEN = 1;
    public static final int RESULT_LOAD_IMAGE_GALLERY = 2;

    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_SEARCHING = 2;
    public static final int STATUS_PENDING = 3;
    public static final int STATUS_DECISION_A = 4;
    public static final int STATUS_DECISION_B = 5;
    public static final int STATUS_ERROR = 6;

    public static final int RESULT_NO_USERS = 7;
    public static final int RESULT_REQUESTED = 8;
    public static final int RESULT_SEND_REQUEST = 9;

    public static final int NO_REPLY = 10;
    public static final int POSITIVE_REPLY = 11;
    public static final int NEGATIVE_REPLY = 12;

    //gender for use in user class
    public enum Gender {
        MALE,
        FEMALE,
        UNKNOWN
    }
}

