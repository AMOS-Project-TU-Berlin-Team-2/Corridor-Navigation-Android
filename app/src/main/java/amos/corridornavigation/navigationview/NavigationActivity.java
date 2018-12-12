package amos.corridornavigation.navigationview;

import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;

import amos.corridornavigation.MapContext;
import amos.corridornavigation.R;

public class NavigationActivity extends MapContext {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_navigation);
        initMapView(savedInstanceState);
    }
}
