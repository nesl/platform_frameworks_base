package com.android.server;

import android_sensorfirewall.FirewallConfigMessages.*;
import android.location.Location;
import android.util.Log;
import android.util.Slog;

public class SensorPerturb {
   private static final String TAG = "LocationSensorPerturb";

   private Location suppressData(Location notifyLocation, Rule rule) {
       // check date
       return null;
   }

   private Location constantData(Location notifyLocation, Rule rule) {
       // Check date
       Action action = rule.getAction();
       VectorValue vectorValue = action.getParam().getConstantValue().getVecVal();
       notifyLocation.setLatitude(vectorValue.getLat());
       notifyLocation.setLongitude(vectorValue.getLon());
       Log.d(TAG, "set constant lat" + vectorValue.getLat());
       Log.d(TAG, "set constant lon" + vectorValue.getLon());
       if(vectorValue.hasAccuracy()) {
           notifyLocation.setAccuracy(vectorValue.getAccuracy());
       }
       if(vectorValue.hasSpeed()) {
           notifyLocation.setSpeed(vectorValue.getSpeed());
       }
       if(vectorValue.hasBearing()) {
           notifyLocation.setBearing(vectorValue.getBearing());
       }
       return notifyLocation;
   }

   private Location perturbData(Location notifyLocation, Rule rule) {
       // Check date
       return notifyLocation;
   }

   public Location transformData(Location notifyLocation, Rule rule) {
       if(rule != null) {
           switch(rule.getAction().getActionType()) {
               case ACTION_SUPPRESS: 
                   return suppressData(notifyLocation, rule);
               case ACTION_PASSTHROUGH:
                   return notifyLocation;
               case ACTION_CONSTANT:
                   return constantData(notifyLocation, rule);
               case ACTION_PERTURB:
                   return perturbData(notifyLocation, rule);
           }
       }
       return notifyLocation;
   } 
}
