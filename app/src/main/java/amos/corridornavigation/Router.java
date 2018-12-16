package amos.corridornavigation;

import android.widget.Toast;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Router {

    final static boolean DEBUG_MODE = true;

    private Marker destinationMarkerPosition;
    private LatLng destinationCoord;

    private DirectionsRoute currentRoute;

    public ArrayList<DirectionsRoute> directionsRoutes = new ArrayList<>();  //TODO: Only for DEBUG

    private NavigationMapRoute navigationMapRoute;

    public Router()
    {
    }


    public DirectionsRoute getCurrentRoute()
    {
        return currentRoute;
    }


    public void setDestinationMarkerPosition(MapContext context, LatLng point)
    {
        if (this.destinationMarkerPosition != null) {
            context.mapboxMap.removeMarker(this.destinationMarkerPosition);
        }
        destinationCoord = point;
        this.destinationMarkerPosition = context.mapboxMap.addMarker(new MarkerOptions()
                .position(destinationCoord)
        );
        if(context.originLocation != null) {
            Point destinationPosition = Point.fromLngLat(destinationCoord.getLongitude(), destinationCoord.getLatitude());
            Point originPosition = Point.fromLngLat(context.originLocation.getLongitude(), context.originLocation.getLatitude());
            getRoute(context, originPosition, destinationPosition);
        }
        else
        {
            Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    public void updateRoute(MapContext context)
    {
        if(destinationCoord != null)
        {
            this.setDestinationMarkerPosition(context, destinationCoord);
        }
    }

    private void getRoute(MapContext context, Point origin, Point destination) {

        NavigationRoute.builder(context)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Timber.d("Response code: %s", response.code());
                        if (response.body() == null) {
                            Timber.e("No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Timber.e("No routes found");
                            return;
                        }

                        if(DEBUG_MODE) {
                            directionsRoutes.add(response.body().routes().get(0));
                        }
                        else
                        {
                            currentRoute = response.body().routes().get(0);
                        }

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            int condition = 0;
                            if (condition == 0) {
                                navigationMapRoute = new NavigationMapRoute(null, context.mapView, context.mapboxMap, R.style.NavigationMapRouteGreen);
                            }
                            else if (condition == 1) {
                                navigationMapRoute = new NavigationMapRoute(null, context.mapView, context.mapboxMap, R.style.NavigationMapRouteYellow);
                            }
                            else {
                                navigationMapRoute = new NavigationMapRoute(null, context.mapView, context.mapboxMap, R.style.NavigationMapRouteRed);
                            }
                            //navigationMapRoute = new NavigationMapRoute(null, context.mapView, context.mapboxMap, R.style.NavigationMapRoute);
                        }

                        if(DEBUG_MODE) {
                            navigationMapRoute.addRoute(directionsRoutes.get(directionsRoutes.size()-1));
                        }
                        else
                        {
                            navigationMapRoute.addRoute(currentRoute);
                        }


                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
    }
}
