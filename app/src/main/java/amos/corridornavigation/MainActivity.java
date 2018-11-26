package amos.corridornavigation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

// classes needed to add location layer

public class MainActivity extends MapContext {
    public static String[] countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        initMapView(savedInstanceState);

        final AutoCompleteTextView addressSearchBar = (AutoCompleteTextView)
                findViewById(R.id.main_searchbar_input);

        addressSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onSearchStart(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        String[] dropdownSuggestions = getSuggestions(addressPart);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.main_searchbar_input);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dropdownSuggestions);
        autoCompleteTextView.setAdapter(adapter);
    }

    // TOTO-make getSuggestion return list of suggested addresses, now it just returns
    // hardcoded list of countries for demoing and testing.
    private String[] getSuggestions(String addressPart) {
        return new String[]{"Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Anguilla", "Antigua; Barbuda",
                "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh",
                "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia &amp; Herzegovina",
                "Botswana", "Brazil", "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia",
                "Cameroon", "Cape Verde", "Cayman Islands", "Chad", "Chile", "China", "Colombia", "Congo", "Cook Islands",
                "Costa Rica", "Cote D Ivoire", "Croatia", "Cruise Ship", "Cuba", "Cyprus", "Czech Republic", "Denmark",
                "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea",
                "Estonia", "Ethiopia", "Falkland Islands", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia",
                "French West Indies", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland",
                "Grenada", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea Bissau", "Guyana", "Haiti", "Honduras",
                "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel",
                "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kuwait", "Kyrgyz Republic", "Laos",
                "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau",
                "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Mauritania", "Mauritius",
                "Mexico", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Namibia",
                "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger",
                "Nigeria", "Norway", "Oman", "Pakistan", "Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru",
                "Philippines", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russia", "Rwanda",
                "Saint Pierre &amp; Miquelon", "Samoa", "San Marino", "Satellite", "Saudi Arabia", "Senegal", "Serbia",
                "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "South Africa", "South Korea", "Spain",
                "Sri Lanka", "St Kitts &amp; Nevis", "St Lucia", "St Vincent", "St. Lucia", "Sudan", "Suriname", "Swaziland",
                "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor L'Este", "Togo", "Tonga",
                "Trinidad &amp; Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks &amp; Caicos", "Uganda", "Ukraine",
                "United Arab Emirates", "United Kingdom", "Uruguay", "Uzbekistan", "Venezuela", "Vietnam", "Virgin Islands (US)", "Yemen", "Zambia", "Zimbabwe"};
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
                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0)); // mapboxMap came from MapContext
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

    public void onNavigateButtonClicked(View view) {

        try {
            boolean simulateRoute = true;
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(super.locationMarker.currentRoute)
                    .shouldSimulateRoute(simulateRoute)
                    .build();

            //System.out.println("\n###########################\n"+options.directionsRoute().legs().get(0).steps().get(0).maneuver().instruction()+"\n############################\n");
            //System.out.println("\n###########################\n"+options.directionsRoute().legs().get(0).steps().get(0).bannerInstructions().get(0).primary().toString()+"\n############################\n");

            if (options.directionsRoute().legs().isEmpty()) {
                throw new Exception("No route selected.");
            }

            // Prints some information for debugging purposes about the list properties. Can be removed as soon as this method is working
            System.out.println("\n###########################\nLeg size: "+options.directionsRoute().legs().size()+"\n############################\n");
            System.out.println("\n###########################\nSteps size: "+options.directionsRoute().legs().get(0).steps().size()+"\n############################\n");
            System.out.println("\n###########################\nbannerInstructions size: "+options.directionsRoute().legs().get(0).steps().get(0).bannerInstructions().size()+"\n############################\n");

            /**
             * I assume that directionsRoute().legs() contains one entry for every separate route, as currently with only one calculated route, its size is 1.
             * Thus, I added these two lists that contain these individual lists for future purposes
             * However at the moment, this list will only contain one sublist each, so it is somewhat obsolete for now.
             */
            ArrayList<ArrayList<StepManeuver>> routeManeuvers = new ArrayList<>();
            ArrayList<ArrayList<BannerInstructions>> routeBannerInstructions = new ArrayList<>();

            // Helper lists created in each iteration
            ArrayList<StepManeuver> maneuvers = new ArrayList<>();
            ArrayList<BannerInstructions> bannerInstructions = new ArrayList<>();

            // Iterate over j) the different available routes and i) over the steps of the route
            for (int j = 0; j < options.directionsRoute().legs().size(); j++) {
                for (int i=0; i < options.directionsRoute().legs().get(j).steps().size(); i++) {
                    maneuvers.add(options.directionsRoute().legs().get(j).steps().get(i).maneuver());
                    LegStep leg = options.directionsRoute().legs().get(j).steps().get(i);
                    if (!leg.bannerInstructions().isEmpty()) {
                        bannerInstructions.add(leg.bannerInstructions().get(0));
                    }
                    else {
                        System.out.println("Found empty bannerInstruction on iteration "+i+": "+leg.bannerInstructions().toString());
                    }
                }
                // Append to the list of lists and clear for next iteration
                routeManeuvers.add(new ArrayList<>(maneuvers));
                routeBannerInstructions.add(new ArrayList<>(bannerInstructions));
                maneuvers.clear();
                bannerInstructions.clear();
            }

            // For debugging purposes. Later on, we should simply return/use the values of the routeLists
            System.out.println("\n#################################################\nRoute information:\nManeuvers:\n"+routeManeuvers.toString());
            System.out.println("\n\nbannerInstructions:\n"+routeBannerInstructions.toString()+"\n#################################################\n");

            /**
             * How to work with StepManeuvers and BannerInstructions:
             * StepManeuvers look like this: StepManeuver{rawLocation=[13.260295, 52.412312], bearingBefore=227.0, bearingAfter=310.0, instruction=Rechts abbiegen auf Ramsteinweg, type=turn, modifier=right, exit=null}
             * Thus by using step.instruction(), one can retrieve the text what to do next
             * BannerInstructions look like this: BannerInstructions{distanceAlongGeometry=73.6, primary=BannerText{text=Ramsteinweg, components=[BannerComponents{text=Ramsteinweg, type=text, abbreviation=null, abbreviationPriority=null, imageBaseUrl=null, directions=null, active=null}], type=turn, modifier=right, degrees=null, drivingSide=null}, secondary=null, sub=null}
             * Thus by using bannerInstruction.primary().text(), one can retrieve the text that is usually placed below an arrow on the labels showing which turn to do next
             */

            // Call this method with Context from within an Activity
            NavigationLauncher.startNavigation(this, options);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "You may not have selected a route yet.", Toast.LENGTH_LONG).show();
        }
    }
}
