package amos.corridornavigation;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;

import java.util.List;
import java.util.Locale;

import amos.corridornavigation.navigationview.CorridorNavigationActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

//public class MapContext extends AppCompatActivity implements LocationEngineListener, PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {
public class MapContext extends AppCompatActivity implements LocationEngineListener, PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener{


    protected MapView mapView;

    protected MapboxMap mapboxMap;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;

    protected Location originLocation;

    protected Router locationMarker;

    private MapboxGeocoding client = null;
    private LatLng destinationPoint;    // To be used for calculating the route when pressing the route button
    private TextView textInCardView;    // To avoid complicated access to the text view within the card view


    protected void initMapView(Bundle savedInstanceState) {
        locationMarker = new Router();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }


    protected void setCameraPosition(Location location)
    {
        LatLng latLng = new LatLng();
        latLng.setLatitude(location.getLatitude());
        latLng.setLongitude(location.getLongitude());
        latLng.setAltitude(location.getAltitude());
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13.0));
    }

    @Override
    public void onMapClick(@NonNull LatLng point){

        //locationMarker.setDestinationMarkerPosition(this, point);
        destinationPoint = point;
        CardView cardView = (CardView)findViewById(R.id.card_view);
        cardView.setVisibility(View.VISIBLE);
        ViewGroup viewGroup = ((ViewGroup) cardView.getChildAt(0));
        textInCardView = (TextView) viewGroup.getChildAt(2);
        makeGeocodeSearch(point);
        locationMarker.setDestinationMarkerPosition(this, destinationPoint, false);
    }

    public void onRouteButtonClicked() {
        locationMarker.setDestinationMarkerPosition(this, destinationPoint, true);
    }

    private void makeGeocodeSearch(LatLng latLng) {
        try {
            // Build a Mapbox geocoding request
            client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.access_token))
                    .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call,
                                       Response<GeocodingResponse> response) {
                    List<CarmenFeature> results = response.body().features();
                    if (results.size() > 0) {
                        // Get the first Feature from the successful geocoding response
                        CarmenFeature feature = results.get(0);
                        animateCameraToNewPosition(latLng);
                        textInCardView.setText(feature.placeName());
                    } else {
                        Toast.makeText(MapContext.this, "No results found.",
                                Toast.LENGTH_SHORT).show();
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

    private void animateCameraToNewPosition(LatLng latLng) {
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13)
                        .build()), 1500);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng point){

        LinearLayout search_navi_ui = (LinearLayout) findViewById(R.id.search_navi_ui);
        FloatingActionButton hideButton = findViewById(R.id.hide_button);
        FloatingActionButton compassButton = findViewById(R.id.floatingActionButton2);
        ImageButton naviButton =  findViewById(R.id.button);

        if (search_navi_ui.getVisibility()== View.VISIBLE){
            search_navi_ui.setVisibility(View.INVISIBLE);
            naviButton.setVisibility(View.INVISIBLE);
            hideButton.hide();
            compassButton.hide();
        }
        else {
            search_navi_ui.setVisibility(View.VISIBLE);
            if(CorridorNavigationActivity.backgroundInstance == true)
                naviButton.setVisibility(View.VISIBLE);
            hideButton.show();
            compassButton.show();
        }
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
        locationMarker.updateRoute(this);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
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

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(this);
        mapboxMap.addOnMapLongClickListener(this);
        enableLocationPlugin();
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
        ImageButton naviButton =  findViewById(R.id.button);
        if(CorridorNavigationActivity.backgroundInstance == true){
            naviButton.setVisibility(View.VISIBLE);
        }else{
            naviButton.setVisibility(View.INVISIBLE);
        }
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
        if (client != null) {
            client.cancelCall();
        }
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
}
