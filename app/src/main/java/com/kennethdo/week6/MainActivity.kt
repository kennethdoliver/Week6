package com.kennethdo.week6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kennethdo.week6.ui.CampusApp
import com.kennethdo.week6.viewmodel.CampusViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val campusViewModel: CampusViewModel = viewModel()
            CampusApp(campusViewModel)
        }
    }
}
