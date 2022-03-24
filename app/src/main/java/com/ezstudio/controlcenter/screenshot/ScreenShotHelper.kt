package com.ezstudio.controlcenter.screenshot

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.ezstudio.controlcenter.R
import com.ezteam.baseproject.extensions.resizeBitmapByCanvas
import com.ezteam.baseproject.utils.PreferencesUtils
import java.nio.Buffer
import java.nio.ByteBuffer
import kotlin.experimental.and


class ScreenShotHelper(context: Context, windowManager: WindowManager, metrics: DisplayMetrics) {
    private val context: Context = context
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null
    private var mDensity = 0
    private var mDisplay: Display? = null
    private var mHeight = 0
    private var mWidth = 0
    var listenerScreenShot: ((Bitmap) -> Unit)? = null
    private fun initDisplay(windowManager: WindowManager, metrics: DisplayMetrics) {
//        mDensity = metrics.densityDpi
        mDensity = PreferencesUtils.getInteger(context.resources.getString(R.string.densityDpi))
        mDisplay = windowManager.defaultDisplay
    }

    fun captureScreen(resultData: Intent?, resultCode: Int) {
        if (!(resultCode == 0 || resultData == null)) {
            if (mMediaProjection != null) {
                tearDownMediaProjection()
            }
            mMediaProjectionManager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            if (mMediaProjection == null) {
                mMediaProjection =
                    mMediaProjectionManager!!.getMediaProjection(resultCode, resultData)
            }
            if (mMediaProjection != null) {
                setUpVirtualDisplay()
            }
        }
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setUpVirtualDisplay() {
        val point = Point()
        mDisplay?.getRealSize(point)
        mWidth = point.x
        mHeight = point.y
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2)
        mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
            "Screenrecorder",
            mWidth,
            mHeight,
            mDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader!!.surface,
            null,
            null
        )
        mImageReader!!.setOnImageAvailableListener(ImageAvailableListener(), null)
    }

    private inner class ImageAvailableListener() :
        ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(imageReader: ImageReader?) {
            var filePath: String? = null
            var createBitmap: Bitmap? = null
            var acquireLatestImage: Image?
            try {
                acquireLatestImage = mImageReader?.acquireLatestImage()
                if (acquireLatestImage != null) {
                    try {
                        val planes: Array<Image.Plane> = acquireLatestImage.planes
                        val buffer: Buffer = planes[0].buffer
                        val pixelStride: Int = planes[0].pixelStride
                        val rowStride: Int = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * mWidth
                        createBitmap = Bitmap.createBitmap(
                            mWidth + rowPadding / pixelStride,
                            mHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        createBitmap.copyPixelsFromBuffer(buffer)
                        createBitmap = Bitmap.createBitmap(createBitmap, 0, 0, mWidth, mHeight)
                        listenerScreenShot?.invoke(createBitmap)
                        acquireLatestImage.close()
                        stopScreenCapture()
                        tearDownMediaProjection()

                    } catch (ex: Exception) {
                        createBitmap = null
                        ex.printStackTrace()
                        if (acquireLatestImage == null) {
                            return
                        }
                        acquireLatestImage.close()
                    } catch (th4: Throwable) {
                        createBitmap = null
                        if (createBitmap != null && !createBitmap.isRecycled) {
                            createBitmap.recycle()
                        }
                        if (acquireLatestImage != null) {
                            acquireLatestImage.close()
                        }
                    }
                }
                createBitmap = null
                if (createBitmap != null && !createBitmap.isRecycled) {
                    createBitmap.recycle()
                }
                if (acquireLatestImage != null) {
                    acquireLatestImage.close()
                }
            } catch (ex: Exception) {
                acquireLatestImage = null
                ex.printStackTrace()
                if (createBitmap != null && !createBitmap.isRecycled) {
                    createBitmap.recycle()
                }
                if (acquireLatestImage == null) {
                    return
                }
                acquireLatestImage.close()
            } finally {
//                RxBusHelper.sendScreenShot(filePath)
            }
        }
    }

    private fun stopScreenCapture() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
            mImageReader?.setOnImageAvailableListener(null, null)
        }
    }

    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    init {
        initDisplay(windowManager, metrics)
    }
}
