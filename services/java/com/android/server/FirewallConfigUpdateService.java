package com.android.server;

import java.io.File;

import android.content.Context;
import android.os.IFirewallConfigUpdateService;
import android.util.Log;

public class FirewallConfigUpdateService extends IFirewallConfigUpdateService.Stub {
    private static final String TAG = "FirewallConfigUpdateService";
    private static final String systemDir = "/etc";
    private static final String configFile = "firewall.cfg";
    private final File mConfigFileName;
    
    private Context mContext;

    
    private Object mLock = new Object();
    
    public FirewallConfigUpdateService(Context context) {
    	super();
    	mContext = context;
    }
    
    public void writeFirewallConfig(int val) {
    	Log.d(TAG, "In Writing Firewall Config with val = " + val);
    	//mConfigFileName = new File(systemDir, configFile);
    }
}