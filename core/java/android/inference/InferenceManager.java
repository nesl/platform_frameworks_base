package android.inference;

public class InferenceManager {
    final Looper mMainLooper;
    // list of listeners here
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
                    mInferenceEventListener.onSensorChanged(t);
                }
            };
        }

        Object getListener() {
            return mInferenceEventListener;
        }

        void onInferenceChangedLocked(String label, long timestamp) {
            InferenceEvent t = new InferenceEvent();
            t.timestamp = timestamp[0];
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
    	registerListener(listener, null);
    }

    public boolean registerListener(InferenceEventListener listener, Handler handler) {
    	new ListenerDelegate(listener, handler);
    	// add to list
    	// start the async task to send data to the listener
    }

}