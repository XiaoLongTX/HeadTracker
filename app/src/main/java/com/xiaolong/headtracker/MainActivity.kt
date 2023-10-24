package com.xiaolong.headtracker

import android.app.Activity
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.xiaolong.sdk.render.VideoTextureSurfaceRenderer

class MainActivity : Activity() {
    private var exoPlayer: ExoPlayer? = null
    private var textureView: TextureView? = null
    var renderer: VideoTextureSurfaceRenderer? = null
    var renderSurface: Surface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView = findViewById<TextureView>(R.id.texture_view)
        textureView!!.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.i(TAG, "onSurfaceTextureAvailable")
                renderer = VideoTextureSurfaceRenderer(this@MainActivity, surface, width, height)
                renderSurface = Surface(renderer!!.mSurfaceTexture)
                exoPlayer!!.setVideoSurface(renderSurface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                renderer!!.resetSize(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                renderer!!.release()
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
        // 创建一个DefaultRenderersFactory实例，并将其设置为player的渲染器工厂
        val renderersFactory = DefaultRenderersFactory(this)
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        // 创建一个SimpleExoPlayer实例
        exoPlayer = ExoPlayer.Builder(this).setRenderersFactory(renderersFactory).build()

        // 创建一个MediaSource实例，用于加载视频数据
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(
                this,
                DefaultHttpDataSource.Factory().setUserAgent("exoplayer")
                    .setAllowCrossProtocolRedirects(true)
            )
        )
            .createMediaSource(MediaItem.fromUri("http://gdc.kg.qq.com/50159/3010_s_0bc3v6mf6aaasmal5rsywnso7l6dl5iqbqca.f0.mp4?dis_k=2149fa7dff934bbb048f1a85ccc62ed6&dis_t=1683546437"))

        // 准备播放器并开始播放
        exoPlayer!!.setMediaSource(mediaSource)
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}