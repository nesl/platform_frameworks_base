package com.android.server;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Binder;
import android.os.IFirewallConfigService;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import android_sensorfirewall.FirewallConfigMessages.*;

public class FirewallConfigService extends IFirewallConfigService.Stub {
    private static final String TAG = "FirewallConfigService";
    private static final String kConfigFilename = "/etc/firewall-config";
    private Context mContext;

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
        FirewallConfig firewallConfig;
        try {
            ByteString byteString =
                    ByteString.copyFromUtf8(serializedFirewallConfigProto);
            firewallConfig = FirewallConfig.parseFrom(byteString);
        } catch (InvalidProtocolBufferException ex) {
            Log.e(TAG, "InvalidProtocolBufferException");
            return;
        }

        // TODO: Do some useful parsing, e.g. rewriting of the config.
        Log.d(TAG, "Config debug_info: " + firewallConfig.getDebugInfo());

        // Serialize the FirewallConfig message to the config file. 
        File configFile = new File(kConfigFilename);
        FileOutputStream outputStream;

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

        try {
            firewallConfig.writeTo(outputStream);
        } catch (IOException ex) {
            Log.e(TAG, "IOException while writing out protobuf.");
            return;
        }

        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            Log.e(TAG, "IOException while flush and close.");
            return;
        }

        // Ask the sensorservice to reload its config.
        String serviceName = Context.SENSOR_SERVICE;
        SensorManager sensorManager =
                (SensorManager)mContext.getSystemService(serviceName);
        sensorManager.reloadConfig();
    }
}
