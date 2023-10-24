package com.xiaolong.sdk

object HeaderTrackerUtil {
    external fun nativeOnCreate(): Long
    external fun nativeOnDestroy(nativeApp: Long)
    external fun nativeOnPause(nativeApp: Long)
    external fun nativeOnResume(nativeApp: Long)
    external fun nativeGetHeaderPose(nativeApp: Long, orientation: Int): FloatArray

    init {
        System.loadLibrary("headtracker")
    }
}