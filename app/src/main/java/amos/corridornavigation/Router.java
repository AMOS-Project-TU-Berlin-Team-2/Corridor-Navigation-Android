package amos.corridornavigation;

import android.content.Context;
import android.util.Log;
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

import java.util.List;

import amos.corridornavigation.navigationview.CorridorNavigationActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Router {

    private Marker destinationMarkerPosition;
    protected LatLng destinationCoord;
    private Toast toast;

    public List<DirectionsRoute> currentRoute;
    private NavigationMapRoute navigationMapRoute;

    public CorridorNavigationActivity act;

    public Router()
    {
    }

    /**
     * This function adds and sets the destination marker position on the map. Depending on the boolean
     * parameter, this function will automatically trigger the route calculation to the destination.
     * @param context
     * @param point : the destination location
     * @param calculateRoute : boolean parameter to toggle automated route calculation or just showing
     *                       the target address
     */
    public void setDestinationMarkerPosition(MapContext context, LatLng point, Boolean calculateRoute, Boolean showUpdateMessage)
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
                if (showUpdateMessage) {
                    toast = Toast.makeText(context, R.string.user_route_loading, Toast.LENGTH_SHORT);
                    toast.show();
                }
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
            this.setDestinationMarkerPosition(context, destinationCoord, true, false);
        }
    }

    /**
     * The actual trigger to calculate a route.
     * Note that this is an asynchronous call, meaning that the results will appear somewhen in the future.
     * Due to that, a toast is added to indicate that a route is being calculated. It will be removed
     * once the results have arrived.
     *
     * To be called from the main activity / anything apart of the navigation view.
     *
     * @param context
     * @param origin
     * @param destination
     */
    private void getRoute(MapContext context, Point origin, Point destination) {

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
                        toast.cancel();
                        System.out.println(response.code());
                        if (response.body() == null) {
                            Timber.e("No routes found, make sure you set the right user and access token.");
                            return; } else if (response.body().routes().size() < 1) {
                            Timber.e("No routes found");
                            return;
                        }

                        currentRoute = response.body().routes();

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
    }

    /**
     * Exactly the same as the previous method, but for being used by the navigation view context.
     * @param context
     * @param origin
     * @param destination
     */
    public void getRoute(Context context, Point origin, Point destination) {

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
                        Call the navigation activity and re-draw the routes.
                        This call is necessary as the calculations happen asynchronously, meaning the app would
                        stop working until the routes are calculated otherwise.
                         */
                        act.drawNewRoutes();
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
    }
}
