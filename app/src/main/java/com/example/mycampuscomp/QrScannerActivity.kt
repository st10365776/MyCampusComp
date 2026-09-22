package com.example.mycampuscomp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_qr_scanner)

        previewView = findViewById(R.id.previewView)

        checkCameraPermission()
    }

    private fun checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider =
                        previewView.surfaceProvider
                }

            val imageAnalyzer =
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

            imageAnalyzer.setAnalyzer(
                cameraExecutor
            ) { imageProxy ->

                val mediaImage =
                    imageProxy.image

                if (mediaImage != null) {

                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    val scanner =
                        BarcodeScanning.getClient()

                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->

                            for (barcode in barcodes) {

                                val qrValue =
                                    barcode.rawValue

                                if (!qrValue.isNullOrEmpty()) {

                                    val result =
                                        android.content.Intent()

                                    result.putExtra(
                                        "QR_RESULT",
                                        qrValue
                                    )

                                    setResult(
                                        RESULT_OK,
                                        result
                                    )

                                    finish()

                                    break
                                }
                            }
                        }
                        .addOnCompleteListener {

                            imageProxy.close()
                        }
                } else {

                    imageProxy.close()
                }
            }

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Unable to start camera",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {

        super.onDestroy()

        cameraExecutor.shutdown()
    }
}