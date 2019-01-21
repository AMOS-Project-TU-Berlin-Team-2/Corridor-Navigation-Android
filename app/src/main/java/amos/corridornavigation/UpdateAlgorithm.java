package amos.corridornavigation;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import java.util.ArrayList;

import amos.corridornavigation.navigationview.CorridorNavigationActivity;

public class UpdateAlgorithm implements Parcelable {

    private Handler handler = new Handler();
    private int delay = 20000; // 20,000 milliseconds = 20 seconds; trigger the update method every 20 seconds
    public MainActivity classContext;
    public Router router;
    public CorridorNavigationActivity navigationActivity;
    private Boolean alreadyRunning = false;

    private int mData;

    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<UpdateAlgorithm> CREATOR = new Parcelable.Creator<UpdateAlgorithm>() {
        public UpdateAlgorithm createFromParcel(Parcel in) {
            return new UpdateAlgorithm(in);
        }

        public UpdateAlgorithm[] newArray(int size) {
            return new UpdateAlgorithm[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private UpdateAlgorithm(Parcel in) {
        mData = in.readInt();
    }

    public UpdateAlgorithm() {
    }

    public void startUpdateAlgorithm() {
        if (alreadyRunning) {
            return;
        }
        alreadyRunning = true;
        // Update algorithm. Fetches new routes every 20 seconds.
        handler.postDelayed(new Runnable() {
            public void run() {
                System.out.println("Executing the update algorithm...");
                /*
                System.out.println("Deleting the old alternatives...");
                // Remove all current alternatives
                for (int i=1; i < routes.size(); i += 1) {
                    intent.removeExtra("DirectionsRoute_"+i);
                }
                System.out.println("Calculating and adding the new alternatives...");
                sendBroadcast(intentNavigation);
                for (int i=0; i < routes.size(); i += 1) {
                    intent.putExtra("DirectionsRoute_"+i, routes.get(i));
                }
                */
                if (router == null || classContext == null) {
                    System.out.println("...but router or main are null :(");
                }
                else {
                    classContext.onRouteButtonClicked();
                    ArrayList<DirectionsRoute> routes = (ArrayList) router.currentRoute;
                    if (navigationActivity == null) {
                        System.out.println("navigationActivity is null :(");
                    }
                    else {
                        navigationActivity.alternativeDirectionsRoutes = routes;
                    }
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void stopUpdateAlgorithm() {
        handler.removeCallbacksAndMessages(null);
        alreadyRunning = false;
    }
}
