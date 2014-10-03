package com.kalpesh.googlemapdirectionapp.direction.finder;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.kalpesh.googlemapdirectionapp.R;
import com.kalpesh.googlemapdirectionapp.direction.app.AppController;
import com.kalpesh.googlemapdirectionapp.direction.factory.DirectionUrlFactory;
import com.kalpesh.googlemapdirectionapp.direction.model.Direction;
import com.kalpesh.googlemapdirectionapp.direction.model.DirectionApiReqConstants;
import com.kalpesh.googlemapdirectionapp.direction.model.gson.DirectionApiMapper;
import com.kalpesh.googlemapdirectionapp.direction.model.gson.Location;
import com.kalpesh.googlemapdirectionapp.direction.model.gson.RouteLag;
import com.kalpesh.googlemapdirectionapp.direction.model.gson.Routes;
import com.kalpesh.googlemapdirectionapp.direction.model.gson.Steps;
import com.kalpesh.googlemapdirectionapp.direction.utils.Utils;
import com.kalpesh.googlemapdirectionapp.utils.LogUtils;

import java.io.StringReader;
import java.util.List;

/**
 * Created by abc on 03-10-2014.
 */
public class DirectionFinder {

    public interface IDirectionListener {
        public void onDirectionFound(Direction direction);

        public void onError(String errorMsg);
    }

    private static final String TAG = DirectionFinder.class.getSimpleName();
    private String apiKey;
    private IDirectionListener directionListener;
    private Context context;


    public DirectionFinder(Context context, String apiKey) {
        this.apiKey = apiKey;
        this.context = context;
    }

    public void findDirection(LatLng originLatLng, LatLng destinationLatLng, IDirectionListener directionListener) {
        this.directionListener = directionListener;
        if (Utils.isNetworkAvailable(context)) {
            String url = DirectionUrlFactory.createDirectionUrl(originLatLng, destinationLatLng, apiKey);
            LogUtils.d(TAG, url);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    parseDirectionResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    DirectionFinder.this.directionListener.onError(context.getString(R.string.server_connection_error_msg));
                }
            });
            AppController.getInstance().addToRequestQueue(stringRequest);
        } else {
            directionListener.onError(context.getString(R.string.no_data_connection_msg));
        }
    }

    private void parseDirectionResponse(String directionJson) {
        Gson gson = new Gson();
        DirectionApiMapper directionApiMapper = gson.fromJson(new StringReader(directionJson), DirectionApiMapper.class);
        String status = directionApiMapper.getStatus();
        if (status.equals(DirectionApiReqConstants.RESPONSE_VALUE_OK)) {
            PolylineOptions routePolylineOptions = new PolylineOptions();
            String startAddress;
            String endAddress;
            LatLng startLocation;
            LatLng endLocation;
            List<Routes> routesList = directionApiMapper.getRoutesList();

            startAddress = routesList.get(0).getRouteLags().get(0).getStartAddress();
            Location location = routesList.get(0).getRouteLags().get(0).getStartLocation();
            startLocation = new LatLng(Double.parseDouble(location.getLattitude()),
                    Double.parseDouble(location.getLongitude()));

            endAddress = routesList.get(routesList.size() - 1).getRouteLags().
                    get(routesList.get(routesList.size() - 1).
                            getRouteLags().size() - 1).getEndAddress();
            location = routesList.get(routesList.size() - 1).
                    getRouteLags().get(routesList.get(routesList.size() - 1).getRouteLags().size() - 1).getEndLocation();
            endLocation = new LatLng(Double.parseDouble(location.getLattitude()),
                    Double.parseDouble(location.getLongitude()));

            for (Routes routes : routesList) {
                List<RouteLag> routeLagList = routes.getRouteLags();
                for (RouteLag routeLag : routeLagList) {
                    List<Steps> stepsList = routeLag.getStepsList();
                    for (Steps steps : stepsList) {
                        String polyLinePoints = steps.getPolyline().getPolylinePints();
                        List<LatLng> decodedPolyLineLatLng = Utils.decodePoly(polyLinePoints);
                        routePolylineOptions.addAll(decodedPolyLineLatLng);
                    }
                }
            }

            Direction direction = new Direction(startAddress, endAddress, startLocation, endLocation, routePolylineOptions);
            directionListener.onDirectionFound(direction);

        } else if (status.equals(DirectionApiReqConstants.RESPONSE_VALUE_INVALID_REQUEST)) {
            directionListener.onError(context.getString(R.string.response_invalid_request));
        } else if (status.equals(DirectionApiReqConstants.RESPONSE_VALUE_NOT_FOUND)) {
            directionListener.onError(context.getString(R.string.response_no_found_msg));
        } else if (status.equals(DirectionApiReqConstants.RESPONSE_VALUE_REQUEST_DENIED)) {
            directionListener.onError(context.getString(R.string.response_request_denied));
        } else if (status.equals(DirectionApiReqConstants.RESPONSE_VALUE_ZERO_RESULTS)) {
            directionListener.onError(context.getString(R.string.response_zero_result));
        } else if (status.equals(DirectionApiReqConstants.RESPONSE_VALUE_UNKNOWN_ERROR)) {
            directionListener.onError(context.getString(R.string.response_unknown_error));
        }
    }
}
