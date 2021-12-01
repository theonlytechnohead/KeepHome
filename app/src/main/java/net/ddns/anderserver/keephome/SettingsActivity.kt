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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme
import net.ddns.anderserver.keephome.ui.theme.SettingsStore
import net.ddns.anderserver.keephome.ui.theme.SettingsStore.Companion.intervalMinutes


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
    fun TextSetting(
        title: String,
        text: String,
        editText: String? = null,
        save: (text: String) -> Unit
    ) {
        val showDialog = remember { mutableStateOf(false) }
        if (showDialog.value) {
            TextDialog(
                title = title,
                text = editText ?: text,
                save = { save(it) },
                onDismiss = { showDialog.value = false }
            )
        }
        Setting(
            title = title,
            description = text,
            onClick = { showDialog.value = true }
        )
    }

    @Composable
    fun PasswordSetting(
        title: String,
        password: String,
        validPassword: String,
        invalidPassword: String,
        save: (password: String) -> Unit
    ) {
        fun checkPassword(password: String): Boolean {
            return password.length >= 8
        }
        TextSetting(
            title = title,
            text = if (checkPassword(password)) {
                validPassword
            } else {
                invalidPassword
            },
            editText = password,
            save = save
        )
    }

    @Composable
    fun IPSetting(
        title: String,
        ip: String,
        save: (ip: String) -> Unit
    ) {
        fun checkIP(ip: String): Boolean {
            return Regex("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}").matches(ip)
        }

        val showDialog = remember { mutableStateOf(false) }
        if (showDialog.value) {
            VerifiableDialog(
                title = title,
                text = ip,
                verify = { checkIP(it) },
                save = { save(it) },
                onDismiss = { showDialog.value = false }
            )
        }
        Setting(
            title = title,
            description = ip,
            onClick = { showDialog.value = true }
        )
    }

    @Composable
    fun SingleSelectionSetting(
        title: String,
        value: Int,
        values: List<Int>,
        descriptionSingular: String,
        descriptionPlural: String,
        save: (text: Int) -> Unit
    ) {
        val showDialog = remember { mutableStateOf(false) }
        val options = mutableListOf<String>()
        values.forEach {
            options.add(
                if (it == 1) {
                    "$it $descriptionSingular"
                } else {
                    "$it $descriptionPlural"
                }
            )
        }
        if (showDialog.value) {
            SingleSelectionDialog(
                title = title,
                value = values.indexOf(value),
                options = options,
                save = { save(it) },
                onDismiss = { showDialog.value = false }
            )
        }
        Setting(
            title = title,
            description = if (value == 1) {
                "$value $descriptionSingular"
            } else {
                "$value $descriptionPlural"
            },
            onClick = { showDialog.value = true }
        )
    }

    @Composable
    fun TextDialog(
        title: String,
        text: String,
        save: (text: String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var textField by remember { mutableStateOf(text) }
        AlertDialog(
            title = { Text(text = title) },
            text = {
                TextField(
                    value = textField,
                    onValueChange = {
                        textField = it
                        save(it)
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "OK")
                }
            },
            onDismissRequest = onDismiss,
            dismissButton = {}
        )
    }

    @Composable
    fun VerifiableDialog(
        title: String,
        text: String,
        verify: (text: String) -> Boolean,
        save: (text: String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var textField by remember { mutableStateOf(text) }
        AlertDialog(
            title = { Text(text = title) },
            text = {
                Column {
                    TextField(
                        value = textField,
                        onValueChange = {
                            textField = it
                            save(it)
                        }
                    )
                    if (!verify(textField)) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            text = "Invalid IP address!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                if (verify(textField)) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "OK")
                    }
                }
            },
            onDismissRequest = {
                if (verify(textField))
                    onDismiss()
            },
            dismissButton = {}
        )
    }

    @Composable
    fun SingleSelectionDialog(
        title: String,
        value: Int,
        options: List<String>,
        save: (interval: Int) -> Unit,
        onDismiss: () -> Unit
    ) {
        val (selectedOption, onOptionsSelected) = remember { mutableStateOf(options[value]) }
        AlertDialog(
            title = { Text(text = title) },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach {
                        Row(Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (it == selectedOption),
                                onClick = {
                                    onOptionsSelected(it)
                                    save(options.indexOf(it))
                                }
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (it == selectedOption),
                                onClick = {
                                    onOptionsSelected(it)
                                    save(options.indexOf(it))
                                })
                            Text(
                                text = it,
                                Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "OK")
                }
            },
            onDismissRequest = onDismiss,
            dismissButton = {}
        )
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

        SingleSelectionSetting(
            title = "Sync interval",
            value = intervalMinutes[settings.getNotificationInterval.collectAsState(initial = 1).value],
            values = intervalMinutes,
            descriptionSingular = "minute",
            descriptionPlural = "minutes"
        ) {
            scope.launch { settings.setNotificationInterval(it) }
        }
    }

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
        TextSetting(
            title = "WiFi name (SSID)",
            text = settings.getSSID.collectAsState(initial = "KeepHome").value,
        ) {
            scope.launch { settings.setSSID(it) }
        }
        PasswordSetting(
            title = "WiFi password",
            password = settings.getPassword.collectAsState(initial = "123345578").value,
            validPassword = "Password is at least 8 characters",
            invalidPassword = "Password must be at least 8 characters"
        ) {
            scope.launch { settings.setPassword(it) }
        }
    }

    @Composable
    fun AdvancedSettings() {
        val settings = SettingsStore(LocalContext.current)
        val scope = rememberCoroutineScope()
        SectionTitle(title = "Advanced")
        IPSetting(title = "KeepHome IP", ip = "192.168.4.1") {
            scope.launch { settings.setIP(it) }
        }
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