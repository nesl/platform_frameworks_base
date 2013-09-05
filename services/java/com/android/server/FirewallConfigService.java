package com.android.server;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.Base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import android.os.Binder;
import android.os.IFirewallConfigService;
import android.os.RemoteException;

//import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import android_sensorfirewall.FirewallConfigMessages.*;
import android.location.ILocationManager;

public class FirewallConfigService extends IFirewallConfigService.Stub {
    private static final String TAG = "FirewallConfigService";
    private static final String kConfigFilename = "/data/firewall-config";
    private Context mContext;
    private ILocationManager mLocationService;
    

    /**
     * @hide The right way to get an instance is via getSystemService(...)
     */
    public FirewallConfigService(Context context) {
        mContext = context;
    }

    public void setFirewallConfig(String serializedFirewallConfigProto) {
        // TODO(krr): Verify permission to invoke this method.
        Log.d(TAG, String.format("Called setFirewallConfig; <UID=%d, PID=%d>.",
                Binder.getCallingUid(), Binder.getCallingPid()));

        // Verify that the input string parses as a FirewallConfig message. 
        FirewallConfig firewallConfig = null;
        try {
            byte[] byteArr = Base64.decode(serializedFirewallConfigProto, Base64.DEFAULT);
            firewallConfig = FirewallConfig.parseFrom(byteArr);
        } catch (InvalidProtocolBufferException ex) {
            Log.e(TAG, "InvalidProtocolBufferException");
            return;
        }

        // TODO: Do some useful parsing, e.g. rewriting of the config.
        if(firewallConfig != null) {
            Log.d(TAG, "Writing the Firewall Config File");
            for(Rule rule: firewallConfig.getRuleList()) {
                Log.d(TAG, "ruleName = " + rule.getRuleName() + ": sensorType = " + rule.getSensorType() + ": pkgName = " + rule.getPkgName() + ": pkgUid = " + rule.getPkgUid());
            }
        }

        // Serialize the FirewallConfig message to the config file. 
        FileOutputStream outputStream = null;
        File configFile = null;
        try{
        	configFile = new File(kConfigFilename);
        	if(!configFile.exists()) {
        		configFile.createNewFile();
        	}
        } catch (IOException e) {
        	Log.e(TAG, "IOException while opening the file for writing out protobuf");
        	return;
        }

        // TODO(krr): Need to lock the file?
        try {
            outputStream = new FileOutputStream(configFile);
        } catch (FileNotFoundException ex) {
            Log.e(TAG, "FileNotFoundException");
            return;
        } catch (SecurityException ex) {
            Log.e(TAG, "SecurityException");
            return;
        }

        Log.d(TAG, "Writing protobuf to file");
        
        try {
        	// a non-blocking call to get the lock on the file
        	FileLock writeLock = outputStream.getChannel().tryLock();
        	if(writeLock != null) {
        		firewallConfig.writeTo(outputStream);
        		outputStream.flush();
        		writeLock.release();
        	}
        	else {
        		Log.e(TAG, "Unable to get write lock on file to write protobuf");
        		return;
        	}
        } catch (IOException ex) {
            Log.e(TAG, "IOException while writing out protobuf.");
            return;
        }

        try {
            outputStream.close();
        } catch (IOException ex) {
            Log.e(TAG, "IOException while close file.");
            return;
        }

        Log.d(TAG, "Finished writing protobuf to file");
        
        // Ask the sensorservice to reload its config.
        String serviceName = Context.SENSOR_SERVICE;
        SensorManager sensorManager =
                (SensorManager)mContext.getSystemService(serviceName);
        Log.d(TAG, "Calling reloadConfig.");
        sensorManager.reloadConfig();
        try {
        	mLocationService.reloadConfig();
        } 
        catch (RemoteException ex) {
        	Log.e(TAG, "Unable to invoke reloadConfig on LocationManagerService");
        }
    }
}
