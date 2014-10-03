package com.kalpesh.googlemapdirectionapp.utils;

import android.app.Activity;
import android.app.Dialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by abc on 02-10-2014.
 */
public class GoogleMapUtils {

    private static final int GOOGLE_PLAY_SERVICE_REQUEST_CODE = 9001;
    public static final LatLng DEFAULT_LOCATION = new LatLng(25.0738579,55.2298444);
    public static final int DEFAULT_ZOOM = 9;

    public static boolean isGoogleServiceOkIfNotShowDialog(Activity activity){
        int response  = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if(response == ConnectionResult.SUCCESS){
            return true;
        }else if(GooglePlayServicesUtil.isUserRecoverableError(response)){
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(response, activity, GOOGLE_PLAY_SERVICE_REQUEST_CODE);
            dialog.show();
        }
        return false;
    }

    public static MarkerOptions createMarker(String title, LatLng latLng, float hue){
        return new MarkerOptions().title(title).position(latLng).icon(BitmapDescriptorFactory.defaultMarker(hue));
    }

    public static MarkerOptions createMarker(String title, LatLng latLng){
        return new MarkerOptions().title(title).position(latLng);
    }
}
