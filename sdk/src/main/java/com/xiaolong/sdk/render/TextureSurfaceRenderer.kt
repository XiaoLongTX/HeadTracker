package com.xiaolong.sdk.render

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLUtils
import android.util.Log
import com.xiaolong.sdk.HeaderTrackerUtil.nativeOnCreate
import com.xiaolong.sdk.HeaderTrackerUtil.nativeOnResume
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

abstract class TextureSurfaceRenderer(
    var surfaceTexture: SurfaceTexture, protected var width: Int,
    protected var height: Int
) : Runnable {
    private var egl: EGL10? = null
    private var eglContext: EGLContext? = null
    private var eglDisplay: EGLDisplay? = null
    private var eglSurface: EGLSurface? = null
    private var running = false
    protected var headTrackApp: Long = -1
    protected var useHeadTracker = true

    init {
        running = true
        val thread = Thread(this)
        thread.start()
    }

    fun resetSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun run() {
        Log.d(LOG_TAG, "OpenGL init OK. start draw...")
        initEGL()
        initGLComponents()
        headTrackApp = nativeOnCreate()
        nativeOnResume(headTrackApp)
        while (running) {
            if (draw()) {
                egl!!.eglSwapBuffers(eglDisplay, eglSurface)
            }
            try {
                Thread.sleep(20)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        deinitGLComponents()
        deinitEGL()
    }

    private fun initEGL() {
        egl = EGLContext.getEGL() as EGL10
        eglDisplay = egl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        egl!!.eglInitialize(eglDisplay, version)
        val eglConfig = chooseEglConfig()
        eglSurface = egl!!.eglCreateWindowSurface(
            eglDisplay, eglConfig,
            surfaceTexture, null
        )
        eglContext = createContext(egl, eglDisplay, eglConfig)
        try {
            if (eglSurface == null || eglSurface === EGL10.EGL_NO_SURFACE) {
                throw RuntimeException(
                    "GL error:"
                            + GLUtils.getEGLErrorString(egl!!.eglGetError())
                )
            }
            if (!egl!!.eglMakeCurrent(
                    eglDisplay, eglSurface, eglSurface,
                    eglContext
                )
            ) {
                throw RuntimeException(
                    "GL Make current Error"
                            + GLUtils.getEGLErrorString(egl!!.eglGetError())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deinitEGL() {
        egl!!.eglMakeCurrent(
            eglDisplay, EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT
        )
        egl!!.eglDestroySurface(eglDisplay, eglSurface)
        egl!!.eglDestroyContext(eglDisplay, eglContext)
        egl!!.eglTerminate(eglDisplay)
        Log.d(LOG_TAG, "OpenGL deinit OK.")
    }

    protected abstract fun draw(): Boolean
    protected abstract fun initGLComponents()
    protected abstract fun deinitGLComponents()
    private fun createContext(
        egl: EGL10?, eglDisplay: EGLDisplay?,
        eglConfig: EGLConfig?
    ): EGLContext {
        val attrs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        return egl!!.eglCreateContext(
            eglDisplay, eglConfig,
            EGL10.EGL_NO_CONTEXT, attrs
        )
    }

    private fun chooseEglConfig(): EGLConfig? {
        val configsCount = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        val attributes = attributes
        val confSize = 1
        require(
            egl!!.eglChooseConfig(
                eglDisplay, attributes, configs, confSize,
                configsCount
            )
        ) {
            ("Failed to choose config:"
                    + GLUtils.getEGLErrorString(egl!!.eglGetError()))
        }
        return if (configsCount[0] > 0) {
            configs[0]
        } else null
    }

    private val attributes: IntArray
        get() = intArrayOf(
            EGL10.EGL_RENDERABLE_TYPE,
            EGL14.EGL_OPENGL_ES2_BIT,
            EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_STENCIL_SIZE, 0,
            EGL10.EGL_NONE
        )

    /**
     * Call when activity pauses. This stops the rendering thread and
     * deinitializes OpenGL.
     */
    fun onPause() {
        running = false
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        running = false
    }

    companion object {
        var LOG_TAG = TextureSurfaceRenderer::class.java.simpleName
    }
}