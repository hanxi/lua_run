ndk-build NDK_MODULE_PATH=jni
ant debug
adb uninstall com.hanxi.luarun
adb install bin/WelcomeActivity-debug.apk
adb logcat
