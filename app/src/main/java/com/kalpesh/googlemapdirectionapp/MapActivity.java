package com.kalpesh.googlemapdirectionapp;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kalpesh.googlemapdirectionapp.direction.finder.DirectionFinder;
import com.kalpesh.googlemapdirectionapp.direction.model.Direction;
import com.kalpesh.googlemapdirectionapp.model.AppConstants;
import com.kalpesh.googlemapdirectionapp.utils.AppMsgUtils;
import com.kalpesh.googlemapdirectionapp.utils.GoogleMapUtils;

public class MapActivity extends ActionBarActivity {

    private GoogleMap mGoogleMap;
    private Marker startMarker;
    private Marker endMarker;
    private static final String TAG = MapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!GoogleMapUtils.isGoogleServiceOkIfNotShowDialog(this)) {
            setContentView(R.layout.layout_no_map);
        } else {
            setContentView(R.layout.activity_map);
            initGoogleMap();
            takeUserToDefaultLocation();
            AppMsgUtils.showInfotMsg(this, getString(R.string.select_start_position_msg));
        }
    }

    //Take User on top of Dubai
    private void takeUserToDefaultLocation() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(GoogleMapUtils.DEFAULT_LOCATION, GoogleMapUtils.DEFAULT_ZOOM);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    private GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (startMarker == null) {
                startMarker = mGoogleMap.addMarker(GoogleMapUtils.createMarker(getString(R.string.start_position),
                        latLng, BitmapDescriptorFactory.HUE_GREEN));
                AppMsgUtils.showInfotMsg(MapActivity.this, getString(R.string.select_end_position_msg));
            } else if (endMarker == null) {
                endMarker = mGoogleMap.addMarker(GoogleMapUtils.createMarker(getString(R.string.end_postion),
                        latLng, BitmapDescriptorFactory.HUE_RED));
                findDirection();
                // Start fetching Direction
            }
        }
    };

    private void findDirection() {
        final ProgressDialog progressDialog = new ProgressDialog(MapActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.finding_location_msg));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        DirectionFinder directionFinder = new DirectionFinder(MapActivity.this, AppConstants.GOOGLE_DIRECTION_API_KEY);
        directionFinder.findDirection(startMarker.getPosition(),
                endMarker.getPosition(), new DirectionFinder.IDirectionListener() {
                    @Override
                    public void onDirectionFound(Direction direction) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        plotRoute(direction);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        AppMsgUtils.showAlertMsg(MapActivity.this, errorMsg);
                    }
                });
    }

    private void plotRoute(Direction direction) {
        startMarker.remove();
        endMarker.remove();
        MarkerOptions startMarkerOption = GoogleMapUtils.createMarker(direction.getStartAddress(), direction.getStartLocation(), BitmapDescriptorFactory.HUE_GREEN);
        startMarker = mGoogleMap.addMarker(startMarkerOption);

        MarkerOptions endMarkerOption = GoogleMapUtils.createMarker(direction.getEndAddress(), direction.getEndLocation(), BitmapDescriptorFactory.HUE_RED);
        endMarker = mGoogleMap.addMarker(endMarkerOption);

        PolylineOptions routePolylineOptions = direction.getRoute();
        routePolylineOptions.color(Color.BLUE);
        mGoogleMap.addPolyline(routePolylineOptions);
    }

    private void initGoogleMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mGoogleMap = supportMapFragment.getMap();
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        mGoogleMap.setOnMapClickListener(mapClickListener);
    }
}
