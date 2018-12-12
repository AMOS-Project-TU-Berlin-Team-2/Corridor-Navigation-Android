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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_navigation);
        initMapView(savedInstanceState);
        instructionView = (TextView) findViewById(R.id.instructionView);
        bannerInstructionView = (ImageView) findViewById(R.id.bannerView);
    }

    public void setInstructionText(String instructionText) {
        instructionView.setText(instructionText);
    }

    public void setBannerInstruction(String bannerInstruction) {
        int id = getResources().getIdentifier("@drawable/"+bannerInstruction, null, null);
        bannerInstructionView.setImageResource(id);
    }
}
