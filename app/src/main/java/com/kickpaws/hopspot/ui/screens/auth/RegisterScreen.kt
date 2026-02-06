package com.kickpaws.hopspot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.ui.components.common.HopSpotButton
import com.kickpaws.hopspot.ui.components.common.HopSpotTextField
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotElevations
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    // Navigate when registration successful
    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            onRegisterSuccess()
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
                .padding(HopSpotDimensions.Spacing.lg)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Title
            Text(
                text = "\uD83C\uDF7A",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            Text(
                text = stringResource(R.string.auth_welcome),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.background
            )

            Text(
                text = stringResource(R.string.auth_join_community),
                fontSize = 16.sp,
                color = colorScheme.background.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xl))

            // Register Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = HopSpotShapes.dialog,
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.background
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = HopSpotElevations.high
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(HopSpotDimensions.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Display Name Field
                    HopSpotTextField(
                        value = uiState.displayName,
                        onValueChange = viewModel::onDisplayNameChange,
                        label = stringResource(R.string.label_your_name),
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

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

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

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
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                    // Invitation Code Field
                    HopSpotTextField(
                        value = uiState.invitationCode,
                        onValueChange = viewModel::onInvitationCodeChange,
                        label = stringResource(R.string.label_invitation_code),
                        leadingIcon = Icons.Default.Badge,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.register()
                            }
                        )
                    )

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                        Text(
                            text = uiState.errorMessage!!,
                            color = colorScheme.error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.lg))

                    // Register Button
                    HopSpotButton(
                        text = stringResource(R.string.btn_register),
                        onClick = viewModel::register,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                    // Login Link
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = stringResource(R.string.auth_have_account),
                            color = colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.lg))
        }
    }
}
