package net.ddns.anderserver.keephome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme

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
        Column {
            SyncSettings()
            HorizontalLine()
            WiFiSettings()
            HorizontalLine()
            AdvancedSettings()
        }
    }

    @Composable
    fun SectionTitle(title: String) {
        Row(
            Modifier
                .padding(60.dp, 10.dp)
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
    fun Setting(title: String, description: String, onClick: (() -> Unit)? = null) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true),
                    onClick = { if (onClick != null) onClick() }
                )
        ) {
            Column(Modifier.padding(60.dp, 10.dp, 0.dp, 10.dp)) {
                Row {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Row {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun HorizontalLine() {
        Row(Modifier.padding(0.dp, 5.dp)) {
            Divider(
                thickness = 1.dp,
            )
        }
    }

//    @Preview
//    @Composable
//    fun SettingPreview() {
//        Setting(title = "Demo setting", description = "Description goes here")
//    }

    @Composable
    fun SyncSettings() {
        SectionTitle(title = "Sync / Notifications")
        Setting(
            title = "Enable sync notifications",
            description = "You won't be disturbed by notifications"
        )
        Setting(title = "Sync interval", description = "5 minutes")
    }

//    @Preview
//    @Composable
//    fun SyncPreview() {
//        SyncSettings()
//    }

    @Composable
    fun WiFiSettings() {
        SectionTitle(title = "KeepHome WiFi")
        Setting(
            title = "Access point mode",
            description = "KeepHome will connect to your own network"
        )
        Setting(title = "WiFi name (SSID)", description = "<Name here>")
        Setting(title = "WiFi password", description = "Password is at least 8 characters")
    }

    @Composable
    fun AdvancedSettings() {
        SectionTitle(title = "Advanced")
        Setting(title = "KeepHome IP", description = "192.168.4.1")
    }

}