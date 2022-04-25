package com.hugh.updater.mirlkoi.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hugh.updater.mirlkoi.ui.theme.GraySemi
import com.hugh.updater.mirlkoi.ui.theme.MirlKoiUpdaterTheme
import com.hugh.updater.mirlkoi.ui.theme.Purple200
import com.hugh.updater.mirlkoi.util.ApiLabels
import com.hugh.updater.mirlkoi.util.findLabelsKey
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.time.timepicker

@Preview
@Composable
fun DrawerPreview(){
    MirlKoiUpdaterTheme {
        Column() {
            Button(onClick = {
                // a select dialog and two dialogs
            },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.Yellow,
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Sharp.DateRange, contentDescription = "Timer Button",
                    modifier = Modifier
                        .size(24.dp))
            }
        }
    }
}
@Composable
fun MomentDialog(dialogState:MaterialDialogState,
                 content:@Composable (MaterialDialogScope.()->Unit)){
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        },
        content = content
    )
}