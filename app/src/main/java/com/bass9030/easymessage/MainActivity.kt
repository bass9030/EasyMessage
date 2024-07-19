package com.bass9030.easymessage

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bass9030.easymessage.ui.theme.EasyMessageTheme

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate(savedInstanceState)
        setContent {
            EasyMessageTheme {
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavScreen(navController = navController)
                    if (prefs.getString("gender", "").isEmpty()) {
                        navController.navigate("gender_sel") {
                            popUpTo("chat")
                        }
                    }

                }
            }
        }
    }
}
fun RequestPermission(context: Context) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.RECORD_AUDIO), 0)
    }
}

@Composable
fun NavScreen(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chat") {
        composable("chat") {

        }

        composable("gender_sel") {
            GenderSelection(navController = navController)
        }
    }
}

fun OnChatClicked(context: Context) {

}

@Composable
fun Chat(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current;
    // TODO: init gemini model
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween) {
        Text(text = "AI의 응답이 담길 예정입니다.",
            fontSize = 25.sp,
            modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(5.dp)
            .weight(1f, false))
        Button(onClick = { OnChatClicked(context = context) }, modifier = Modifier.padding(5.dp).fillMaxWidth()) {
            Text("대화하기", fontSize = 20.sp)
        }
    }
}

@Composable
fun GenderSelection(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current;
    val prefs = PreferenceUtil(context)


    Column(modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(text = "성별을 선택해 주세요.")
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Button(onClick = {
                prefs.setString("gender", "남성")
                navController.navigate("chat") {
                    popUpTo("gender_sel")
                }
            }) {
                Text("남성")
            }
            Spacer(modifier = Modifier.width(5.dp))
            Button(onClick = {
                prefs.setString("gender", "여성")
                navController.navigate("chat") {
                    popUpTo("gender_sel")
                }
            }) {
                Text("여성")
            }
        }
    }
}


// Previews
@Composable
@Preview
fun PreviewGenderSelection() {
    EasyMessageTheme {
        GenderSelection(navController = rememberNavController())
    }
}

@Composable
@Preview
fun PreviewChat() {
    EasyMessageTheme {
        Chat(navController = rememberNavController())
    }
}

@Composable
@Preview
fun PreviewNavScreen() {
    EasyMessageTheme {
        NavScreen(navController = rememberNavController())
    }
}

