package com.android.server;

import android_sensorfirewall.FirewallConfigMessages.*;
import android.location.Location;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Random;

public class SensorPerturb {


   private static final String TAG = "LocationSensorPerturb";
   private ArrayList<Location> pb_buffer = new ArrayList<Location>();
   private Random mRandom = new Random();

   public void addLocation(Location l)
   {
       pb_buffer.add(l);
   }

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
       Log.d(TAG, "-set constant lat" + vectorValue.getLat());
       Log.d(TAG, "-set constant lon" + vectorValue.getLon());
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
       if (notifyLocation != null) {
           Action action = rule.getAction();
           // Latitude 1 deg = 110.574 km approximately
           double lat_degrees_per_km = 9e-3; // 1 km is approximately 0.009 degree
            // Longitude 1 deg = 111.32 * cos(latitude) KM
            double lng_degrees_per_km = 0.00898; // 

           Perturb perturb = action.getParam().getPerturb();
           if (perturb.getDistType() == Perturb.DistributionType.GAUSSIAN) {
               double mean = perturb.getMean();
               double variance = perturb.getVariance();
               double dLat = (mRandom.nextGaussian() * Math.sqrt(variance) + mean) * lat_degrees_per_km;
               double dLng = (mRandom.nextGaussian() * Math.sqrt(variance) + mean) * lng_degrees_per_km *
               Math.cos(notifyLocation.getLatitude());
               notifyLocation.setLatitude(notifyLocation.getLatitude() + dLat);
               notifyLocation.setLongitude(notifyLocation.getLongitude() + dLng);

           } else if (perturb.getDistType() == Perturb.DistributionType.UNIFORM) {
               double unifMin = perturb.getUnifMin();
               double unifMax = perturb.getUnifMax();
               double dLat = (mRandom.nextDouble() * (unifMax-unifMin) + unifMin) * lat_degrees_per_km;
               double dLng = (mRandom.nextDouble() * (unifMax-unifMin) + unifMin) * lng_degrees_per_km *
               Math.cos(notifyLocation.getLatitude());
               notifyLocation.setLatitude(notifyLocation.getLatitude() + dLat);
               notifyLocation.setLongitude(notifyLocation.getLongitude() + dLng);

           } else if (perturb.getDistType() == Perturb.DistributionType.EXPONENTIAL) {
               double lambda = perturb.getExpParam();
               double dLat = -1.0/ lambda * Math.log(1.0 - mRandom.nextDouble()) * lat_degrees_per_km;
               double dLng = -1.0/ lambda * Math.log(1.0 - mRandom.nextDouble()) * lng_degrees_per_km *
               Math.cos(notifyLocation.getLatitude());
               notifyLocation.setLatitude(notifyLocation.getLatitude() + dLat);
               notifyLocation.setLongitude(notifyLocation.getLongitude() + dLng);
           }
       }
       return notifyLocation;
   }

   public boolean isActionPlayback(Rule rule) {
       if (rule == null) {
           Log.d(TAG, "isActionPlayback: rule is null");
           return false;
       } else
           Log.d(TAG, "isActionPlayback: rule is not null");
       switch(rule.getAction().getActionType()) {
           case ACTION_PLAYBACK:
               return true;
           default:
               return false;
       }
   }

   public Location transformData(Location notifyLocation, Rule rule) {
       if(rule != null) {
           switch(rule.getAction().getActionType()) {
               case ACTION_PLAYBACK:
                   notifyLocation.makeComplete();
                   return notifyLocation;
//                 Log.d(TAG, "Playback called 1 " + notifyLocation.getLongitude());
//                 if (pb_buffer.isEmpty()) {
//                     Log.d(TAG, "pb_buffer is empty");
//                     return null;
//                 } else {
//                     Location l = pb_buffer.remove(0);
//                     notifyLocation.setLatitude(l.getLatitude());
//                     notifyLocation.setAltitude(l.getAltitude());
//                     notifyLocation.setLongitude(l.getLongitude());
//                     Log.d(TAG, "setting log " + notifyLocation.getLongitude());
//                     return notifyLocation;
//                 }
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
