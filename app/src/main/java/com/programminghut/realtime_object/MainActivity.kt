package com.programminghut.realtime_object

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.programminghut.realtime_object.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 123
        private const val REQUEST_CODE_VIDEO_PICKER = 456
        const val EXTRA_VIDEO_URI = "extra_video_uri"
    }

    private var selectedVideoUri: Uri? = null
    private lateinit var labels: List<String>
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var model: SsdMobilenetV11Metadata1
    private lateinit var videoView: VideoView
    private lateinit var toolbar: Toolbar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Проверяем и запрашиваем разрешение на доступ к хранилищу
        checkStoragePermission()

        // Инициализируем модель и ImageProcessor
        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        model = SsdMobilenetV11Metadata1.newInstance(this)

        // Настройка кнопок
        val btnSelectVideo = findViewById<Button>(R.id.loadVideoButton)
        btnSelectVideo.setOnClickListener { openVideoFilePicker() }

        val btnDetectObjects = findViewById<Button>(R.id.detectObjectsButton)
        btnDetectObjects.setOnClickListener { startDetectActivity() }

        val btnDetectCamera = findViewById<Button>(R.id.detectCameraButton)
        btnDetectCamera.setOnClickListener {
            val intent = Intent(this, RealtimeDetection::class.java)
            startActivity(intent)
        }

        // Инициализация VideoView
        videoView = findViewById(R.id.videoView)
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        }
    }

    private fun openVideoFilePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_VIDEO_PICKER)
    }

    private fun startDetectActivity() {
        selectedVideoUri?.let {
            val intent = Intent(this, DetectActivity::class.java)
            intent.putExtra(EXTRA_VIDEO_URI, it.toString())
            startActivity(intent)
        } ?: run {
            Toast.makeText(
                this,
                "Выберите видео для распознавания",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_VIDEO_PICKER -> {
                if (resultCode == RESULT_OK) {
                    selectedVideoUri = data?.data
                    videoView.setVideoURI(selectedVideoUri)
                    videoView.start()
                    findViewById<Button>(R.id.detectObjectsButton).isEnabled = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}

