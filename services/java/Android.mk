LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	com/android/server/FirewallConfigMessages.proto

LOCAL_SRC_FILES += \
            $(call all-subdir-java-files) \
	    com/android/server/EventLogTags.logtags \
	    com/android/server/am/EventLogTags.logtags

LOCAL_MODULE:= services

LOCAL_JAVA_LIBRARIES := android.policy telephony-common

include $(BUILD_JAVA_LIBRARY)

LOCAL_STATIC_JAVA_LIBRARIES :=  # Cleaned out.
LOCAL_JAVA_LIBRARIES += libprotobuf-java-2.3.0-lite

include $(BUILD_DROIDDOC)
