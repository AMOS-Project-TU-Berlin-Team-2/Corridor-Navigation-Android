package amos.corridornavigation.navigationview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;

import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import java.util.ArrayList;

import amos.corridornavigation.R;
import amos.corridornavigation.Router;
import amos.corridornavigation.UpdateAlgorithm;

public class CorridorNavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback {

    NavigationView navigationView;
    private Router locationMarker = new Router();
    private int delay = 20000;
    private Handler handler;
    public static boolean backgroundInstance = false;

    DirectionsRoute mainDriectionRoute;
    public ArrayList<DirectionsRoute> alternativeDirectionsRoutes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corridor_navigation);

        handler = new Handler();

        mainDriectionRoute = (DirectionsRoute) getIntent().getSerializableExtra("DirectionsRoute_0");
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

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
        backgroundInstance = false;
        handler.postDelayed(new Runnable() {
            public void run() {
                System.out.println("Executing the update algorithm...");
                /*
                System.out.println("Deleting the old alternatives...");
                // Remove all current alternatives
                for (int i=1; i < routes.size(); i += 1) {
                    intent.removeExtra("DirectionsRoute_"+i);
                }
                System.out.println("Calculating and adding the new alternatives...");
                sendBroadcast(intentNavigation);
                for (int i=0; i < routes.size(); i += 1) {
                    intent.putExtra("DirectionsRoute_"+i, routes.get(i));
                }
                */
                if (locationMarker == null) {
                    System.out.println("...but router is null :(");
                }
                else {
                    MapboxMap map = navigationView.retrieveNavigationMapboxMap().retrieveMap();
                    LatLng originPoint = new LatLng();
                    originPoint.setLatitude(map.getLocationComponent().getLastKnownLocation().getLatitude());
                    originPoint.setLongitude(map.getLocationComponent().getLastKnownLocation().getLongitude());
                    Point currentPoint = Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude());

                    System.out.println("Current position: "+currentPoint.toString());
                    System.out.println("Size of the marker list: "+map.getMarkers().size());
                    if (map.getMarkers().size() > 0) {
                        for (int i=0; i < map.getMarkers().size(); i+=1) {
                            System.out.println("Marker "+i+": "+map.getMarkers().get(i).toString());
                        }
                    }

                    originPoint = map.getMarkers().get(0).getPosition();
                    Point destinationPoint = Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude());


                    locationMarker.getRoute(CorridorNavigationActivity.this, currentPoint , destinationPoint);
                    ArrayList<DirectionsRoute> routes = (ArrayList) locationMarker.currentRoute;
                    if (routes != null) {
                        System.out.println("Calculated new routes: "+routes.toString());
                        System.out.println("Total number of routes: "+routes.size());
                        navigationView.retrieveNavigationMapboxMap().drawRoutes(routes);
                    }
                    else {
                        System.out.println("Calculated routes are null");
                    }
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
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
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onNavigationReady(boolean isRunning) {

        MapboxNavigationOptions navigationOptions = MapboxNavigationOptions.builder().defaultMilestonesEnabled(true).build();

        NavigationViewOptions options = NavigationViewOptions.builder()
                .directionsRoute(mainDriectionRoute)
                .navigationOptions(navigationOptions)
                .shouldSimulateRoute(true)

                .milestoneEventListener((routeProgress, instruction, milestone) -> {

                })
                .build();


        navigationView.startNavigation(options);
        navigationView.retrieveNavigationMapboxMap().showAlternativeRoutes(true);

        navigationView.retrieveNavigationMapboxMap().drawRoutes(alternativeDirectionsRoutes); // Print all Alt-Routes

        handler.postDelayed(new Runnable() {
            public void run() {
                System.out.println("Executing the update algorithm...");
                /*
                System.out.println("Deleting the old alternatives...");
                // Remove all current alternatives
                for (int i=1; i < routes.size(); i += 1) {
                    intent.removeExtra("DirectionsRoute_"+i);
                }
                System.out.println("Calculating and adding the new alternatives...");
                sendBroadcast(intentNavigation);
                for (int i=0; i < routes.size(); i += 1) {
                    intent.putExtra("DirectionsRoute_"+i, routes.get(i));
                }
                */
                if (locationMarker == null) {
                    System.out.println("...but router is null :(");
                }
                else {
                    MapboxMap map = navigationView.retrieveNavigationMapboxMap().retrieveMap();
                    LatLng originPoint = new LatLng();
                    originPoint.setLatitude(map.getLocationComponent().getLastKnownLocation().getLatitude());
                    originPoint.setLongitude(map.getLocationComponent().getLastKnownLocation().getLongitude());
                    Point currentPoint = Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude());

                    System.out.println("Current position: "+currentPoint.toString());
                    System.out.println("Size of the marker list: "+map.getMarkers().size());
                    if (map.getMarkers().size() > 0) {
                        for (int i=0; i < map.getMarkers().size(); i+=1) {
                            System.out.println("Marker "+i+": "+map.getMarkers().get(i).toString());
                        }
                    }

                    originPoint = map.getMarkers().get(0).getPosition();
                    Point destinationPoint = Point.fromLngLat(originPoint.getLongitude(), originPoint.getLatitude());


                    locationMarker.getRoute(CorridorNavigationActivity.this, currentPoint , destinationPoint);
                    ArrayList<DirectionsRoute> routes = (ArrayList) locationMarker.currentRoute;
                    if (routes != null) {
                        System.out.println("Calculated new routes: "+routes.toString());
                        System.out.println("Total number of routes: "+routes.size());
                        navigationView.retrieveNavigationMapboxMap().drawRoutes(routes);
                    }
                    else {
                        System.out.println("Calculated routes are null");
                    }
                }
                handler.postDelayed(this, delay);
            }
        }, delay);

        Context c = this;

        //navigationView.retrieveNavigationMapboxMap().removeRoute(); // Removes all Alt-Routes


        /*MapView mapView = navigationView.findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.navigationMapView);
        MapboxMap mapboxMap = navigationView.retrieveNavigationMapboxMap().retrieveMap();

        NavigationMapRoute yellow_router = new NavigationMapRoute(null, mapView,mapboxMap, R.style.NavigationMapRouteYellow);
        yellow_router.addRoutes(alternativeDirectionsRoutes);
        yellow_router.showAlternativeRoutes(true);*/

        //NavigationMapRoute red_router = new NavigationMapRoute(null, mapView,mapboxMap, R.style.NavigationMapRouteRed);
        //red_router.addRoute(alternativeDirectionsRoutes.get(2));

    }
    public void onClickNaviPause(View view){
        handler.removeCallbacksAndMessages(null);
        Intent intent=new Intent();//CorridorNavigationActivity.this, amos.corridornavigation.MainActivity.class);
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
