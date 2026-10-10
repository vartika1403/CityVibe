package com.cityvibe.application.ui.createshow

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.cityvibe.application.ui.enableCityVibeEdgeToEdge

/** Hosts [CreateShowScreen]; returns RESULT_OK once a show is submitted so Home can refresh. */
class CreateShowActivity : ComponentActivity() {

    private val viewModel: CreateShowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCityVibeEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CreateShowScreen(
                viewModel = viewModel,
                onBack = ::finish,
                onSubmitted = {
                    setResult(Activity.RESULT_OK)
                    finish()
                },
            )
        }
    }
}
