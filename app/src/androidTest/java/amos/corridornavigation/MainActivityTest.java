package amos.corridornavigation;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import android.support.test.rule.ActivityTestRule;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import static org.junit.Assert.*;

public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);
    private MainActivity mActivity = null;

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch() {
        View mapView = mActivity.findViewById(R.id.mapView);
        View actionButton = mActivity.findViewById(R.id.floatingActionButton2);
        assertNotNull(mapView);
        assertNotNull(actionButton);
        onView(withId(R.id.floatingActionButton2)).perform(click());
        onView(withId(R.id.mapView)).perform(swipeDown(), swipeLeft(), swipeRight(), swipeUp());
    }

    @After
    public void tearDown() throws Exception {
        mActivity = null;
    }
}
