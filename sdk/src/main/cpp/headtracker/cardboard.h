/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef CARDBOARD_SDK_INCLUDE_CARDBOARD_H_
#define CARDBOARD_SDK_INCLUDE_CARDBOARD_H_

#ifdef __ANDROID__
#include <jni.h>
#endif

#include <stdint.h>

/// Enum to describe the possible orientations of the viewport.
typedef enum CardboardViewportOrientation {
  /// Landscape left orientation, which maps to:
  /// - Android: landscape.
  /// - IOS: UIDeviceOrientationLandscapeLeft.
  /// - Unity: ScreenOrientation.LandscapeLeft.
  kLandscapeLeft = 0,
  /// Landscape right orientation, which maps to:
  /// - Android: reverseLandscape.
  /// - IOS: UIDeviceOrientationLandscapeRight.
  /// - Unity: ScreenOrientation.LandscapeRight.
  kLandscapeRight = 1,
  /// Portrait orientation, which maps to:
  /// - Android: portrait.
  /// - IOS: UIDeviceOrientationPortrait.
  /// - Unity: ScreenOrientation.Portrait.
  kPortrait = 2,
  /// Portrait upside down orientation, which maps to:
  /// - Android: reversePortrait.
  /// - IOS: UIDeviceOrientationPortraitUpsideDown.
  /// - Unity: ScreenOrientation.PortraitUpsideDown.
  kPortraitUpsideDown = 3,
} CardboardViewportOrientation;
/// An opaque Head Tracker object.
typedef struct CardboardHeadTracker CardboardHeadTracker;

/// @}

