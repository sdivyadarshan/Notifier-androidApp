package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.AlwaysOnProtectionService
import com.example.ui.NotifierApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NotifierViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: NotifierViewModel by viewModels()

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      viewModel.refreshSystemPermissions(this)
      if (isGranted) {
        AlwaysOnProtectionService.start(this)
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Request notification permission on Android 13+ for persistent foreground notification
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    viewModel.refreshSystemPermissions(this)
    AlwaysOnProtectionService.start(this)

    setContent {
      MyApplicationTheme {
        NotifierApp(viewModel = viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshSystemPermissions(this)
    AlwaysOnProtectionService.start(this)
  }
}

