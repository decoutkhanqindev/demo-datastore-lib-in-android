package com.example.demodatastorelibinandroid

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.demodatastorelibinandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  private val binding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }
  
  private val viewModel: MainViewModel by viewModels<MainViewModel>(
    factoryProducer = {
      viewModelFactory {
        addInitializer(MainViewModel::class) {
          MainViewModel(application = application)
        }
      }
    }
  )
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    
    lifecycleScope.launch {
      // Runs the block of code in a coroutine when the lifecycle is at least STARTED.
      // The coroutine will be cancelled when the ON_STOP event happens and will
      // restart executing if the lifecycle receives the ON_START event again.
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiStateFlow.collect { uiState: DataStoreUiState? ->
          binding.textView.text = uiState?.toString() ?: "No data"
        }
      }
    }
    
    binding.updateBtn.setOnClickListener {
      // multiple rapid clicks will queue updates without overlapping operations.
      viewModel.updateData()
    }
    
    binding.clearBtn.setOnClickListener {
      // multiple rapid clicks will queue updates without overlapping operations.
      viewModel.clearData()
    }
  }
}