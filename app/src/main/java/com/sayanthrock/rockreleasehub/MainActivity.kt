package com.sayanthrock.rockreleasehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sayanthrock.rockreleasehub.core.designsystem.theme.RockReleaseHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RockReleaseHubTheme {
                AppNavGraph()
            }
        }
    }
}
