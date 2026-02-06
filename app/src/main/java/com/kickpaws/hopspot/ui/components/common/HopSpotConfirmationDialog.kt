package com.kickpaws.hopspot.ui.components.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions

@Composable
fun HopSpotConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.common_ok),
    dismissText: String = stringResource(R.string.common_cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    isDestructive: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val confirmColor = if (isDestructive) colorScheme.error else colorScheme.primary
    val confirmContentColor = if (isDestructive) colorScheme.onError else colorScheme.onPrimary

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) colorScheme.error else colorScheme.primary
                )
            }
        } else null,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    contentColor = confirmContentColor
                )
            ) {
                if (isLoading) {
                    HopSpotLoadingIndicator(
                        size = LoadingSize.Small,
                        color = confirmContentColor,
                        modifier = Modifier.size(HopSpotDimensions.Loading.buttonSize)
                    )
                } else {
                    Text(confirmText)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun HopSpotDeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    isLoading: Boolean = false
) {
    HopSpotConfirmationDialog(
        title = title,
        message = message,
        confirmText = stringResource(R.string.common_delete),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = icon,
        isLoading = isLoading,
        isDestructive = true
    )
}
