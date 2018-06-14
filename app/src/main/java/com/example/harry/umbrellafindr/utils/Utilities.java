package com.example.harry.umbrellafindr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK);
        String m_currentDateAndTime = m_sdf.format(new Date());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, m_currentDateAndTime,
                "profile picture");

        return Uri.parse(path);
    }
}
