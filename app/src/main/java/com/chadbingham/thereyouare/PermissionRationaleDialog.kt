package com.chadbingham.thereyouare

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_required)) },
        text = {
            Column {
                Text(stringResource(R.string.permission_rationale))
                Spacer(modifier = Modifier.padding(8.dp))
                Text(stringResource(R.string.permission_warning))
            }
        },
        confirmButton = {
            Button(onClick = {
                onRequestPermission()
            }) {
                Text(stringResource(R.string.continue_text))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
fun PermissionRationaleDialogPreview() {
    MaterialTheme {
        PermissionRationaleDialog(onDismiss = {}, onRequestPermission = {})
    }
}