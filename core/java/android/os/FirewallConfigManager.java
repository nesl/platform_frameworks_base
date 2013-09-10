package android.os;

import android.os.IBinder;
import android.os.IFirewallConfigService;
import android.util.Log;
import android.hardware.Sensor;

public class FirewallConfigManager {
    private static final String TAG = "FirewallConfigManager";
    private final IFirewallConfigService mFirewallConfigService;
    private static FirewallConfigManager mFirewallConfigManager;

    public static final int TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public static final int TYPE_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
    public static final int TYPE_ORIENTATION = Sensor.TYPE_ORIENTATION;
    public static final int TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public static final int TYPE_LIGHT = Sensor.TYPE_LIGHT;
    public static final int TYPE_PRESSURE = Sensor.TYPE_PRESSURE;
    public static final int TYPE_TEMPERATURE = Sensor.TYPE_TEMPERATURE;
    public static final int TYPE_PROXIMITY = Sensor.TYPE_PROXIMITY;
    public static final int TYPE_GRAVITY = Sensor.TYPE_GRAVITY;
    public static final int TYPE_LINEAR_ACCELERATION = Sensor.TYPE_LINEAR_ACCELERATION;
    public static final int TYPE_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
    public static final int TYPE_RELATIVE_HUMIDITY = Sensor.TYPE_RELATIVE_HUMIDITY;
    public static final int TYPE_AMBIENT_TEMPERATURE = Sensor.TYPE_AMBIENT_TEMPERATURE;
    public static final int TYPE_ALL = Sensor.TYPE_ALL;
    public static final int TYPE_GPS = 30;


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
