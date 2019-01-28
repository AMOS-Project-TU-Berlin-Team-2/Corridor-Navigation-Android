package amos.corridornavigation;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;
import java.util.ArrayList;

import amos.corridornavigation.navigationview.CorridorNavigationActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Router {

    private Marker destinationMarkerPosition;
    protected LatLng destinationCoord;

    public List<DirectionsRoute> currentRoute;
    private NavigationMapRoute navigationMapRoute;

    public CorridorNavigationActivity act;

    public Router()
    {
    }

    // TODO: Perhaps we could add a separate function instead of using this boolean parameter
    public void setDestinationMarkerPosition(MapContext context, LatLng point, Boolean calculateRoute)
    {
        if (this.destinationMarkerPosition != null) {
            context.mapboxMap.removeMarker(this.destinationMarkerPosition);
        }
        destinationCoord = point;
        this.destinationMarkerPosition = context.mapboxMap.addMarker(new MarkerOptions()
                .position(destinationCoord)
        );
        if (calculateRoute) {
            if (context.originLocation != null) {
                Point destinationPosition = Point.fromLngLat(destinationCoord.getLongitude(), destinationCoord.getLatitude());
                Point originPosition = Point.fromLngLat(context.originLocation.getLongitude(), context.originLocation.getLatitude());
                getRoute(context, originPosition, destinationPosition);
            } else {
                Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateRoute(MapContext context)
    {
        if(destinationCoord != null)
        {
            this.setDestinationMarkerPosition(context, destinationCoord, true);
        }
    }

    private void getRoute(MapContext context, Point origin, Point destination) {

        MapboxDirections.Builder directionsBuilder = MapboxDirections.builder();

        NavigationRoute.builder(context)
                .baseUrl("https://245.ip-51-68-139.eu/osrm/")
                .accessToken(Mapbox.getAccessToken())
                .profile("driving")
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        System.out.println(response.code());
                        if (response.body() == null) {
                            Timber.e("No routes found, make sure you set the right user and access token.");
                            return; } else if (response.body().routes().size() < 1) {
                            Timber.e("No routes found");
                            return;
                        }

                        currentRoute = response.body().routes();

                        /*

                        Log.d("test1","Calculated new routes: "+currentRoute.toString());
                        Log.d("test1","Total number of routes: "+currentRoute.size());
                        for (int i=0; i < currentRoute.size(); i+=1) {
                            for (int j=0; j < currentRoute.get(i).legs().size(); j+=1) {
                                for (int k=0; k < currentRoute.get(i).legs().get(j).steps().size(); k+=1) {
                                    Log.d("test1", "Route "+i+", Leg "+j+", Step "+k+" = "+currentRoute.get(i).legs().get(j).steps().get(k).toString());
                                }
                            }
                        }
                        */

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, context.mapView, context.mapboxMap, R.style.NavigationMapRouteGreen);
                        }
                        navigationMapRoute.addRoutes(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
        System.out.println("test1: Initially: Calculated route to destination: "+destination.toString());
    }

    public void getRoute(Context context, Point origin, Point destination) {

        if (context != null) {
            System.out.println("context = " + context.toString());
        }
        else {
            System.out.println("Context is null -.-");
        }
        MapboxDirections.Builder directionsBuilder = MapboxDirections.builder();

        NavigationRoute.builder(context)
                .baseUrl("https://245.ip-51-68-139.eu/osrm/")
                .accessToken(Mapbox.getAccessToken())
                .profile("driving")
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        System.out.println(response.code());
                        if (response.body() == null) {
                            Timber.e("No routes found, make sure you set the right user and access token.");
                            return; } else if (response.body().routes().size() < 1) {
                            Timber.e("No routes found");
                            return;
                        }

                        currentRoute = response.body().routes();
                        act.drawNewRoutes();

                        /*
                        Log.d("test1","Calculated new routes: "+currentRoute.toString());
                        Log.d("test1","Total number of routes: "+currentRoute.size());
                        for (int i=0; i < currentRoute.size(); i+=1) {
                            for (int j=0; j < currentRoute.get(i).legs().size(); j+=1) {
                                for (int k=0; k < currentRoute.get(i).legs().get(j).steps().size(); k+=1) {
                                    Log.d("test1", "Route "+i+", Leg "+j+", Step "+k+" = "+currentRoute.get(i).legs().get(j).steps().get(k).toString());
                                }
                            }
                        }
                        */
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
        //System.out.println("test1: On Update: Calculated route to destination: "+destination.toString());
    }
}
