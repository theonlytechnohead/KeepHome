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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.druk.rx2dnssd.BonjourService
import com.github.druk.rx2dnssd.Rx2DnssdBindable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme
import net.ddns.anderserver.keephome.ui.theme.SettingsStore
import org.json.JSONObject


class MainActivity : ComponentActivity() {

    enum class ContentState {
        BLANK, ERROR, INFO
    }

    private val state = mutableStateOf(ContentState.BLANK)
    private val online = mutableStateOf(false)
    private var result = "{}"

    private lateinit var browseDisposable: Disposable
    private lateinit var rx2dnssd: Rx2DnssdBindable


    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Layout()
        }
        rx2dnssd = Rx2DnssdBindable(applicationContext)
        val settings = SettingsStore(applicationContext)
        browseDisposable = rx2dnssd.browse(
            "_http._tcp",
            "local."
        )
            .compose(rx2dnssd.resolve())
            .compose(rx2dnssd.queryIPV4Records())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { service: BonjourService ->
                if (service.serviceName.contains("KeepHome")) {
                    if (!service.isLost) {
                        val ip = service.inet4Address!!.hostAddress ?: "192.168.4.1"
                        lifecycleScope.launch {
                            settings.setIP(ip)
                        }
                        online.value = true
                    } else {
                        online.value = false
                    }
                }
            }
    }


    @ExperimentalMaterial3Api
    @Preview(showBackground = true)
    @Composable
    fun Layout() {
        KeephomeTheme {
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
        if (online.value)
            Text(text = "Online", color = MaterialTheme.colorScheme.secondary)
        else
            Text(text = "Offline", color = MaterialTheme.colorScheme.outline)
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
                    state.value = ContentState.INFO
                    result = it
                },
                {
                    state.value = ContentState.ERROR
                    result = it.toString()
                }
            )
        )
    }

    @Composable
    fun Content() {
        when (state.value) {
            ContentState.BLANK -> {
                Column(
                    Modifier
                        .padding(10.dp)
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Use the 'Refresh' button to get info",
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            ContentState.ERROR -> {
                Column(
                    Modifier
                        .padding(10.dp)
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error!", fontWeight = FontWeight.Bold)
                    Text(text = result, fontStyle = FontStyle.Italic)
                }
            }
            ContentState.INFO -> {
                val json = JSONObject(result)
                var time = ""
                if (json.has("time")) time = json["time"].toString()
                var additional = ""
                if (json.has("additional")) additional = json["additional"].toString()
                Column(
                    Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Uptime: ${time}s")
                    Text(text = "Additional info: $additional")
                }
            }
        }
    }

}