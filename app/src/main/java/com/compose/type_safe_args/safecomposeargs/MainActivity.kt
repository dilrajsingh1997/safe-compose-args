package com.compose.type_safe_args.safecomposeargs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import com.compose.type_safe_args.annotation.ComposeDestination
import com.compose.type_safe_args.safecomposeargs.ui.theme.DemoScreen
import com.compose.type_safe_args.safecomposeargs.ui.theme.SafeComposeArgsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafeComposeArgsTheme {
                Surface(color = MaterialTheme.colors.background) {
                    DemoScreen()
                }
            }
        }
    }
}
