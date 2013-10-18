package android.inference;

import java.util.ArrayList;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class InferenceManager {
    final Looper mMainLooper;
    static final ArrayList<ListenerDelegate> mListeners = new ArrayList<ListenerDelegate>();
    private final IInferenceService mInferenceService;
    
	public final static String STILL = "still";
	public final static String WALKING = "walking";
	public final static String RUNNING = "running";

	private class ListenerDelegate {
        private final InferenceEventListener mInferenceEventListener;
        private final Handler mHandler;
        ListenerDelegate(InferenceEventListener listener, Handler handler) {
            mInferenceEventListener = listener;
            Looper looper = (handler != null) ? handler.getLooper() : mMainLooper;
            // currently we create one Handler instance per listener, but we could
            // have one per looper (we'd need to pass the ListenerDelegate
            // instance to handleMessage and keep track of them separately).
            mHandler = new Handler(looper) {
                @Override
                public void handleMessage(Message msg) {
                    final InferenceEvent t = (InferenceEvent)msg.obj;
                    mInferenceEventListener.onInferenceChanged(t);
                }
            };
        }

        void onInferenceChangedLocked(String label, long timestamp) {
            InferenceEvent t = new InferenceEvent();
            t.timestamp = timestamp;
            t.label = label;
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = t;
            msg.setAsynchronous(true);
            mHandler.sendMessage(msg);
        }
    }

	public InferenceManager(Looper mainLooper) {
    	mMainLooper = mainLooper;
    	IBinder binder = android.os.ServiceManager.getService("inferenceservice");
        if(binder != null) {
        	mInferenceService = IInferenceService.Stub.asInterface(binder);
        } else {
        	mInferenceService = null;
            Log.e("InferenceManager", "IInferenceService binder is null");
        }
    }
	
	public void setModel(String model) {
		Log.d("InferenceManager", "set model to InferenceService");
		try {
			mInferenceService.setModel(model);
		}
		catch (Exception ex) {
			 Log.e("InferenceManager", "Failed to call InferenceService from InferenceManager");
			 Log.e("InferenceManager", ex.toString());
			 //ex.printStackTrace();
		}
		
	}

    public boolean registerListener(InferenceEventListener listener) {
    	return registerListener(listener, null);
    }

    public boolean registerListener(InferenceEventListener listener, Handler handler) {
    	ListenerDelegate l = new ListenerDelegate(listener, handler);
    	mListeners.add(l);
    	InferenceThreadRunnable runnable = new InferenceThreadRunnable();
        Thread thread = new Thread(runnable);
        thread.start();
    	return true;
    }

    private class InferenceThreadRunnable implements Runnable {
        InferenceThreadRunnable() {
        
        }

        public void run() {
        	Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
        	for (int i = 0; i < 100; i++) {
	        	for(ListenerDelegate l:mListeners) {
	        		l.onInferenceChangedLocked("WALK", 12345);
	        	}
	        	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    }

}