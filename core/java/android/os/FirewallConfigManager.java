package android.os;

import android.os.IBinder;
import android.os.IFirewallConfigService;
import android.util.Log;

public class FirewallConfigManager {
    private static final String TAG = "FirewallConfigManager";
    private final IFirewallConfigService mFirewallConfigService;
    private static FirewallConfigManager mFirewallConfigManager;

    public static synchronized FirewallConfigManager getFirewallConfigManager() {
        if(mFirewallConfigManager == null) {
            IBinder binder = android.os.ServiceManager.getService("firewallconfigservice");
            if(binder != null) {
                IFirewallConfigService firewallConfigService = IFirewallConfigService.Stub.asInterface(binder);
                mFirewallConfigManager = new FirewallConfigManager(firewallConfigService);
            } else {
                Log.e(TAG, "FirewallConfigService binder is null");
            }
        }
        return mFirewallConfigManager;
    }

    FirewallConfigManager(IFirewallConfigService firewallConfigService) {
        if(firewallConfigService == null) {
            throw new IllegalArgumentException("FirewallConfigService is null");
        }
        mFirewallConfigService = firewallConfigService;
    }

    public void setFirewallConfig(String serializedFirewallConfigProto) {
        try{
            Log.d(TAG, "Calling service from framework proxy");
            mFirewallConfigService.setFirewallConfig(serializedFirewallConfigProto);
        } catch (Exception e) {
            Log.d(TAG, "Failed to call service from framework proxy");
            e.printStackTrace();
        }
    }

    public IFirewallConfigService getFirewallConfigService() {
        return mFirewallConfigService;
    }
}
