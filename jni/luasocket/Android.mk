LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := socket
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../lua/
LOCAL_SRC_FILES := luasocket.c timeout.c buffer.c io.c auxiliar.c options.c inet.c tcp.c udp.c except.c select.c usocket.c mime.c

include $(BUILD_STATIC_LIBRARY)
