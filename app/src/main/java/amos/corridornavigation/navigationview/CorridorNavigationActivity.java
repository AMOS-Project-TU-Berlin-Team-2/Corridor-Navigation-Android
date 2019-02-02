package amos.corridornavigation.navigationview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;

import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

import amos.corridornavigation.R;
import amos.corridornavigation.Router;

public class CorridorNavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback {

    NavigationView navigationView;
    private Router locationMarker;
    public static boolean backgroundInstance = false;
    public Boolean simulateRoute = false;
    public Boolean triggerUpdate = false;

    DirectionsRoute mainDirectionRoute;
    public ArrayList<DirectionsRoute> alternativeDirectionsRoutes = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corridor_navigation);

        locationMarker  = new Router();

        mainDirectionRoute = (DirectionsRoute) getIntent().getSerializableExtra("DirectionsRoute_0");
        //The MainDirectionRoute must be the first in the ArrayList

        DirectionsRoute route;
        int c = 0;
        while((route = (DirectionsRoute) getIntent().getSerializableExtra("DirectionsRoute_"+c)) != null)
        {
            c++;
            alternativeDirectionsRoutes.add(route);
        }
        // Receiver needs to be registered to be able to receive massage later from main Activity
        registerReceiver(broadcastReceiver, new IntentFilter("finish_activity"));

        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
        backgroundInstance = false;
        locationMarker  = new Router();
    }

    // Update the routes drawn in the navigation view.
    public void drawNewRoutes() {
        ArrayList<DirectionsRoute> routes = (ArrayList) locationMarker.currentRoute;
        if (routes != null) {
            navigationView.stopNavigation();
            
            mainDirectionRoute = routes.get(0);
            alternativeDirectionsRoutes.clear();
            navigationView.retrieveNavigationMapboxMap().removeRoute();
            for (int i=0; i<routes.size(); i+=1) {
                alternativeDirectionsRoutes.add(routes.get(i));
            }

            List<Marker> markerList = navigationView.retrieveNavigationMapboxMap().retrieveMap().getMarkers();
            while (markerList.size() > 1) {
                navigationView.retrieveNavigationMapboxMap().retrieveMap().removeMarker(markerList.remove(0));
            }

            startNavigation();

            /**
             * Important: The main route has to be the first element of the alternativeDirectionsRoutes-list
             * Otherwise the routes disappear immediately after drawing.
             * Additionally, it seems like the only way to really get rid of the primary black route is
             * to stop the navigation and restart it. Otherwise it keeps overwriting the new routes.
             */
            navigationView.retrieveNavigationMapboxMap().drawRoutes(alternativeDirectionsRoutes); // Print all Alt-Routes
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
        locationMarker = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
        locationMarker = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
        locationMarker = null;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onNavigationReady(boolean isRunning) {

        simulateRoute = false;

        AlertDialog.Builder builder1 = new AlertDialog.Builder(CorridorNavigationActivity.this);
        builder1.setMessage("Do you want to start a demo mode of the navigation?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CorridorNavigationActivity.this.simulateRoute = true;
                        startNavigation();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startNavigation();
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @SuppressWarnings("MissingPermission")
    private Point getUserLocation(MapboxMap map) {
        Location lastLocation = map.getLocationComponent().getLastKnownLocation();//getLocationEngine().getLastLocation();
        LatLng originPoint = new LatLng();
        originPoint.setLatitude(lastLocation.getLatitude());
        originPoint.setLongitude(lastLocation.getLongitude());
        originPoint.setAltitude(lastLocation.getAltitude());
        return Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude(), originPoint.getAltitude());
    }

    private void startNavigation() {
        MapboxNavigationOptions navigationOptions = MapboxNavigationOptions.builder().defaultMilestonesEnabled(true).build();

        NavigationViewOptions options = NavigationViewOptions.builder()
                .directionsRoute(mainDirectionRoute)
                .navigationOptions(navigationOptions)
                .shouldSimulateRoute(simulateRoute)
                .progressChangeListener(new ProgressChangeListener() {
                    @SuppressWarnings("MissingPermission")
                    @Override
                    public void onProgressChange(Location location, RouteProgress routeProgress) {

                        MapboxMap map = null;
                        Point upcomingIntersection = null;
                        try {
                            map = navigationView.retrieveNavigationMapboxMap().retrieveMap();
                        } catch (Exception e) {
                            System.err.println("unable to retrieve map : " + e.getMessage());
                        }
                        try {
                            upcomingIntersection = routeProgress.currentLegProgress().currentStepProgress().upcomingIntersection().location();
                        } catch (Exception e) {
                            System.err.println("error while retrieving next intersection location : " + e.getMessage());
                        }
                        assert map != null;
                        assert upcomingIntersection != null;

                        Point currentPoint = getUserLocation(map);

                        // Obviously a mistake in location calculations, so do not execute an update
                        // Tests showed that occasionally the mapbox internal '.getLastKnownLocation()' returns [0.0, 0.0]
                        if (Math.abs(currentPoint.longitude()) < 0.01 && Math.abs(currentPoint.latitude()) < 0.01) {
                            return;
                        }

                        double distanceToNextIntersection = TurfMeasurement.distance(currentPoint, upcomingIntersection, TurfConstants.UNIT_METERS);
                        Log.d("Update Mechanism", "Remaining distance to next intersection = " + distanceToNextIntersection);
                        if (distanceToNextIntersection < 50) {
                            if (CorridorNavigationActivity.this.triggerUpdate) {
                                Log.d("Update Mechanism", "Triggered the update");
                                CorridorNavigationActivity.this.triggerUpdate = false;
                                updateMechanism();
                            }
                        }
                        else {
                            CorridorNavigationActivity.this.triggerUpdate = true;
                        }
                    }
                })

                .milestoneEventListener((routeProgress, instruction, milestone) -> {

                })
                .build();


        navigationView.startNavigation(options);
        navigationView.retrieveNavigationMapboxMap().showAlternativeRoutes(true);

        navigationView.retrieveNavigationMapboxMap().drawRoutes(alternativeDirectionsRoutes); // Print all Alt-Routes
    }

    @SuppressWarnings("MissingPermission")
    public void updateMechanism() {
        locationMarker.act = this;
        MapboxMap map = navigationView.retrieveNavigationMapboxMap().retrieveMap();

        try {
            Location lastLocation = map.getLocationComponent().getLastKnownLocation();
            LatLng originPoint = new LatLng();
            originPoint.setLatitude(lastLocation.getLatitude());
            originPoint.setLongitude(lastLocation.getLongitude());
            originPoint.setAltitude(lastLocation.getAltitude());
            Point currentPoint = Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude(), originPoint.getAltitude());

            // Obviously a mistake in location calculations, so do not execute an update
            // Tests showed that occasionally the mapbox internal '.getLastKnownLocation()' returns [0.0, 0.0]
            if (Math.abs(currentPoint.longitude()) < 0.01 && Math.abs(currentPoint.latitude()) < 0.01) {
                return;
            }

            originPoint = map.getMarkers().get(0).getPosition();
            Point destinationPoint = Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude(), originPoint.getAltitude());

            locationMarker.getRoute(CorridorNavigationActivity.this, currentPoint, destinationPoint);

        } catch (NullPointerException e) {

        }
    }

    // Pause the current navigation and return to the main activity without the previously shown routes.
    public void onClickNaviPause(View view){
        locationMarker = null;
        Intent intent=new Intent();
        intent.setClassName(this,"amos.corridornavigation.MainActivity");
        intent.putExtra("naviIsPaused",true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        backgroundInstance = true;
    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                finish();
            }
        }
    };
}