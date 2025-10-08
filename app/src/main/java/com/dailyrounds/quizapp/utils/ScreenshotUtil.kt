package com.dailyrounds.quizapp.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

object ScreenshotUtil {
    
    private const val TAG = "ScreenshotUtil"
    
    /**
     * Render any composable offscreen to a bitmap and share it.
     * Automatically wraps content with app logo header and footer.
     */
    fun captureAndShareComposable(
        context: Context,
        widthPx: Int,
        heightPx: Int = 0,
        content: @Composable () -> Unit
    ) {
        try {
            Log.d(TAG, "Starting screenshot capture, widthPx: $widthPx")
            
            val activity = context.findActivity()
            if (activity == null) {
                Toast.makeText(context, "Unable to find activity", Toast.LENGTH_SHORT).show()
                return
            }
            
            val composeView = ComposeView(context).apply {
                setContent { 
                    ScreenshotWrapper {
                        content()
                    }
                }
                layoutParams = ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(composeView)
            
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
                    val heightSpec = if (heightPx > 0) {
                        View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
                    } else {
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    }
                    
                    composeView.measure(widthSpec, heightSpec)
                    val actualHeight = if (heightPx > 0) heightPx else composeView.measuredHeight
                    composeView.layout(0, 0, widthPx, actualHeight)
                    
                    Log.d(TAG, "Creating bitmap: ${widthPx}x${actualHeight}")
                    
                    val bitmap = Bitmap.createBitmap(widthPx, actualHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    composeView.draw(canvas)
                    
                    rootView.removeView(composeView)
                    
                    Log.d(TAG, "Bitmap created successfully")
                    
                    val file = saveBitmap(context, bitmap)
                    Log.d(TAG, "File saved: ${file.absolutePath}")
                    
                    shareImage(context, file)
                    Log.d(TAG, "Share intent launched")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in delayed capture", e)
                    rootView.removeView(composeView)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }, 100)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing/sharing screenshot", e)
            Toast.makeText(context, "Error sharing result: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "QuizApp")
        if (!dir.exists()) dir.mkdirs()
        
        val file = File(dir, "quiz_result_${System.currentTimeMillis()}.png")
        file.outputStream().use { 
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file
    }

    private fun shareImage(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share Quiz Result"
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }
}

/**
 * Wrapper composable that adds logo header and footer to any content
 */
@Composable
private fun ScreenshotWrapper(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        QuizAppLogo()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ScreenshotFooter()
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuizAppLogo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📚",
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "QuizApp",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ScreenshotFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Made with QuizApp",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = "Test your knowledge daily!",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}