package android.inference;

import android.os.Looper;
import android.os.Process;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;

public class InferenceManager {
    final Looper mMainLooper;
    static final ArrayList<ListenerDelegate> mListeners = new ArrayList<ListenerDelegate>();

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