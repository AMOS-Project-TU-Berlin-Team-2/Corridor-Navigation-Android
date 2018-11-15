package amos.corridornavigation;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

// classes needed to add location layer

public class MainActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener
{
    private static final String TAG = "DirectionsActivity";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
    private Location originLocation;
    // variables for adding a marker
    private Marker destinationMarker;
    private LatLng destinationCoord;
    // variables for calculating and drawing a route

    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(this);
        enableLocationPlugin();
    }


    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();
            initializeLocationComponent();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.addLocationEngineListener(this);
        locationEngine.activate();
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationComponent()
    {
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(this,locationEngine);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.NORMAL);

    }

    public void actionButtonPressed(View view) {
        if(originLocation != null) {
            this.setCameraPosition(originLocation);
        }
        else
        {
            Toast.makeText(this, R.string.user_location_not_available, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point){
        if (destinationMarker != null) {
            mapboxMap.removeMarker(destinationMarker);
        }
        destinationCoord = point;
        destinationMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(destinationCoord)
        );
        if(originLocation != null) {
            Point destinationPosition = Point.fromLngLat(destinationCoord.getLongitude(), destinationCoord.getLatitude());
            Point originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
            getRoute(originPosition, destinationPosition);
        }
        else
        {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
        originLocation = locationEngine.getLastLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            originLocation = location;
            setCameraPosition(location);
        }
        if(destinationCoord != null)
        {
            this.onMapClick(destinationCoord);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
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

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
    }

    private void setCameraPosition(Location location)
    {
        LatLng latLng = new LatLng();
        latLng.setLatitude(location.getLatitude());
        latLng.setLongitude(location.getLongitude());
        latLng.setAltitude(location.getAltitude());
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13.0));
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        if(locationEngine != null)
        {
            locationEngine.requestLocationUpdates();
        }
        if(locationComponent != null)
        {
            locationComponent.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
        if(locationComponent != null)
        {
            locationComponent.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(locationEngine != null)
        {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void onSearchButtonClicked(View view) {
        EditText editText = (EditText) findViewById(R.id.main_searchbar_input);
        String address = editText.getText().toString();
        MapboxGeocoding client;
        try {
            if(originLocation != null) {
                client = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .proximity(Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude()))
                        .query(address)
                        .build();
            }
            else
            {
                client = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .query(address)
                        .build();
            }
            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                    List<CarmenFeature> results = response.body().features();
                    if(results.size() > 1)
                    {

                        LatLng latLng = new LatLng();
                        latLng.setLatitude(results.get(0).center().latitude());
                        latLng.setLongitude(results.get(0).center().longitude());
                        onMapClick(latLng);
                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13.0));
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: " + throwable.getMessage());
                }
            });

        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: " + servicesException.toString());
            servicesException.printStackTrace();
        }

    }
}
