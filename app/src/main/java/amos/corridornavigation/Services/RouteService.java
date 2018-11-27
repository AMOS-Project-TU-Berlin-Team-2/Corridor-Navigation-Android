package amos.corridornavigation.Services;


import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
// import org.apache.http.*;

import retrofit2.Response;


public class RouteService {
    String getRoute(Point origin, Point destination) throws Exception {

        String url = "http://www.google.com/search?q=mkyong";

        URL object = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) object.openConnection();

        // optional default is GET
        connection.setRequestMethod("GET");

        //add request header
        //connection.setRequestProperty("User-Agent", USER_AGENT); //TODO which user agent

        int responseCode = connection.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString(); //TODO convert response to wished model
    }

}
