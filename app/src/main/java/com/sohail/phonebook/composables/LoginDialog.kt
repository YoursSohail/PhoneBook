package com.sohail.phonebook.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController

@Composable
fun LoginDialog(navController: NavHostController) {
    val dialogState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    Dialog(
        onDismissRequest = { dialogState.value = false },
        content = {
            CompleteDialogContent(navController)
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )

}