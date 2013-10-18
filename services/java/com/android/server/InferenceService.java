package com.android.server;

import android.content.Context;
import android.util.Log;
import android.inference.IInferenceService;


public class InferenceService extends IInferenceService.Stub{
	private Context mContext;
    public InferenceService(Context context) {
        mContext = context;
        Log.d("InferenceService", "InferenceService started!");
    }
    
    public void setModel(String serializedClassifier) {
    	Log.d("InferenceService", "get classifier=" + serializedClassifier);
    }
}
