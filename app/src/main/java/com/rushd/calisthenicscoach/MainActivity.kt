package com.rushd.calisthenicscoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rushd.calisthenicscoach.ui.CalisthenicsApp
import com.rushd.calisthenicscoach.ui.theme.CalisthenicsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalisthenicsTheme { CalisthenicsApp() }
        }
    }
}
