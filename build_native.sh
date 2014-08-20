ndk-build NDK_MODULE_PATH=jni
ant debug
adb uninstall com.hanxi.luarun
adb install bin/luaIDE-debug.apk
adb logcat
