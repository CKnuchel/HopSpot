package com.kickpaws.hopspot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kickpaws.hopspot.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme

    // Navigate when login successful
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.secondaryContainer,
                        colorScheme.secondary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Title
            Text(
                text = "\uD83C\uDF7A",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.background
            )

            Text(
                text = stringResource(R.string.auth_slogan),
                fontSize = 16.sp,
                color = colorScheme.background.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.background
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email Field
                    HopSpotTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = stringResource(R.string.label_email),
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    HopSpotTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = stringResource(R.string.label_password),
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
                        )
                    )

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = colorScheme.error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    HopSpotButton(
                        text = stringResource(R.string.btn_login),
                        onClick = viewModel::login,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Register Link
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = stringResource(R.string.auth_no_account),
                            color = colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HopSpotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colorScheme.primary
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePasswordVisibility?.invoke() }) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (isPasswordVisible) {
                            stringResource(R.string.cd_hide_password)
                        } else {
                            stringResource(R.string.cd_show_password)
                        },
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // Border
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            // Label
            focusedLabelColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurfaceVariant,
            // Text
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            // Cursor
            cursorColor = colorScheme.primary,
            // Container
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun HopSpotButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
