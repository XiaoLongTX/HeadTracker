package com.xiaolong.sdk.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.xiaolong.sdk.HeaderTrackerUtil.nativeGetHeaderPose
import com.xiaolong.sdk.HeaderTrackerUtil.nativeOnDestroy
import com.xiaolong.sdk.HeaderTrackerUtil.nativeOnPause
import com.xiaolong.sdk.HeaderTrackerUtil.nativeOnResume
import com.xiaolong.sdk.render.util.MatrixUtil
import com.xiaolong.sdk.render.util.ShaderUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * 渲染器
 */
class VideoTextureSurfaceRenderer(context: Context?, texture: SurfaceTexture, width: Int, height: Int) :
    TextureSurfaceRenderer(texture, width, height), SurfaceTexture.OnFrameAvailableListener {
    // 模型旋转矩阵
    private var m4Rotate: FloatArray

    // 摄像机矩阵
    private val m4lookAt: FloatArray

    // 投影矩阵
    private val m4Perspective: FloatArray
    private var rotationX = -45f
    private var rotationY = 0f
    private var distance = 350f
    private val MAX_DISTANCE = 380f
    private val MIN_DISTANCE = -100f
    private val MAX_SCALE = 4
    private val MIN_SCALE = 1
    private val EYE_DISTANCE = 12f

    // 公式  (MAX_SCALE - scale) / (MAX_SCALE - MIN_SCALE) = (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)
    private var scale = MAX_SCALE - (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE) * (MAX_SCALE - MIN_SCALE)
    private var textureBuffer: FloatBuffer? = null
    private val textures = IntArray(1)
    private var shaderProgram = 0
    private var vertexBuffer: FloatBuffer? = null
    private var drawListBuffer: ShortBuffer? = null
    private val vertexTransform: FloatArray
    private val videoTextureTransform: FloatArray
    private var frameAvailable = false
    private var vertexShaderHandle = -1
    private var fragmentShaderHandle = -1
    private var textureTranformHandle = 0
    private var vertexTransformHandle = 0
    private var textureCoordsArray: FloatArray? = null
    private var drawOrderArray: ShortArray? = null
    var mSurfaceTexture:SurfaceTexture? = null
    init {
        m4Rotate = FloatArray(16)
        m4lookAt = FloatArray(16)
        m4Rotate = FloatArray(16)
        m4Perspective = FloatArray(16)
        vertexTransform = FloatArray(16)
        videoTextureTransform = FloatArray(16)
        mSurfaceTexture = SurfaceTexture(textures[0])
        mSurfaceTexture?.setOnFrameAvailableListener(this)
    }

    private fun loadShaders() {
        // 下面这两个程序可以参考H5页面上的 内容基本一样
        // 片段着色程序
        val fragmentShader = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES texture;
            varying vec2 v_TexCoordinate;
            void main () {
                vec4 color = texture2D(texture, v_TexCoordinate);
                gl_FragColor = color;
                }
        """
        // 顶点渲染程序
        val vertexShader = """
            attribute vec3 vPosition;
            attribute vec2 vTexCoordinate;
            uniform mat4 textureTransform;
            uniform mat4 vertexTransform;
            varying vec2 v_TexCoordinate;
            void main () {
                v_TexCoordinate = (textureTransform * vec4(vTexCoordinate, 0, 1.0)).xy;
                gl_Position = vertexTransform * vec4(vPosition, 1.0);
            }
        """
        // 编译  链接
        vertexShaderHandle = ShaderUtil.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderHandle = ShaderUtil.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        shaderProgram = ShaderUtil.createAndLinkProgram(
            vertexShaderHandle,
            fragmentShaderHandle,
            arrayOf("texture", "vPosition", "vTexCoordinate", "textureTransform")
        )
    }

    private fun setupVertexBuffer() {
        val textureCoords = Points.uV
        val squareCoords = Points.xYZ
        val drawOrder = Points.getIndex()
        val squareCoordsArray = FloatArray(squareCoords.size)
        for (i in squareCoords.indices) {
            squareCoordsArray[i] = squareCoords[i]
        }
        textureCoordsArray = FloatArray(textureCoords.size)
        for (i in textureCoords.indices) {
            textureCoordsArray!![i] = textureCoords[i]
        }
        drawOrderArray = ShortArray(drawOrder.size)
        for (i in drawOrder.indices) {
            drawOrderArray!![i] = drawOrder[i]
        }
        // Draw list buffer
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer!!.put(drawOrderArray)
        drawListBuffer!!.position(0)

        // Initialize the texture holder
        val bb = ByteBuffer.allocateDirect(squareCoordsArray.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer!!.put(squareCoordsArray)
        vertexBuffer!!.position(0)
    }

    private fun setupTexture() {
        val texturebb = ByteBuffer.allocateDirect(textureCoordsArray!!.size * 4)
        texturebb.order(ByteOrder.nativeOrder())
        textureBuffer = texturebb.asFloatBuffer()
        textureBuffer!!.put(textureCoordsArray)
        textureBuffer!!.position(0)

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("Texture generate")
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        checkGlError("Texture bind")
        if (mSurfaceTexture == null) {
            mSurfaceTexture = SurfaceTexture(textures[0])
            mSurfaceTexture?.setOnFrameAvailableListener(this)
        }
    }

    override fun draw(): Boolean {
        synchronized(this) {
            mSurfaceTexture?.updateTexImage()
            mSurfaceTexture?.getTransformMatrix(videoTextureTransform)
            frameAvailable = false
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawTexture()
        return true
    }

    private fun drawTexture() {
        GLES20.glViewport(0, 0, width, height)
        Matrix.setIdentityM(vertexTransform, 0)
        Matrix.setIdentityM(m4Rotate, 0)
        Matrix.setIdentityM(m4lookAt, 0)
        Matrix.setIdentityM(m4Perspective, 0)
        // 旋转
        if (useHeadTracker) {
            m4Rotate = nativeGetHeaderPose(headTrackApp, 2)
            MatrixUtil.m4RotateX(m4Rotate, m4Rotate, (rotationY * Math.PI / 180f).toFloat())
            MatrixUtil.m4RotateY(m4Rotate, m4Rotate, (rotationX * Math.PI / 180f).toFloat())
        } else {
            MatrixUtil.m4RotateX(m4Rotate, m4Rotate, (rotationY * Math.PI / 180f).toFloat())
            MatrixUtil.m4RotateY(m4Rotate, m4Rotate, (rotationX * Math.PI / 180f).toFloat())
        }

        // 摄像机
        val MAX_RANGE_DISTANCE = -500f
        Matrix.setLookAtM(m4lookAt, 0, 0f, 0f, distance, 0f, 0f, MAX_RANGE_DISTANCE, 0f, 1f, 0f)
        // 投影
        Matrix.perspectiveM(m4Perspective, 0, 70f, width.toFloat() / height.toFloat(), 0.1f, 1000f)
        // 合并所有矩阵
        MatrixUtil.m4Multiply(vertexTransform, m4Perspective, m4lookAt)
        MatrixUtil.m4Multiply(vertexTransform, vertexTransform, m4Rotate)
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0)
        GLES20.glUniformMatrix4fv(vertexTransformHandle, 1, false, vertexTransform, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrderArray!!.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
    }

    protected fun setupX() {
        // Draw texture
        GLES20.glUseProgram(shaderProgram)
        val textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "texture")
        val textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate")
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        textureTranformHandle = GLES20.glGetUniformLocation(shaderProgram, "textureTransform")
        vertexTransformHandle = GLES20.glGetUniformLocation(shaderProgram, "vertexTransform")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, textures[0])
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(textureParamHandle, 0)
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
    }

    override fun initGLComponents() {
        setupVertexBuffer()
        setupTexture()
        loadShaders()
        setupX()
        Points.clear()
    }

    override fun deinitGLComponents() {
        GLES20.glDeleteTextures(1, textures, 0)
        GLES20.glDeleteProgram(shaderProgram)
        mSurfaceTexture?.release()
        mSurfaceTexture?.setOnFrameAvailableListener(null)
    }

    fun checkGlError(op: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.d(TAG, op + ": glError " + GLUtils.getEGLErrorString(error))
        }
    }


    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        synchronized(this) { frameAvailable = true }
    }

    fun setRotation(rx: Float, ry: Float, rz: Float) {
        rotationX = rx % 360
        rotationY = ry
    }

    fun getRotationX(): Float {
        return rotationX
    }

    fun setRotationX(rotationX: Float) {
        var rotationX = rotationX
        rotationX %= 360f
        if (rotationX >= 360f) rotationX -= 360f
        if (rotationX < 0f) rotationX += 360f
        this.rotationX = rotationX
    }

    fun getRotationY(): Float {
        return rotationY
    }

    fun setRotationY(rotationY: Float) {
        var rotationY = rotationY
        rotationY = Math.min(rotationY, 90f)
        rotationY = Math.max(rotationY, -90f)
        this.rotationY = rotationY
    }

    fun getScale(): Float {
        return scale
    }

    // 公式  (MAX_SCALE - scale) / (MAX_SCALE - MIN_SCALE) = (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)
    fun changeScale(scale: Float) {
        val newScale = scale * this.scale
        if (newScale >= MAX_SCALE || newScale <= MIN_SCALE) return
        distance = (MAX_SCALE - newScale) / (MAX_SCALE - MIN_SCALE) * (MAX_DISTANCE - MIN_DISTANCE) + MIN_DISTANCE
    }

    fun setScale(scale: Float) {
        this.scale *= scale
        if (this.scale >= MAX_SCALE) this.scale = MAX_SCALE.toFloat() else if (this.scale <= MIN_SCALE) this.scale = MIN_SCALE.toFloat()
    }

    fun release() {
        try {
            onPause()
            GLES20.glDeleteShader(vertexShaderHandle)
            GLES20.glDeleteShader(fragmentShaderHandle)
            if (headTrackApp != -1L) {
                Log.i(TAG, "releaseHeadTrack")
                nativeOnDestroy(headTrackApp)
            }
        } catch (ignored: Exception) {
        }
    }

    fun startHeadTrack() {
        if (headTrackApp != -1L) {
            Log.i(TAG, "startHeadTrack")
            nativeOnResume(headTrackApp)
        }
    }

    fun pauseHeadTrack() {
        if (headTrackApp != -1L) {
            Log.i(TAG, "pauseHeadTrack")
            nativeOnPause(headTrackApp)
        }
    }

    companion object {
        val TAG = VideoTextureSurfaceRenderer::class.java.simpleName
        private const val MODE_3D_RATE = 1f
    }
}