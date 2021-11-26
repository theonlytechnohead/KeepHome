package net.ddns.anderserver.keephome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.ddns.anderserver.keephome.ui.theme.KeephomeTheme

class MainActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Layout()
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun Layout() {
    KeephomeTheme {
        Scaffold(
            topBar = { CenterAlignedTopAppBar(
                title = { Text(text = "KeepHome")}
            ) }
        ) {
            Column {

                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        Row(Modifier.padding(10.dp)) {
                            Greeting(name = "Jean")
                        }
                        Row(Modifier.padding(10.dp)) {
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "Click me!")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}
