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
#include "cardboard.h"

#include <cmath>

#include "head_tracker.h"
#include "../util/is_arg_null.h"

// TODO(b/134142617): Revisit struct/class hierarchy.
struct CardboardHeadTracker : cardboard::HeadTracker {
};

namespace {

// Return default (zero) position.
    void GetDefaultPosition(float *position) {
        if (position != nullptr) {
            position[0] = 0.0f;
            position[1] = 0.0f;
            position[2] = 0.0f;
        }
    }

// Return default (identity quaternion) orientation.
    void GetDefaultOrientation(float *orientation) {
        if (orientation != nullptr) {
            orientation[0] = 0.0f;
            orientation[1] = 0.0f;
            orientation[2] = 0.0f;
            orientation[3] = 1.0f;
        }
    }

}  // anonymous namespace

extern "C" {

#ifdef __ANDROID__
void Cardboard_initializeAndroid(JavaVM *vm, jobject context) {
    if (CARDBOARD_IS_ARG_NULL(vm) || CARDBOARD_IS_ARG_NULL(context)) {
        return;
    }
    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);
}
#endif

CardboardHeadTracker *CardboardHeadTracker_create() {
    return reinterpret_cast<CardboardHeadTracker *>(new cardboard::HeadTracker());
}

void CardboardHeadTracker_destroy(CardboardHeadTracker *head_tracker) {
    if (CARDBOARD_IS_ARG_NULL(head_tracker)) {
        return;
    }
    delete head_tracker;
}

void CardboardHeadTracker_pause(CardboardHeadTracker *head_tracker) {
    if (CARDBOARD_IS_ARG_NULL(head_tracker)) {
        return;
    }
    static_cast<cardboard::HeadTracker *>(head_tracker)->Pause();
}

void CardboardHeadTracker_resume(CardboardHeadTracker *head_tracker) {
    if (CARDBOARD_IS_ARG_NULL(head_tracker)) {
        return;
    }
    static_cast<cardboard::HeadTracker *>(head_tracker)->Resume();
}

void CardboardHeadTracker_getPose(
        CardboardHeadTracker *head_tracker, int64_t timestamp_ns,
        CardboardViewportOrientation viewport_orientation, float *position,
        float *orientation) {
    if (CARDBOARD_IS_ARG_NULL(head_tracker) ||
        CARDBOARD_IS_ARG_NULL(position) || CARDBOARD_IS_ARG_NULL(orientation)) {
        GetDefaultPosition(position);
        GetDefaultOrientation(orientation);
        return;
    }
    std::array<float, 3> out_position;
    std::array<float, 4> out_orientation;
    static_cast<cardboard::HeadTracker *>(head_tracker)
            ->GetPose(timestamp_ns, viewport_orientation, out_position, out_orientation);
    std::memcpy(position, &out_position[0], 3 * sizeof(float));
    std::memcpy(orientation, &out_orientation[0], 4 * sizeof(float));
}
}  // extern "C"
