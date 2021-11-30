package net.ddns.anderserver.keephome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme
import net.ddns.anderserver.keephome.ui.theme.SettingsStore


class SettingsActivity : ComponentActivity() {

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SettingsLayout() }
    }

    @ExperimentalMaterial3Api
    @Preview(showBackground = true)
    @Composable
    fun SettingsLayout() {
        KeephomeTheme() {
            Scaffold(
                topBar = { SettingsBar() }
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SettingsContent()
                }
            }
        }
    }

    @Composable
    fun SettingsBar() {
        SmallTopAppBar(
            title = { Text(text = "Settings") },
            navigationIcon = {
                IconButton(onClick = { finish() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }

    @Composable
    fun SettingsContent() {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            SyncSettings()
            HorizontalLine()
            WiFiSettings()
            HorizontalLine()
            AdvancedSettings()
            HorizontalLine()
            SupportSettings()
        }
    }

    @Composable
    fun SectionTitle(title: String) {
        Row(
            Modifier
                .padding(80.dp, 15.dp, 0.dp, 10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

//    @Preview
//    @Composable
//    fun SectionTitlePreview() {
//        SectionTitle(title = "Title goes here")
//    }

    @Composable
    fun Setting(
        title: String,
        description: String,
        onClick: (() -> Unit)? = null,
        toggle: (@Composable () -> Unit)? = null
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true),
                    onClick = { if (onClick != null) onClick() }
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .padding(80.dp, 18.dp, 0.dp, 18.dp)
                    .weight(1f, fill = false)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (toggle != null) {
                Column(
                    Modifier.requiredWidth(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    toggle()
                }
            }
        }
    }

    @Composable
    fun ToggleSetting(
        title: String,
        enabledDescription: String,
        disabledDescription: String,
        state: Boolean,
        save: (state: Boolean) -> Unit
    ) {
        Setting(
            title = title,
            description = if (state) {
                enabledDescription
            } else {
                disabledDescription
            },
            onClick = {
                save(!state)
            }
        ) {
            Switch(
                checked = state,
                onCheckedChange = { save(!state) }
            )
        }
    }

    @Composable
    fun HorizontalLine() {
        Row(Modifier.padding(0.dp, 5.dp)) {
            Divider(
                thickness = 1.dp
            )
        }
    }

//    @Preview
//    @Composable
//    fun ToggleSettingPreview() {
//        ToggleSetting(
//            title = "Demo setting",
//            description = "Description goes here, and this is a really long one too!"
//        )
//    }

    @Composable
    fun SyncSettings() {
        val settings = SettingsStore(LocalContext.current)
        val scope = rememberCoroutineScope()
        SectionTitle(title = "Sync / Notifications")
        ToggleSetting(
            title = "Enable sync notifications",
            enabledDescription = "You will receive notifications from the app",
            disabledDescription = "You won't be disturbed by notifications",
            state = settings.getSyncNotifications.collectAsState(initial = false).value
        ) {
            scope.launch { settings.setSyncNotifications(it) }
        }
        Setting(title = "Sync interval", description = "5 minutes")
    }

//    @Preview
//    @Composable
//    fun SyncPreview() {
//        SyncSettings()
//    }

    @Composable
    fun WiFiSettings() {
        val settings = SettingsStore(LocalContext.current)
        val scope = rememberCoroutineScope()
        SectionTitle(title = "KeepHome WiFi")
        ToggleSetting(
            title = "Access point mode",
            enabledDescription = "KeepHome will connect to your own network",
            disabledDescription = "KeepHome will broadcast its own network",
            state = settings.getAPMode.collectAsState(initial = true).value
        ) {
            scope.launch { settings.setAPMode(it) }
        }
        Setting(title = "WiFi name (SSID)", description = "<Name here>")
        Setting(title = "WiFi password", description = "Password is at least 8 characters")
    }

    @Composable
    fun AdvancedSettings() {
        SectionTitle(title = "Advanced")
        Setting(title = "KeepHome IP", description = "192.168.4.1")
    }

    @Composable
    fun SupportSettings() {
        SectionTitle(title = "Support")
        Setting(title = "Website", description = "Submit issues and requests", onClick = {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://anderserver.ddns.net/blog/2021/5")
                )
            )
        })
    }


}