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

#include "util.h"

#include <android/log.h>
#include <string.h>  // Needed for strtok_r and strstr
#include <time.h>
#include <unistd.h>

#include <array>
#include <cmath>
#include <random>
#include <sstream>
#include <string>


namespace ndk_header_tracker {

Matrix4x4 Matrix4x4::operator*(const Matrix4x4& right) {
  Matrix4x4 result;
  for (int i = 0; i < 4; ++i) {
    for (int j = 0; j < 4; ++j) {
      result.m[i][j] = 0.0f;
      for (int k = 0; k < 4; ++k) {
        result.m[i][j] += this->m[k][j] * right.m[i][k];
      }
    }
  }
  return result;
}

std::array<float, 4> Matrix4x4::operator*(const std::array<float, 4>& vec) {
  std::array<float, 4> result;
  for (int i = 0; i < 4; ++i) {
    result[i] = 0;
    for (int k = 0; k < 4; ++k) {
      result[i] += this->m[k][i] * vec[k];
    }
  }
  return result;
}

std::array<float, 16> Matrix4x4::ToGlArray() {
  std::array<float, 16> result;
  memcpy(&result[0], m, 16 * sizeof(float));
  return result;
}

Matrix4x4 Quatf::ToMatrix() {
  // Based on ion::math::RotationMatrix3x3
  const float xx = 2 * x * x;
  const float yy = 2 * y * y;
  const float zz = 2 * z * z;

  const float xy = 2 * x * y;
  const float xz = 2 * x * z;
  const float yz = 2 * y * z;

  const float xw = 2 * x * w;
  const float yw = 2 * y * w;
  const float zw = 2 * z * w;

  Matrix4x4 m;
  m.m[0][0] = 1 - yy - zz;
  m.m[0][1] = xy + zw;
  m.m[0][2] = xz - yw;
  m.m[0][3] = 0;
  m.m[1][0] = xy - zw;
  m.m[1][1] = 1 - xx - zz;
  m.m[1][2] = yz + xw;
  m.m[1][3] = 0;
  m.m[2][0] = xz + yw;
  m.m[2][1] = yz - xw;
  m.m[2][2] = 1 - xx - yy;
  m.m[2][3] = 0;
  m.m[3][0] = 0;
  m.m[3][1] = 0;
  m.m[3][2] = 0;
  m.m[3][3] = 1;

  return m;
}

Matrix4x4 GetTranslationMatrix(const std::array<float, 3>& translation) {
  return {{{1.0f, 0.0f, 0.0f, 0.0f},
           {0.0f, 1.0f, 0.0f, 0.0f},
           {0.0f, 0.0f, 1.0f, 0.0f},
           {translation.at(0), translation.at(1), translation.at(2), 1.0f}}};
}

static constexpr uint64_t kNanosInSeconds = 1000000000;

int64_t GetBootTimeNano() {
  struct timespec res;
  clock_gettime(CLOCK_BOOTTIME, &res);
  return (res.tv_sec * kNanosInSeconds) + res.tv_nsec;
}


}  // namespace ndk_header_tracker
