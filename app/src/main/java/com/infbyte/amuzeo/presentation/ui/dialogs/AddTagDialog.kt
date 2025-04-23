package com.infbyte.amuzeo.presentation.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit = {},
    onApplyTag: (String) -> Unit = {},
) {
    Dialog(onDismiss) {
        Column(
            Modifier.background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(10.dp),
            ).padding(16.dp),
        ) {
            var tag by rememberSaveable { mutableStateOf("") }
            OutlinedTextField(
                tag,
                onValueChange = {
                    tag = it
                },
                label = { Text(stringResource(R.string.amuzeo_tag)) },
            )

            Row(
                Modifier.padding(8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) { Text(stringResource(R.string.amuzeo_cancel)) }
                Button(
                    onClick = {
                        onApplyTag(tag)
                        onDismiss()
                    },
                ) { Text(stringResource(R.string.amuzeo_apply)) }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAddTagDialog() {
    AmuzeoTheme {
        AddTagDialog()
    }
}
