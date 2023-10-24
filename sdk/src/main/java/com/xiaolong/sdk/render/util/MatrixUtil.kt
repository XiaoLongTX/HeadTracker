package com.xiaolong.sdk.render.util

import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MatrixUtil {
    /**
     * Rotates a matrix by the given angle around the X axis
     *
     * @param {mat4} out the receiving matrix
     * @param {mat4} a the matrix to rotate
     * @param {Number} rad the angle to rotate the matrix by
     * @returns {mat4} out
     */
    fun m4RotateX(out: FloatArray, a: FloatArray, rad: Float): FloatArray {
        val s = sin(rad.toDouble()).toFloat()
        val c = cos(rad.toDouble()).toFloat()
        val a10 = a[4]
        val a11 = a[5]
        val a12 = a[6]
        val a13 = a[7]
        val a20 = a[8]
        val a21 = a[9]
        val a22 = a[10]
        val a23 = a[11]
        if (!a.contentEquals(out)) { // If the source and destination differ, copy the unchanged rows
            out[0] = a[0]
            out[1] = a[1]
            out[2] = a[2]
            out[3] = a[3]
            out[12] = a[12]
            out[13] = a[13]
            out[14] = a[14]
            out[15] = a[15]
        }
        out[4] = a10 * c + a20 * s
        out[5] = a11 * c + a21 * s
        out[6] = a12 * c + a22 * s
        out[7] = a13 * c + a23 * s
        out[8] = a20 * c - a10 * s
        out[9] = a21 * c - a11 * s
        out[10] = a22 * c - a12 * s
        out[11] = a23 * c - a13 * s
        return out
    }

    /**
     * Rotates a matrix by the given angle around the Y axis
     *
     * @param {mat4} out the receiving matrix
     * @param {mat4} a the matrix to rotate
     * @param {Number} rad the angle to rotate the matrix by
     * @returns {mat4} out
     */
    fun m4RotateY(out: FloatArray, a: FloatArray, rad: Float): FloatArray {
        val s = sin(rad.toDouble()).toFloat()
        val c = cos(rad.toDouble()).toFloat()
        val a00 = a[0]
        val a01 = a[1]
        val a02 = a[2]
        val a03 = a[3]
        val a20 = a[8]
        val a21 = a[9]
        val a22 = a[10]
        val a23 = a[11]
        if (!a.contentEquals(out)) { // If the source and destination differ, copy the unchanged rows
            out[4] = a[4]
            out[5] = a[5]
            out[6] = a[6]
            out[7] = a[7]
            out[12] = a[12]
            out[13] = a[13]
            out[14] = a[14]
            out[15] = a[15]
        }

        // Perform axis-specific matrix multiplication
        out[0] = a00 * c - a20 * s
        out[1] = a01 * c - a21 * s
        out[2] = a02 * c - a22 * s
        out[3] = a03 * c - a23 * s
        out[8] = a00 * s + a20 * c
        out[9] = a01 * s + a21 * c
        out[10] = a02 * s + a22 * c
        out[11] = a03 * s + a23 * c
        return out
    }

    /**
     * Rotates a matrix by the given angle around the Z axis
     *
     * @param {mat4} out the receiving matrix
     * @param {mat4} a the matrix to rotate
     * @param {Number} rad the angle to rotate the matrix by
     * @returns {mat4} out
     */
    fun m4RotateZ(out: FloatArray, a: FloatArray, rad: Float): FloatArray {
        val s = sin(rad.toDouble()).toFloat()
        val c = cos(rad.toDouble()).toFloat()
        val a00 = a[0]
        val a01 = a[1]
        val a02 = a[2]
        val a03 = a[3]
        val a10 = a[4]
        val a11 = a[5]
        val a12 = a[6]
        val a13 = a[7]
        if (!a.contentEquals(out)) { // If the source and destination differ, copy the unchanged last row
            out[8] = a[8]
            out[9] = a[9]
            out[10] = a[10]
            out[11] = a[11]
            out[12] = a[12]
            out[13] = a[13]
            out[14] = a[14]
            out[15] = a[15]
        }

        // Perform axis-specific matrix multiplication
        out[0] = a00 * c + a10 * s
        out[1] = a01 * c + a11 * s
        out[2] = a02 * c + a12 * s
        out[3] = a03 * c + a13 * s
        out[4] = a10 * c - a00 * s
        out[5] = a11 * c - a01 * s
        out[6] = a12 * c - a02 * s
        out[7] = a13 * c - a03 * s
        return out
    }

    /**
     * Generates a look-at matrix with the given eye position, focal point, and up axis
     *
     * @param {mat4} out mat4 frustum matrix will be written into
     * @param {vec3} eye Position of the viewer
     * @param {vec3} center Point the viewer is looking at
     * @param {vec3} up vec3 pointing up
     * @returns {mat4} out
     */
    fun m4LookAt(out: FloatArray, eye: FloatArray, center: FloatArray, up: FloatArray): FloatArray {
        var x0: Float
        var x1: Float
        var x2: Float
        var y0: Float
        var y1: Float
        var y2: Float
        var z0: Float
        var z1: Float
        var z2: Float
        var len: Float
        val eyex = eye[0]
        val eyey = eye[1]
        val eyez = eye[2]
        val upx = up[0]
        val upy = up[1]
        val upz = up[2]
        val centerx = center[0]
        val centery = center[1]
        val centerz = center[2]
        if (eyex == centerx && eyey == centery && eyez == centerz) {
            Matrix.setIdentityM(out, 0)
            return out
        }
        z0 = eyex - centerx
        z1 = eyey - centery
        z2 = eyez - centerz
        len = 1f / sqrt((z0 * z0 + z1 * z1 + z2 * z2).toDouble()).toFloat()
        z0 *= len
        z1 *= len
        z2 *= len
        x0 = upy * z2 - upz * z1
        x1 = upz * z0 - upx * z2
        x2 = upx * z1 - upy * z0
        len = sqrt((x0 * x0 + x1 * x1 + x2 * x2).toDouble()).toFloat()
        if (len == 0f) {
            x0 = 0f
            x1 = 0f
            x2 = 0f
        } else {
            len = 1 / len
            x0 *= len
            x1 *= len
            x2 *= len
        }
        y0 = z1 * x2 - z2 * x1
        y1 = z2 * x0 - z0 * x2
        y2 = z0 * x1 - z1 * x0
        len = sqrt((y0 * y0 + y1 * y1 + y2 * y2).toDouble()).toFloat()
        if (len == 0f) {
            y0 = 0f
            y1 = 0f
            y2 = 0f
        } else {
            len = 1 / len
            y0 *= len
            y1 *= len
            y2 *= len
        }
        out[0] = x0
        out[1] = y0
        out[2] = z0
        out[3] = 0f
        out[4] = x1
        out[5] = y1
        out[6] = z1
        out[7] = 0f
        out[8] = x2
        out[9] = y2
        out[10] = z2
        out[11] = 0f
        out[12] = -(x0 * eyex + x1 * eyey + x2 * eyez)
        out[13] = -(y0 * eyex + y1 * eyey + y2 * eyez)
        out[14] = -(z0 * eyex + z1 * eyey + z2 * eyez)
        out[15] = 1f
        return out
    }

    fun m4Multiply(out: FloatArray, a: FloatArray, b: FloatArray): FloatArray {
        val a00 = a[0]
        val a01 = a[1]
        val a02 = a[2]
        val a03 = a[3]
        val a10 = a[4]
        val a11 = a[5]
        val a12 = a[6]
        val a13 = a[7]
        val a20 = a[8]
        val a21 = a[9]
        val a22 = a[10]
        val a23 = a[11]
        val a30 = a[12]
        val a31 = a[13]
        val a32 = a[14]
        val a33 = a[15]

        // Cache only the current line of the second matrix
        var b0 = b[0]
        var b1 = b[1]
        var b2 = b[2]
        var b3 = b[3]
        out[0] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30
        out[1] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31
        out[2] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32
        out[3] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33
        b0 = b[4]
        b1 = b[5]
        b2 = b[6]
        b3 = b[7]
        out[4] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30
        out[5] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31
        out[6] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32
        out[7] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33
        b0 = b[8]
        b1 = b[9]
        b2 = b[10]
        b3 = b[11]
        out[8] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30
        out[9] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31
        out[10] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32
        out[11] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33
        b0 = b[12]
        b1 = b[13]
        b2 = b[14]
        b3 = b[15]
        out[12] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30
        out[13] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31
        out[14] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32
        out[15] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33
        return out
    }
}