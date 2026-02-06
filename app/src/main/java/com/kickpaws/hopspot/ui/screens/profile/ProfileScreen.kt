package com.kickpaws.hopspot.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.ui.components.common.HopSpotButton
import com.kickpaws.hopspot.ui.components.common.HopSpotCenteredLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotElevations
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLoggedOut()
        }
    }

    if (uiState.isEditDialogOpen) {
        EditProfileDialog(
            displayName = uiState.editDisplayName,
            onDisplayNameChange = viewModel::onEditDisplayNameChange,
            onSave = viewModel::saveProfile,
            onDismiss = viewModel::closeEditDialog,
            isSaving = uiState.isSaving,
            error = uiState.saveError
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                HopSpotCenteredLoadingIndicator()
            }

            uiState.errorMessage != null -> {
                HopSpotErrorView(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.user != null -> {
                val user = uiState.user!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(HopSpotDimensions.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xl))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    }

                    if (user.role == "admin") {
                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                        Surface(
                            shape = HopSpotShapes.chip,
                            color = colorScheme.primary
                        ) {
                            Text(
                                text = stringResource(R.string.profile_admin_badge),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xl))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = HopSpotShapes.dialog,
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = HopSpotElevations.medium)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(HopSpotDimensions.Spacing.md)
                        ) {
                            ProfileInfoRow(
                                icon = Icons.Default.Person,
                                label = stringResource(R.string.label_name),
                                value = user.displayName,
                                onClick = { viewModel.openEditDialog() }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = HopSpotDimensions.Spacing.sm),
                                color = colorScheme.outlineVariant
                            )

                            ProfileInfoRow(
                                icon = Icons.Default.Email,
                                label = stringResource(R.string.label_email),
                                value = user.email
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = HopSpotDimensions.Spacing.sm),
                                color = colorScheme.outlineVariant
                            )

                            ProfileInfoRow(
                                icon = Icons.Default.Shield,
                                label = stringResource(R.string.label_role),
                                value = if (user.role == "admin") {
                                    stringResource(R.string.profile_role_admin)
                                } else {
                                    stringResource(R.string.profile_role_user)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = HopSpotShapes.button,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.secondary,
                            contentColor = colorScheme.onSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = HopSpotElevations.medium),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = !uiState.isLoggingOut
                    ) {
                        if (uiState.isLoggingOut) {
                            HopSpotLoadingIndicator(
                                size = LoadingSize.Button,
                                color = colorScheme.onSecondary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
                            Text(
                                text = stringResource(R.string.btn_logout),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(HopSpotDimensions.Icon.medium)
        )

        Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.cd_edit),
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(HopSpotDimensions.Icon.small)
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean,
    error: String?
) {
    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = HopSpotShapes.dialog,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(HopSpotDimensions.Spacing.lg)
            ) {
                Text(
                    text = stringResource(R.string.dialog_edit_name_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text(stringResource(R.string.label_your_name)) },
                    singleLine = true,
                    enabled = !isSaving,
                    shape = HopSpotShapes.textField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedLabelColor = colorScheme.primary,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        cursorColor = colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                    Text(
                        text = error,
                        color = colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text(stringResource(R.string.common_cancel), color = colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))

                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        shape = HopSpotShapes.button,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        if (isSaving) {
                            HopSpotLoadingIndicator(
                                size = LoadingSize.Small,
                                color = colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.common_save))
                        }
                    }
                }
            }
        }
    }
}
