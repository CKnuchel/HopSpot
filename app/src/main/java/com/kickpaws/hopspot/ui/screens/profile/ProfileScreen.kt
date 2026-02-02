package com.kickpaws.hopspot.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    // Navigate when logged out
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLoggedOut()
        }
    }

    // Edit Dialog
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.primary
                )
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😕",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.loadProfile() }) {
                        Text("Erneut versuchen", color = colorScheme.primary)
                    }
                }
            }

            uiState.user != null -> {
                val user = uiState.user!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Avatar
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name with Edit Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.displayName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    }

                    // Role Badge
                    if (user.role == "admin") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.primary
                        ) {
                            Text(
                                text = "👑 Admin",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            ProfileInfoRow(
                                icon = Icons.Default.Person,
                                label = "Name",
                                value = user.displayName,
                                onClick = { viewModel.openEditDialog() }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.outlineVariant
                            )

                            ProfileInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = user.email
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.outlineVariant
                            )

                            ProfileInfoRow(
                                icon = Icons.Default.Shield,
                                label = "Rolle",
                                value = if (user.role == "admin") "Administrator" else "Benutzer"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout Button
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.secondary,
                            contentColor = colorScheme.onSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = !uiState.isLoggingOut
                    ) {
                        if (uiState.isLoggingOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colorScheme.onSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ausloggen",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

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
                contentDescription = "Bearbeiten",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Name ändern",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("Dein Name") },
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text("Abbrechen", color = colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Speichern")
                        }
                    }
                }
            }
        }
    }
}