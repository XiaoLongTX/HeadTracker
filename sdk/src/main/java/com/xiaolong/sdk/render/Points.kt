package com.xiaolong.sdk.render

object Points {
    private val pp = ArrayList<Float>(10000)
    private val tt = ArrayList<Float>(10000)
    private val index = ArrayList<Short>(40000)
    private const val r = 400f
    private const val step = 5f
    private var i = 0f
    private var ii = 0f
    private var j = 0f
    private var jj = 0f
    private var sinj = 0f
    private var cosj = 0f
    private var sini = 0f
    private var cosi = 0f
    private var x = 0f
    private var y = 0f
    private var z = 0f
    private var initFlat = false
    private fun init() {
        pp.clear()
        tt.clear()
        j = -90f
        jj = 90f
        while (j <= jj) {
            i = 0f
            ii = 360f
            while (i <= ii) {
                sini = Math.sin(i / 180 * Math.PI).toFloat()
                cosi = Math.cos(i / 180 * Math.PI).toFloat()
                sinj = Math.sin(j / 180 * Math.PI).toFloat()
                cosj = Math.cos(j / 180 * Math.PI).toFloat()
                y = r * sinj
                x = r * cosj * cosi
                z = r * cosj * sini
                pp.add(x)
                pp.add(y)
                pp.add(z)
                tt.add(i / 360)
                tt.add(j / 90 / 2 + 0.5f)
                i += step
            }
            j += step
        }
        index.clear()
        val jstep = (360 / step + 1).toInt().toShort()
        i = 0f
        ii = 360 / step
        while (i < ii) {
            j = 0f
            jj = 180 / step
            while (j < jj) {
                when (j) {
                    0f -> {
                        index.add((j * jstep + i).toInt().toShort())
                        index.add(((j + 1) * jstep + i + 1).toInt().toShort())
                        index.add(((j + 1) * jstep + i).toInt().toShort())
                    }

                    jj - 1 -> {
                        index.add((j * jstep + i).toInt().toShort())
                        index.add((j * jstep + i + 1).toInt().toShort())
                        index.add(((j + 1) * jstep + i + 1).toInt().toShort())
                    }

                    else -> {
                        index.add((j * jstep + i).toInt().toShort())
                        index.add((j * jstep + i + 1).toInt().toShort())
                        index.add(((j + 1) * jstep + i + 1).toInt().toShort())
                        index.add((j * jstep + i).toInt().toShort())
                        index.add(((j + 1) * jstep + i + 1).toInt().toShort())
                        index.add(((j + 1) * jstep + i).toInt().toShort())
                    }
                }
                j++
            }
            ++i
        }
        initFlat = true
    }

    val xYZ: ArrayList<Float>
        get() {
            if (!initFlat) init()
            return pp
        }
    val uV: ArrayList<Float>
        get() {
            if (!initFlat) init()
            return tt
        }

    fun getIndex(): ArrayList<Short> {
        if (!initFlat) init()
        return index
    }

    fun clear() {
        pp.clear()
        tt.clear()
        index.clear()
        initFlat = false
    }
}