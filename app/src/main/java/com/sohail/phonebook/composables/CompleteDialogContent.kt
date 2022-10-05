package com.sohail.phonebook.composables

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.sohail.phonebook.utils.getActivity
import java.util.concurrent.TimeUnit

val auth = FirebaseAuth.getInstance()
var storedVerificationId: String = ""

@Composable
fun CompleteDialogContent(navController: NavHostController) {
    val context = LocalContext.current
    var phoneNumber by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var otp by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var isOtpVisible by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth(1f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(1f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Login with phone number", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextField(
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White
                ),
                placeholder = { Text("Enter phone number") },
                value = phoneNumber,
                onValueChange = {
                    if (it.text.length <= 10) phoneNumber = it
                },
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(top = 4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if(isOtpVisible) {
                TextField(
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    ),
                    value = otp,
                    placeholder = { Text("Enter otp") },
                    onValueChange = { otp = it },
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(top = 4.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if(!isOtpVisible) {
                Button(
                    onClick = { onLoginClicked(context,phoneNumber.text, onCodeSent = {
                        Log.d("phoneBook","setting otp visible")
                        isOtpVisible = true
                    }, onLoggedIn = {
                        navController.navigate("home"){
                            popUpTo(0)
                        }
                    })}
                    ,
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(top = 8.dp)
                ) {
                    Text(text = "Send otp", color = Color.White)
                }
            }


            if(isOtpVisible) {
                Button(
                    onClick = { verifyPhoneNumberWithCode(context, storedVerificationId,otp.text){
                        navController.navigate("home"){
                            popUpTo(0)
                        }
                    } },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(top = 8.dp)
                ) {
                    Text(text = "Verify", color = Color.White)
                }
            }


        }
    }
}

private fun verifyPhoneNumberWithCode(context: Context, verificationId: String, code: String,onVerified:() -> Unit) {
    val credential = PhoneAuthProvider.getCredential(verificationId, code)
    signInWithPhoneAuthCredential(context,credential){
        onVerified()
    }
}

fun onLoginClicked (context: Context, phoneNumber: String, onCodeSent: () -> Unit,onLoggedIn: () -> Unit) {

    auth.setLanguageCode("en")
    val callback = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("phoneBook","verification completed")
            signInWithPhoneAuthCredential(context,credential){
                onLoggedIn()
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Log.d("phoneBook","verification failed" + p0)
        }

        override fun onCodeSent(verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("phoneBook","code sent" + verificationId)
            storedVerificationId = verificationId
            onCodeSent()
        }

    }
    val options = context.getActivity()?.let {
        PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91"+phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(it)
            .setCallbacks(callback)
            .build()
    }
    if (options != null) {
        Log.d("phoneBook",options.toString())
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}

private fun signInWithPhoneAuthCredential(context: Context, credential: PhoneAuthCredential,onLoggedIn: () -> Unit) {
    context.getActivity()?.let {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(it) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    Log.d("phoneBook","logged in")
                    onLoggedIn()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.d("phoneBook","wrong otp")
                    }
                    // Update UI
                }
            }
    }
}