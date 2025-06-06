package com.tomatomediacenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tomatomediacenter.ui.navigation.AppNavigation
import com.tomatomediacenter.ui.theme.TomatoMediaCenterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomatoMediaCenterTheme {
                AppNavigation()
            }
        }
    }
}
