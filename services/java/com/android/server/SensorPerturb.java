package com.android.server;
import android_sensorfirewall.FirewallConfigMessages.*;
import android.location.Location;

public class SensorPerturb {
   Location transformData(Location notifyLocation, Rule rule) {
   		 // test to modify notifyLocation
   		 notifyLocation.setLatitude(10.0);
   		 notifyLocation.setLongitude(5.0);
       return notifyLocation;
   } 
}
