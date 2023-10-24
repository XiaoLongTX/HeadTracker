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

#ifndef HELLO_CARDBOARD_ANDROID_SRC_MAIN_JNI_UTIL_H_
#define HELLO_CARDBOARD_ANDROID_SRC_MAIN_JNI_UTIL_H_

#include "../../../../../../../Android/ndk/21.4.7075529/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/android/asset_manager.h"
#include "../../../../../../../Android/ndk/21.4.7075529/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/jni.h"

#include "../../../../../../../Android/ndk/21.4.7075529/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/c++/v1/array"
#include "../../../../../../../Android/ndk/21.4.7075529/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/c++/v1/vector"

#define LOG_TAG "HelloCardboardApp"
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace ndk_header_tracker {

class Matrix4x4 {
 public:
  float m[4][4];

  // Multiplies two matrices.
  Matrix4x4 operator*(const Matrix4x4& right);

  // Multiplies a matrix with a vector.
  std::array<float, 4> operator*(const std::array<float, 4>& vec);

  // Converts a matrix to an array of floats suitable for passing to OpenGL.
  std::array<float, 16> ToGlArray();
};

struct Quatf {
  float x;
  float y;
  float z;
  float w;

  Quatf(float x_, float y_, float z_, float w_) : x(x_), y(y_), z(z_), w(w_) {}

  Quatf() : x(0), y(0), z(0), w(1) {}

  static Quatf FromXYZW(float q[4]) { return Quatf(q[0], q[1], q[2], q[3]); }

  Matrix4x4 ToMatrix();
};
/**
 * Construct a translation matrix.
 *
 * @param translation Translation array
 * @return Obtained matrix
 */
Matrix4x4 GetTranslationMatrix(const std::array<float, 3>& translation);

/**
 * Gets system boot time in nanoseconds.
 *
 * @return System boot time in nanoseconds
 */
int64_t GetBootTimeNano();
}  // namespace ndk_header_tracker

#endif  // HELLO_CARDBOARD_ANDROID_SRC_MAIN_JNI_UTIL_H_
