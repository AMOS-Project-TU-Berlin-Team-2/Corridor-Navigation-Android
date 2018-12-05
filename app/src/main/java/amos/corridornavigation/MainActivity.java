package amos.corridornavigation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.StepManeuver;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import amos.corridornavigation.navigationview.CorridorNavigationActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

// classes needed to add location layer

public class MainActivity extends MapContext {
    public ArrayAdapter<String> adapter;
    public AutoCompleteTextView autoCompleteTextView;
    private ArrayList<String> previous_autocomplete_results;
    private AutoCompleteTextView addressSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        ImageButton naviButton =  findViewById(R.id.button);
        naviButton.setVisibility(View.INVISIBLE);
        initMapView(savedInstanceState);

        addressSearchBar = (AutoCompleteTextView)
                findViewById(R.id.main_searchbar_input);

        this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {""});
        this.adapter.setNotifyOnChange(true);
        addressSearchBar.setAdapter(adapter);

        addressSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    onSearchStart(s);
                }
            }
        });
    }

    public void actionButtonPressed(View view) {
        if (super.originLocation != null) {
            this.setCameraPosition(super.originLocation);
        } else {
            Toast.makeText(this, R.string.user_location_not_available, Toast.LENGTH_LONG).show();
        }
    }

    public void onSearchStart(CharSequence s) {
        String addressPart = s.toString();
        getSuggestions(addressPart);
    }

    private void getSuggestions(String addressPart) {
        MapboxGeocoding client;
        ArrayList<String> autocomplete_results = new ArrayList<>();
        try {

            if (super.originLocation != null) {
                client = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .proximity(Point.fromLngLat(super.originLocation.getLongitude(), super.originLocation.getLatitude()))
                        .query(addressPart)
                        .autocomplete(true)
                        .build();
            } else {
                client = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .query(addressPart)
                        .autocomplete(true)
                        .build();
            }

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                    List<CarmenFeature> results = response.body().features();
                    if(results.size() > 1)
                    {
                        for (CarmenFeature result : results) {
                            try {
                                autocomplete_results.add(result.placeName()); //.substring(0, result.placeName().indexOf(","))
                            } catch (Exception e) {
                                System.err.println(e.getStackTrace());
                            }
                        }
//                        add_previous_results(autocomplete_results, addressPart);
                        String[] stockArr = new String[autocomplete_results.size()];
                        stockArr = autocomplete_results.toArray(stockArr);
                        adapter.clear();
                        adapter.addAll(stockArr);
                        adapter.notifyDataSetChanged();
                        addressSearchBar.setAdapter(adapter);
                        addressSearchBar.showDropDown();
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

    private void add_previous_results(ArrayList<String> autocomplete_results, String adressPart) {
        for (String result : previous_autocomplete_results) {
            if (result.toLowerCase().startsWith(adressPart.toLowerCase()) && autocomplete_results.size() <= 5 && !autocomplete_results.contains(result)) {
                autocomplete_results.add(result);
            }
        }
        System.err.println("Yay !");
    }

    public void onSearchButtonClicked(View view) {

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(this.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        EditText editText = findViewById(R.id.main_searchbar_input);
        String address = editText.getText().toString();
        MapboxGeocoding client;
        try {
            if (super.originLocation != null) {
                client = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .proximity(Point.fromLngLat(super.originLocation.getLongitude(), super.originLocation.getLatitude()))
                        .query(address)
                        .build();
            } else {
                client = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .query(address)
                        .build();
            }
            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                    List<CarmenFeature> results = response.body().features();
                    if (results.size() > 1) {

                        LatLng latLng = new LatLng();
                        latLng.setLatitude(results.get(0).center().latitude());
                        latLng.setLongitude(results.get(0).center().longitude());
                        onMapClick(latLng);
                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13.0)); // mapboxMap came from MapContext
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

    public void onRouteButtonClicked(View view) {
        super.onRouteButtonClicked();
    }


    public void onNavigateButtonClicked(View view) {

        // ArrayList which contains all the routes that should be drawn when the CorridorNavigationActivity starts
        ArrayList<DirectionsRoute> routes = new ArrayList<>();
        // The first element of routes should be the Main route (aka. the fastest route)
        if (super.locationMarker.currentRoute == null || super.locationMarker.currentRoute.size() <= 0) {
            Toast.makeText(this, R.string.user_no_route_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        DirectionsRoute mainRoute = super.locationMarker.currentRoute.get(0);
        routes.add(mainRoute);

        // Closes the Navigation Activity which has remained in the Background
        Intent intentNavigation = new Intent("finish_activity");
        sendBroadcast(intentNavigation);

        Intent intent = new Intent(MainActivity.this, CorridorNavigationActivity.class);

        // Adds the routes to the intent. So we can use these in #CorrdorNavigationActivity
        for(int i = 0; i < routes.size(); i++)
        {
            intent.putExtra("DirectionsRoute_"+i,routes.get(i));
        }

        startActivity(intent);
    }

    public void onBackToNaviClicked(View view){
        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setClassName(this,"amos.corridornavigation.navigationview.CorridorNavigationActivity");
        startActivity(intent);
    }

    public void onHideButtonClicked(View view){

        LinearLayout search_navi_ui = findViewById(R.id.search_navi_ui);
        ImageButton naviButton =  findViewById(R.id.button);
        ImageButton hideButton = (ImageButton)view;

        if (search_navi_ui.getVisibility()== View.VISIBLE){
            search_navi_ui.setVisibility(View.INVISIBLE);
            naviButton.setVisibility(View.INVISIBLE);
            hideButton.setImageResource(R.drawable.eye_icon);
        }
        else {
            search_navi_ui.setVisibility(View.VISIBLE);
            // only if there is running in background a navigation activity it is possible to go
            // back to it, so the button is set to visible
            if(CorridorNavigationActivity.backgroundInstance == true)
                naviButton.setVisibility(View.VISIBLE);
            hideButton.setImageResource(R.drawable.closed_eye_icon);
        }
    }
}
