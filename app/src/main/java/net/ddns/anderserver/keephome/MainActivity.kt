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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme
import net.ddns.anderserver.keephome.ui.theme.SettingsStore
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private var result = mutableStateOf("{}")

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
        val settings = SettingsStore(LocalContext.current)
        val ip = settings.getIP.collectAsState(initial = "192.168.4.1").value
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(10.dp, 0.dp)
                .fillMaxWidth()
        ) {
            Button(onClick = {
                refreshContent(ip)
            }) {
                Text(text = "Refresh")
            }
        }
    }

    private fun refreshContent(address: String) {
        val queue = Volley.newRequestQueue(this)
        queue.add(
            StringRequest(
                Request.Method.POST,
                "http://$address/post",
                {
                    result.value = it
                },
                {
                    result.value = "Error: $it"
                }
            )
        )
    }

    @Composable
    fun Content() {
        val json = JSONObject(result.value)
        var time = ""
        if (json.has("time")) time = json["time"].toString()
        var additional = ""
        if (json.has("additional")) additional = json["additional"].toString()
        Row(Modifier.padding(10.dp)) {
            Text(text = "Uptime: ${time}s")
        }
        Row(Modifier.padding(10.dp)) {
            Text(text = "Additional info: $additional")
        }
    }

}