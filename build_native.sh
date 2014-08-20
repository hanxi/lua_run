ndk-build NDK_MODULE_PATH=jni
ant release
adb uninstall com.hanxi.luarun
adb install bin/luaIDE-debug.apk
adb logcat
