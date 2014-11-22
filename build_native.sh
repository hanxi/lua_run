ndk-build NDK_MODULE_PATH=jni
#ant release
#adb uninstall com.hanxi.luarun
#adb install bin/luaIDE-release.apk
#adb logcat
ant debug
adb uninstall com.hanxi.luarun
adb install bin/luaIDE-debug-unaligned.apk
#adb logcat
