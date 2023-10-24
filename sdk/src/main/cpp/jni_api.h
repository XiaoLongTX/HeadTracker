//
// Created by maxbian on 2023/5/9.
//

#ifndef HEADTRACKER_JNI_API_H
#define HEADTRACKER_JNI_API_H

#include <jni.h>
#include <memory>
#include <string>
#include <thread>
#include <vector>
#include "util.h"
#include "headtracker/cardboard.h"

namespace ndk_header_tracker {
    class HeadTracker {
    public:
        HeadTracker(JavaVM *vm, jobject obj);

        ~HeadTracker();

        /**
         * Pauses head tracking.
         */
        void OnPause();

        /**
         * Resumes head tracking.
         */
        void OnResume();

        /**
         * Gets head's pose as a 4x4 matrix.
         *
         * @return matrix containing head's pose.
         */
        Matrix4x4 GetPose(int viewport_orientation);

    private:
        CardboardHeadTracker *head_tracker_;
    };
}

#endif //HEADTRACKER_JNI_API_H
