@file:OptIn(ExperimentalPermissionsApi::class)

package com.bass9030.easymessage

import android.Manifest
import android.R
import android.R.attr.permission
import android.R.attr.phoneNumber
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bass9030.easymessage.ui.theme.EasyMessageTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
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
                    val permissionStates = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_CONTACTS
                        )
                    )

                    NavScreen(navController = navController)
                    Log.d("isPermissionGranted", if (permissionStates.allPermissionsGranted) "true" else "false")
                    if(!permissionStates.allPermissionsGranted) {
                        navController.navigate("check_perm") {
                            popUpTo("chat") { inclusive = true }
                        }
                    }
                    else if (prefs.getString("gender", "").isEmpty()) {
                        navController.navigate("gender_sel") {
                            popUpTo("chat") { inclusive = true }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun NavScreen(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chat") {
        composable("chat") {
            Chat(navController = navController)
        }

        composable("check_perm") {
            CheckPerm(navController = navController)
        }

        composable("gender_sel") {
            GenderSelection(navController = navController)
        }
    }
}

fun OnChatClicked(context: Context) {
    //TODO: STT 구현
    val listener: RecognitionListener = object : RecognitionListener {
        // 말하기 시작할 준비가되면 호출
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(context, "음성인식 시작", Toast.LENGTH_SHORT).show()
//            binding.tvState.text = "이제 말씀하세요!"
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
//            binding.tvState.text = "잘 듣고 있어요."
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
//            binding.tvState.text = "끝!"
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            var result = "";
            for (i in matches!!.indices) result += matches[i]
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
            sendMMS(context, "신한카드 승인안내", result)

        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, R.attr.packageNames)    // 여분의 키
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")         // 언어 설정

    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    speechRecognizer.setRecognitionListener(listener)    // 리스너 설정
    speechRecognizer.startListening(intent)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPerm(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
    )

    Column (horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("해당 앱은 사용자의 음성 인식, 문자 전송, 연락처 검색을 위한 권한이 필요합니다." +
                "\n권한을 허용해주세요.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            permissionStates.launchMultiplePermissionRequest()
//            if(permissionStates.allPermissionsGranted) {
//                if (MainActivity.prefs.getString("gender", "").isEmpty()) {
//                    navController.navigate("gender_sel") {
//                        popUpTo("check_perm") { inclusive = true }
//                    }
//                } else navController.navigate("chat") {
//                    popUpTo("check_perm") { inclusive = true }
//                }
//            }else{
//                Toast.makeText(context, "앱 사용을 위해 마이크, 연락처및 SMS 권한을 허용해 주세요.", Toast.LENGTH_LONG)
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                intent.putExtra("package", R.attr.packageNames)
//                ContextCompat.startActivity(context, intent, null)
//            }
        }) {
            Text("권한 부여하기")
        }
    }
}

private fun sendMMS(context: Context, to: String, message: String) {
    val sms: SmsManager = context.getSystemService(SmsManager::class.java)
    val phoneNumber = getPhoneNumberByName(context, to)
    sms.sendTextMessage(phoneNumber, null, message, null, null)
}

private fun getPhoneNumberByName(context: Context, contactName: String): String? {
    var phoneNumber: String? = null
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?"
    val selectionArgs = arrayOf(contactName)
    val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    if (cursor != null && cursor.moveToFirst()) {
        phoneNumber =
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
        cursor.close()
    }
    return phoneNumber
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
        Button(onClick = { OnChatClicked(context = context) }, modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()) {
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
                    popUpTo("gender_sel") { inclusive = true }
                }
            }) {
                Text("남성")
            }
            Spacer(modifier = Modifier.width(5.dp))
            Button(onClick = {
                prefs.setString("gender", "여성")
                navController.navigate("chat") {
                    popUpTo("gender_sel") { inclusive = true }
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

