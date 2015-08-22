LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/lua
LOCAL_C_INCLUDES += $(LOCAL_PATH)/luasocket
LOCAL_MODULE     := luajava
LOCAL_SRC_FILES  := luajava.c
LOCAL_STATIC_LIBRARIES := liblua libsocket
LOCAL_LDLIBS     := -llog

$(warning $(LOCAL_C_INCLUDES))
include $(BUILD_SHARED_LIBRARY)
$(call import-module,lua)
$(call import-module,luasocket)

