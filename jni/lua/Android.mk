LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := lua
LOCAL_MODULE_FILENAME := liblua
LOCAL_SRC_FILES := lapi.c \
lauxlib.c \
lbaselib.c \
lbitlib.c \
lcode.c \
lcorolib.c \
lctype.c \
ldblib.c \
ldebug.c \
ldo.c \
ldump.c \
lfunc.c \
lgc.c \
linit.c \
liolib.c \
llex.c \
lmathlib.c \
lmem.c \
loadlib.c \
lobject.c \
lopcodes.c \
loslib.c \
lparser.c \
lstate.c \
lstring.c \
lstrlib.c \
ltable.c \
ltablib.c \
ltm.c \
lua.c \
luac.c \
lundump.c \
lvm.c \
lzio.c

LOCAL_CFLAGS := -std=c99
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
 
include $(BUILD_STATIC_LIBRARY)

