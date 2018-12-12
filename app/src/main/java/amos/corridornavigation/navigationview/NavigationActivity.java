package amos.corridornavigation.navigationview;

import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.Mapbox;

import java.util.ArrayList;

import amos.corridornavigation.MapContext;
import amos.corridornavigation.R;

public class NavigationActivity extends MapContext {

    TextView instructionView;
    ImageView bannerInstructionView;
    ArrayList<DirectionsRoute> routes;

    /**
     * TODO: Remove the functionality to set a new destination by touching on a location
     * Otherwise navigation will be funny :D
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_navigation);
        // Initialize the map in the navigation view
        initMapView(savedInstanceState);
        // Initialize the two instruction views
        instructionView = (TextView) findViewById(R.id.instructionView);
        bannerInstructionView = (ImageView) findViewById(R.id.bannerView);
    }

    /**
     * This function sets the instruction text in the textView in the NavigationView. To do so, simply
     * use the text that should be displayed and call this function with that text as a string
     * @param instructionText
     */
    public void setInstructionText(String instructionText) {
        instructionView.setText(instructionText);
    }

    /**
     * This function sets the banner instruction icon (e.g. an arrow to take a turn to the left). To do so, simply extract
     * the banner instruction from the calculated route (which looks like 'direction_end_of_road_left') and call this function
     * with that icon-name as a string
     * @param bannerInstruction
     */
    public void setBannerInstruction(String bannerInstruction) {
        int id = getResources().getIdentifier("@drawable/"+bannerInstruction, null, null);
        bannerInstructionView.setImageResource(id);
    }
}
