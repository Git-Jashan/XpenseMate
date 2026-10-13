package com.jsingh.XpenseMate.ui.components

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class UpiData(
    val merchantName: String,
    val merchantVpa: String,
    val merchantCode: String,
    val amount: String
)

private fun parseUpiUri(rawUri: String): UpiData {
    return try {
        val parsedUri = Uri.parse(rawUri)
        val rawName = parsedUri.getQueryParameter("pn") ?: ""

        // Handles URL encoding (%20, %26) and legacy plus-sign encoding safely
        val decodedName = URLDecoder.decode(rawName, StandardCharsets.UTF_8.name())
            .replace("+", " ")

        UpiData(
            merchantName = decodedName,
            merchantVpa = parsedUri.getQueryParameter("pa") ?: "",
            merchantCode = parsedUri.getQueryParameter("mc") ?: "",
            amount = parsedUri.getQueryParameter("am") ?: ""
        )
    } catch (e: Exception) {
        e.printStackTrace()
        UpiData("", "", "", "")
    }
}

@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onQrScanned: (merchantName: String, merchantVpa: String, merchantCode: String, amount: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val image = InputImage.fromFilePath(context, selectedUri)
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                )

                scanner.process(image).addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null && rawValue.startsWith("upi://")) {
                            val upiData = parseUpiUri(rawValue)
                            onQrScanned(
                                upiData.merchantName,
                                upiData.merchantVpa,
                                upiData.merchantCode,
                                upiData.amount
                            )
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Camera Layer
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), QrCodeAnalyzer { qrText ->
                                    val upiData = parseUpiUri(qrText)
                                    onQrScanned(
                                        upiData.merchantName,
                                        upiData.merchantVpa,
                                        upiData.merchantCode,
                                        upiData.amount
                                    )
                                })
                            }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Transparent Mask and Target Brackets
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val boxSize = 320.dp.toPx()
                val left = (canvasWidth - boxSize) / 2
                val top = (canvasHeight - boxSize) / 2
                val right = left + boxSize
                val bottom = top + boxSize

                val overlayColor = Color.Black.copy(alpha = 0.75f)

                drawRect(color = overlayColor, topLeft = Offset(0f, 0f), size = Size(canvasWidth, top))
                drawRect(color = overlayColor, topLeft = Offset(0f, bottom), size = Size(canvasWidth, canvasHeight - bottom))
                drawRect(color = overlayColor, topLeft = Offset(0f, top), size = Size(left, boxSize))
                drawRect(color = overlayColor, topLeft = Offset(right, top), size = Size(canvasWidth - right, boxSize))

                val strokeWidth = 4.dp.toPx()
                val bracketLength = 32.dp.toPx()
                val bracketColor = Color.White

                drawLine(bracketColor, Offset(left, top), Offset(left + bracketLength, top), strokeWidth)
                drawLine(bracketColor, Offset(left, top), Offset(left, top + bracketLength), strokeWidth)

                drawLine(bracketColor, Offset(right, top), Offset(right - bracketLength, top), strokeWidth)
                drawLine(bracketColor, Offset(right, top), Offset(right, top + bracketLength), strokeWidth)

                drawLine(bracketColor, Offset(left, bottom), Offset(left + bracketLength, bottom), strokeWidth)
                drawLine(bracketColor, Offset(left, bottom), Offset(left, bottom - bracketLength), strokeWidth)

                drawLine(bracketColor, Offset(right, bottom), Offset(right - bracketLength, bottom), strokeWidth)
                drawLine(bracketColor, Offset(right, bottom), Offset(right, bottom - bracketLength), strokeWidth)
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 18.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Rounded.Image, contentDescription = "Gallery", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Upload from Gallery", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private class QrCodeAnalyzer(private val onQrScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )
    // AtomicBoolean prevents background multi-thread race conditions during analysis latches
    private val canScan = AtomicBoolean(true)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && canScan.compareAndSet(true, false)) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    var qrFound = false

                    for (barcode in barcodes) {
                        val text = barcode.rawValue
                        if (text != null && text.startsWith("upi://")) {
                            qrFound = true
                            onQrScanned(text)
                            break // Safely breaks the loop now
                        }
                    }

                    // Only re-enable scanning if NO valid UPI QR match was found in this frame
                    if (!qrFound) {
                        canScan.set(true)
                    }
                }
                .addOnFailureListener {
                    // If processing fails, reset latch so it can try next frame
                    canScan.set(true)
                }
                .addOnCompleteListener {
                    // CRUCIAL: Always close the imageProxy to prevent memory leaks and camera freeze
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