#ifdef __cplusplus
extern "C" {
#endif

/////////////////////////////////////////////////////////////////////////////
// Initialization (Android only)
/////////////////////////////////////////////////////////////////////////////
/// @defgroup initialization Initialization (Android only)
/// @brief This module initializes the JavaVM and Android context.
///
/// Important: This function is only used by Android and it's mandatory to call
///     this function before using any other Cardboard APIs.
/// @{

#ifdef __ANDROID__
/// Initializes the JavaVM and Android context.
///
/// @details The following methods are required to work for the parameter
///   @p context:
///
/// -
/// <a
/// href="https://developer.android.com/reference/android/content/Context#getFilesDir()">Context.getFilesDir()</a>
/// -
/// <a
/// href="https://developer.android.com/reference/android/content/Context#getResources()">Context.getResources()</a>
/// -
/// <a
/// href="https://developer.android.com/reference/android/content/Context#getSystemService(java.lang.String)">Context.getSystemService(Context.WINDOW_SERVICE)</a>
/// -
/// <a
/// href="https://developer.android.com/reference/android/content/Context#startActivity(android.content.Intent)">Context.startActivity(Intent)</a>
/// -
/// <a
/// href="https://developer.android.com/reference/android/content/Context#getDisplay()">Context.getDisplay()</a>
///
/// @pre @p vm Must not be null.
/// @pre @p context Must not be null.
/// When it is unmet a call to this function results in a no-op.
///
/// @param[in]      vm                      JavaVM pointer
/// @param[in]      context                 The current Android Context. It is
///                                         generally an Activity instance or
///                                         wraps one.
void Cardboard_initializeAndroid(JavaVM* vm, jobject context);
#endif

/// @}

/////////////////////////////////////////////////////////////////////////////
// Head Tracker
/////////////////////////////////////////////////////////////////////////////
/// @defgroup head-tracker Head Tracker
/// @brief This module calculates the predicted head's pose for a given
///     timestamp. It takes data from accelerometer and gyroscope sensors and
///     uses a Kalman filter to generate the output value. The head's pose is
///     returned as a quaternion. To have control of the usage of the sensors,
///     this module also includes pause and resume functions.
///
/// @details Let the World frame be an arbitrary 3D Cartesian right handed frame
///          whose basis is defined by a triplet of unit vectors
///          (x, y, z) which point in the same
///          direction as OpenGL. That is: x points to the right,
///          y points up and z points backwards.
///
///          The head pose is always returned in the World frame. It is the
///          average of the left and right eye position. By default, the head
///          pose is near the origin, looking roughly forwards (down the
///          -z axis).
///
///          Implementation and application code could refer to another three
///          poses:
///          - Raw sensor pose: no position, only orientation of device, derived
///            directly from sensors.
///          - Recentered sensor pose: like "Raw sensor pose", but with
///            recentering applied.
///          - Head pose: Recentered sensor pose, with neck model applied. The
///            neck model only adjusts position, it does not adjust orientation.
///            This is usually used directly as the camera pose, though it may
///            be further adjusted via a scene graph. This is the only pose
///            exposed through the API.
/// @{

/// Creates a new head tracker object.
///
/// @return         head tracker object pointer
CardboardHeadTracker* CardboardHeadTracker_create();

/// Destroys and releases memory used by the provided head tracker object.
///
/// @pre @p head_tracker Must not be null.
/// When it is unmet, a call to this function results in a no-op.
///
/// @param[in]      head_tracker            Head tracker object pointer.
void CardboardHeadTracker_destroy(CardboardHeadTracker* head_tracker);

/// Pauses head tracker and underlying device sensors.
///
/// @pre @p head_tracker Must not be null.
/// When it is unmet, a call to this function results in a no-op.
///
/// @param[in]      head_tracker            Head tracker object pointer.
void CardboardHeadTracker_pause(CardboardHeadTracker* head_tracker);

/// Resumes head tracker and underlying device sensors.
///
/// @pre @p head_tracker Must not be null.
/// When it is unmet, a call to this function results in a no-op.
///
/// @param[in]      head_tracker            Head tracker object pointer.
void CardboardHeadTracker_resume(CardboardHeadTracker* head_tracker);

/// Gets the predicted head pose for a given timestamp.
///
/// @details On Android devices, @p timestamp_ns must be in system boot time
///          (see [CLOCK_BOOTTIME](https://linux.die.net/man/2/clock_gettime))
///          clock (see [Android
///          Timestamp](https://developer.android.com/reference/android/hardware/SensorEvent#timestamp)).
///          On iOS devices, @p timestamp_ns must be in system uptime raw
///          (see
///          [CLOCK_UPTIME_RAW](http://www.manpagez.com/man/3/clock_gettime/))
///          clock (see [Apple
///          Timestamp](https://developer.apple.com/documentation/coremotion/cmlogitem/1615939-timestamp?language=objc)).
///
/// @pre @p head_tracker Must not be null.
/// @pre @p position Must not be null.
/// @pre @p orientation Must not be null.
/// When it is unmet, a call to this function results in a no-op and default
/// values are returned (zero values and identity quaternion, respectively).
///
/// @param[in]      head_tracker            Head tracker object pointer.
/// @param[in]      timestamp_ns            The timestamp for the pose in
///                                         nanoseconds.
/// @param[in]      viewport_orientation    The viewport orientation.
/// @param[out]     position                3 floats for (x, y, z).
/// @param[out]     orientation             4 floats for quaternion
void CardboardHeadTracker_getPose(
    CardboardHeadTracker* head_tracker, int64_t timestamp_ns,
    CardboardViewportOrientation viewport_orientation, float* position,
    float* orientation);

/// Recenters the head tracker.
///
/// @details        By recentering, the @p head_tracker orientation gets aligned
///                 with a zero yaw angle.
///
/// @pre @p head_tracker Must not be null.
///
/// @param[in]      head_tracker            Head tracker object pointer.
void CardboardHeadTracker_recenter(CardboardHeadTracker* head_tracker);

#ifdef __cplusplus
}
#endif

#endif  // CARDBOARD_SDK_INCLUDE_CARDBOARD_H_
