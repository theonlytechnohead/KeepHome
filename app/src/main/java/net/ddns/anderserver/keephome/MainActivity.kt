package net.ddns.anderserver.keephome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme

class MainActivity : ComponentActivity() {

    val result = mutableStateOf("")

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Layout()
        }
    }


    @ExperimentalMaterial3Api
    @Preview(showBackground = true)
    @Composable
    fun Layout() {
        KeephomeTheme() {
            Scaffold(
                topBar = { ActionBar() }
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        ButtonBar()
                        Content()
                    }
                }
            }
        }
    }

    @Composable
    fun ActionBar() {
        SmallTopAppBar(
            title = { Text(text = "KeepHome") },
            actions = { AppBarActions() }
        )
    }

    @Composable
    fun AppBarActions() {
        val context = LocalContext.current
        IconButton(onClick = {
            context.startActivity(
                Intent(
                    context,
                    SettingsActivity::class.java
                )
            )
        }) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }

    @Composable
    fun ButtonBar() {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(10.dp, 0.dp)
                .fillMaxWidth()
        ) {
            Button(onClick = {
                refreshContent()
            }) {
                Text(text = "Refresh")
            }
        }
    }

    fun refreshContent() {
        result.value = "Result!"
    }

    @Composable
    fun Content() {
        val text by result
        Row(Modifier.padding(10.dp)) {
            Text(text = text)
        }
    }

}