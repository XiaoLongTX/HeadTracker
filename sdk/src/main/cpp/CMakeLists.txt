# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# C++ flags.
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)
add_compile_options(-Wall -Wextra)

# Declares and names the project.
project("sdk")

include_directories(../java)
file(GLOB general_srcs "*.cc")
file(GLOB sensors_srcs "sensors/*.cc")
file(GLOB sensors_android_srcs "sensors/android/*.cc")
file(GLOB util_srcs "util/*.cc")
file(GLOB headtracker "headtracker/*.cc")

add_library(headtracker
        SHARED
        ${general_srcs}
        ${sensors_srcs}
        ${sensors_android_srcs}
        ${util_srcs}
        ${headtracker}
        )

# Standard Android dependencies
find_library(android-lib android)
find_library(log-lib log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        headtracker
        ${android-lib}
        ${log-lib})