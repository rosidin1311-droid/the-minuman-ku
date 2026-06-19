package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MinumanKuApp
import com.example.ui.MinumanKuViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MinumanKuViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MinumanKuApp(viewModel = viewModel)
    }
  }
}
