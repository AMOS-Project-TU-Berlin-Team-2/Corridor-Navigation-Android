package amos.corridornavigation.Services;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;

import retrofit2.Response;

public interface RouteServiceInterface {

    String getRoute(Point origin, Point destination);

}
