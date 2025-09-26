package com.dailyrounds.quizapp.helper

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

object Utils {

    val isLandscape: Boolean
        @Composable get() {
            val configuration = LocalConfiguration.current
            return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
}