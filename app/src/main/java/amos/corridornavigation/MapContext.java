package amos.corridornavigation;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

public class MapContext extends AppCompatActivity implements LocationEngineListener, PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {


    protected MapView mapView;

    protected MapboxMap mapboxMap;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;

    protected Location originLocation;

    protected Router locationMarker;


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
        locationMarker.setDestinationMarkerPosition(this, point);
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
}
