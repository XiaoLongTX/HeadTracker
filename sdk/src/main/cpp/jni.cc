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

#include <android/log.h>
#include <jni.h>
#include <memory>
#include <string>
#include <thread>
#include <vector>
#include "util.h"
#include "headtracker/cardboard.h"
#include "jni_api.h"


#define JNI_METHOD(return_type, method_name) \
  JNIEXPORT return_type JNICALL              \
      Java_com_xiaolong_sdk_HeaderTrackerUtil_##method_name

namespace {

    inline jlong jptr(ndk_header_tracker::HeadTracker *native_app) {
        return reinterpret_cast<intptr_t>(native_app);
    }

    inline ndk_header_tracker::HeadTracker *native(jlong ptr) {
        return reinterpret_cast<ndk_header_tracker::HeadTracker *>(ptr);
    }

    JavaVM *javaVm;
}  // anonymous namespace

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void * /*reserved*/) {
    javaVm = vm;
    return JNI_VERSION_1_6;
}

JNI_METHOD(jlong, nativeOnCreate)
(JNIEnv * /*env*/, jobject obj) {
    return jptr(new ndk_header_tracker::HeadTracker(javaVm, obj));
}

JNI_METHOD(void, nativeOnDestroy)
(JNIEnv * /*env*/, jobject /*obj*/, jlong native_app) {
    delete native(native_app);
}

JNI_METHOD(void, nativeOnPause)
(JNIEnv * /*env*/, jobject /*obj*/, jlong native_app) {
    native(native_app)->OnPause();
}

JNI_METHOD(void, nativeOnResume)
(JNIEnv * /*env*/, jobject /*obj*/, jlong native_app) {
    native(native_app)->OnResume();
}

JNI_METHOD(jfloatArray, nativeGetHeaderPose)
(JNIEnv *env, jobject /*obj*/, jlong native_app, jint orientation) {
    std::array<float, 16> array = native(native_app)->GetPose(orientation).ToGlArray();
    jfloatArray result = env->NewFloatArray(16);
    if (result == nullptr) {
        return nullptr;
    }
    env->SetFloatArrayRegion(result,0,array.size(),array.data());
    return result;
}
}  // extern "C"
