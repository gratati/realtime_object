package com.programminghut.realtime_object

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.programminghut.realtime_object.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class DetectActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var videoUri: Uri
    private lateinit var surfaceView: SurfaceView
    private lateinit var imageView: ImageView
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var model: SsdMobilenetV11Metadata1
    private lateinit var toolbar: Toolbar
    private lateinit var labels: List<String>
    private val paint = Paint()

    private var mediaPlayer: MediaPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var isVideoPlaying = false
    private var videoProcessingHandler: Handler? = null
    private var videoProcessingRunnable: Runnable? = null

    private var videoWidth = 0
    private var videoHeight = 0
    private var frameSkipCounter = 0 // Счетчик для пропуска кадров

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect)

        videoUri = Uri.parse(intent.getStringExtra(MainActivity.EXTRA_VIDEO_URI) ?: return)

        surfaceView = findViewById(R.id.surfaceView)
        imageView = findViewById(R.id.imageView)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        initializeObjectDetection()

        surfaceHolder = surfaceView.holder
        surfaceHolder?.addCallback(this)

        // Listen to the global layout of the surface view to set its size correctly
        surfaceView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                override fun onGlobalLayout() {
                    surfaceView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    adjustAspectRatio(surfaceView.width, surfaceView.height)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        startVideoProcessing()
    }

    override fun onPause() {
        super.onPause()
        stopVideoProcessing()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        releaseObjectDetectionResources()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startVideoPlayback()
        // Adjust aspect ratio when the surface is created
        adjustAspectRatio(videoWidth, videoHeight)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer?.pause()
        isVideoPlaying = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startVideoPlayback() {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(this, videoUri)
            mediaPlayer?.setSurface(surfaceHolder?.surface)
            mediaPlayer?.setOnPreparedListener {
                videoWidth = it.videoWidth
                videoHeight = it.videoHeight
                adjustAspectRatio(videoWidth, videoHeight)
                mediaPlayer?.start()
                isVideoPlaying = true
            }
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeObjectDetection() {
        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        model = SsdMobilenetV11Metadata1.newInstance(this)
    }

    private fun startVideoProcessing() {
        isVideoPlaying = true
        videoProcessingHandler = Handler(Looper.getMainLooper())
        videoProcessingRunnable = object : Runnable {
            override fun run() {
                if (isVideoPlaying) {
                    frameSkipCounter++
                    if (frameSkipCounter % 3 == 0) { // Пропуск каждого третьего кадра
                        processVideoFrame()
                    }
                    videoProcessingHandler?.postDelayed(this, 33) // 30 fps
                }
            }
        }
        videoProcessingHandler?.post(videoProcessingRunnable as Runnable)
    }

    private fun stopVideoProcessing() {
        isVideoPlaying = false
        videoProcessingRunnable?.let {
            videoProcessingHandler?.removeCallbacks(it)
        }
    }

    private fun releaseObjectDetectionResources() {
        model.close()
    }

    private fun processVideoFrame() {
        mediaPlayer?.let { player ->
            val currentPosition = player.currentPosition
            player.setVolume(0.0f, 0.0f) // Выключение звука

            // Получение текущего кадра
            val bitmap = getBitmapFromMediaPlayer(player, currentPosition) ?: return

            // Уменьшение разрешения для обработки
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
            val tensorImage = TensorImage.fromBitmap(resizedBitmap)

            // Обработка изображения с помощью ImageProcessor
            val processedImage = imageProcessor.process(tensorImage)

            // Применение модели обнаружения
            val outputs = model.process(processedImage)

            // Отображение ограничивающих рамок на изображении
            val resultBitmap = drawBoundingBoxes(bitmap, outputs)
            imageView.setImageBitmap(resultBitmap)

            // Восстановление звука
            //player.setVolume(1.0f, 1.0f)//
        }
    }

    private fun getBitmapFromMediaPlayer(player: MediaPlayer, timeUs: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(this, videoUri)
            retriever.getFrameAtTime(timeUs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    private fun drawBoundingBoxes(
        bitmap: Bitmap,
        outputs: SsdMobilenetV11Metadata1.Outputs
    ): Bitmap {
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.intArray
        val scores = outputs.scoresAsTensorBuffer.floatArray

        // Получение количества обнаруженных объектов
        val numDetections = minOf(scores.size, locations.size / 5, classes.size)

        // Проход по результатам обнаружения и отображение ограничивающих рамок
        for (i in 0 until numDetections) {
            if (scores[i] < 0.5) continue // Игнорируем детекции с низкой уверенностью

            val left = locations[i * 4 + 1] * videoWidth
            val top = locations[i * 4] * videoHeight
            val right = locations[i * 4 + 3] * videoWidth
            val bottom = locations[i * 4 + 2] * videoHeight

            paint.color = Color.RED
            paint.strokeWidth = 4f
            paint.style = Paint.Style.STROKE
            canvas.drawRect(left, top, right, bottom, paint)

            val label = labels[classes[i]]
            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.style = Paint.Style.FILL
            canvas.drawText(label, left, top - 10, paint)
        }

        return resultBitmap
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        val viewWidth = surfaceView.width
        val viewHeight = surfaceView.height

        if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) return

        val aspectRatio = videoWidth.toFloat() / videoHeight
        val viewAspectRatio = viewWidth.toFloat() / viewHeight

        val newWidth: Int
        val newHeight: Int

        if (aspectRatio > viewAspectRatio) {
            newWidth = viewWidth
            newHeight = (viewWidth / aspectRatio).toInt()
        } else {
            newHeight = viewHeight
            newWidth = (viewHeight * aspectRatio).toInt()
        }

        val layoutParams = surfaceView.layoutParams
        layoutParams.width = newWidth
        layoutParams.height = newHeight
        surfaceView.layoutParams = layoutParams

        val imageLayoutParams = imageView.layoutParams
        imageLayoutParams.width = newWidth
        imageLayoutParams.height = newHeight
        imageView.layoutParams = imageLayoutParams
    }
}
