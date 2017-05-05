package com.petrolpatrol.petrolpatrol.util;


import com.petrolpatrol.petrolpatrol.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class GradientTest {

    @Test
    public void testColourConversion() {
    Gradient g = new Gradient(RuntimeEnvironment.application, 114.5);

        for (double i = 100; i < 140; i++) {
            Colour c = g.gradiateColour(i);
            System.out.println(String.valueOf(c.r) + " " + String.valueOf(c.g) + " " + String.valueOf(c.b));
        }

    }

}