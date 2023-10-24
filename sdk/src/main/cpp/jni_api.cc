//
// Created by maxbian on 2023/5/9.
//

#include "jni_api.h"
#include <android/log.h>
#include <array>
#include <cmath>
#include <fstream>

#include "util/matrix_4x4.h"

namespace ndk_header_tracker {

    namespace {
        constexpr uint64_t kPredictionTimeWithoutVsyncNanos = 50000000;
    }  // anonymous namespace

    HeadTracker::HeadTracker(JavaVM *vm, jobject obj)
            : head_tracker_(nullptr) {
        JNIEnv *env;
        vm->GetEnv((void **) &env, JNI_VERSION_1_6);

        Cardboard_initializeAndroid(vm, obj);
        head_tracker_ = CardboardHeadTracker_create();
    }

    HeadTracker::~HeadTracker() {
        CardboardHeadTracker_destroy(head_tracker_);
    }

    void HeadTracker::OnPause() { CardboardHeadTracker_pause(head_tracker_); }

    void HeadTracker::OnResume() {
        CardboardHeadTracker_resume(head_tracker_);
    }


    Matrix4x4 HeadTracker::GetPose(int viewport_orientation) {
        std::array<float, 4> out_orientation;
        std::array<float, 3> out_position;
        CardboardViewportOrientation orientation;
        if (viewport_orientation == 0) {
            orientation = kLandscapeLeft;
        } else if (viewport_orientation == 1) {
            orientation = kLandscapeRight;
        } else if (viewport_orientation == 2) {
            orientation = kPortrait;
        } else {
            orientation = kPortraitUpsideDown;
        }
        CardboardHeadTracker_getPose(
                head_tracker_, GetBootTimeNano() + kPredictionTimeWithoutVsyncNanos,
                orientation, &out_position[0], &out_orientation[0]);
        return GetTranslationMatrix(out_position) *
               Quatf::FromXYZW(&out_orientation[0]).ToMatrix();
    }
}