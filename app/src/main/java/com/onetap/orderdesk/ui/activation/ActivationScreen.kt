package com.onetap.orderdesk.ui.activation

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onetap.orderdesk.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(onActivationSuccess: (String) -> Unit = {}) {
    var activationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("onetap_order_desk_prefs", Context.MODE_PRIVATE) }
    val deviceId = remember {
        var id = sharedPrefs.getString("device_id", null)
        if (id.isNullOrEmpty()) { id = UUID.randomUUID().toString(); sharedPrefs.edit().putString("device_id", id).apply() }
        id
    }

    fun handleActivation() {
        val trimmed = activationCode.trim()
        if (trimmed.isEmpty()) { errorMessage = "Please enter an activation code"; return }
        errorMessage = null
        isLoading = true
        keyboardController?.hide()

        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                try {
                    val endpointUrl = "https://script.google.com/macros/s/AKfycbw-LDpewbHQCvOb_mgPr-NDEs1shbRuiAmvWaylfwJZisphtJD8rcgSVZT6yxe-51FTSA/exec"
                    val url = URL(endpointUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        doOutput = true
                    }
                    val payload = JSONObject().apply { put("deviceId", deviceId); put("appVersion", "1.0"); put("activationCode", trimmed) }.toString()
                    OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(payload); it.flush() }
                    val responseCode = connection.responseCode
                    var responseBody = ""
                    if (responseCode == 302 || responseCode == 303 || responseCode == 307) {
                        val redirectLocation = connection.getHeaderField("Location")
                        connection.disconnect()
                        if (!redirectLocation.isNullOrEmpty()) {
                            val redirectConn = (URL(redirectLocation).openConnection() as HttpURLConnection).apply { requestMethod = "GET" }
                            responseBody = BufferedReader(InputStreamReader(redirectConn.inputStream)).use { it.readText() }
                            redirectConn.disconnect()
                        }
                    } else {
                        responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    }
                    val json = JSONObject(responseBody)
                    Pair(json.optString("status") == "success", json.optString("message", "Activated"))
                } catch (e: Exception) { Pair(false, e.localizedMessage ?: "Error") } finally { connection?.disconnect() }
            }
            isLoading = false
            if (result.first) {
                isSuccess = true
                sharedPrefs.edit().putBoolean("isActivated", true).putString("activation_code", trimmed).apply()
                Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                onActivationSuccess(trimmed)
            } else {
                errorMessage = result.second
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(TechNavy).statusBarsPadding().navigationBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("OneTap", color = GoldAccent, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
            Text("ORDER DESK", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(40.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavySurface), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(value = activationCode, onValueChange = { activationCode = it.uppercase() }, label = { Text("Activation Code") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { handleActivation() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)) {
                        if (isLoading) CircularProgressIndicator(color = TechNavy, modifier = Modifier.size(24.dp)) else Text("ACTIVATE APP", color = TechNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Developed by Imran", color = TextGrey, fontSize = 12.sp, fontStyle = FontStyle.Italic)
        }
    }
}
