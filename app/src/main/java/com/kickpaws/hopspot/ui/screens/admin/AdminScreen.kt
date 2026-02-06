package com.kickpaws.hopspot.ui.screens.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.InvitationCode
import com.kickpaws.hopspot.domain.model.User
import com.kickpaws.hopspot.ui.components.common.HopSpotCenteredLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.HopSpotDeleteConfirmationDialog
import com.kickpaws.hopspot.ui.components.common.HopSpotEmptyView
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val tabCodes = stringResource(R.string.admin_tab_codes)
    val tabUsers = stringResource(R.string.admin_tab_users)
    val tabs = listOf(tabCodes, tabUsers)

    val codeCopiedMessage = stringResource(R.string.admin_code_copied)
    val codeCopiedFormatMessage = stringResource(R.string.admin_code_copied_format)

    LaunchedEffect(uiState.createdCode) {
        uiState.createdCode?.let { code ->
            copyToClipboard(context, code)
            Toast.makeText(context, codeCopiedFormatMessage.replace("%1\$s", code), Toast.LENGTH_LONG).show()
            viewModel.clearCreatedCode()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    if (uiState.showCreateCodeDialog) {
        CreateCodeDialog(
            comment = uiState.newCodeComment,
            onCommentChange = viewModel::setNewCodeComment,
            onConfirm = viewModel::createInvitationCode,
            onDismiss = viewModel::hideCreateCodeDialog,
            isLoading = uiState.isCreatingCode
        )
    }

    if (uiState.showDeleteCodeDialog && uiState.codeToDelete != null) {
        HopSpotDeleteConfirmationDialog(
            title = stringResource(R.string.dialog_delete_code_title),
            message = stringResource(R.string.dialog_delete_code_message, uiState.codeToDelete?.code ?: ""),
            onConfirm = viewModel::deleteInvitationCode,
            onDismiss = viewModel::hideDeleteCodeDialog,
            icon = Icons.Default.Key,
            isLoading = uiState.isDeletingCode
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (uiState.selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = viewModel::showCreateCodeDialog,
                    containerColor = colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_create_code))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Key else Icons.Default.People,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            when (uiState.selectedTabIndex) {
                0 -> InvitationCodesTab(
                    codes = uiState.invitationCodes,
                    isLoading = uiState.isLoadingCodes,
                    error = uiState.codesError,
                    onRefresh = viewModel::loadInvitationCodes,
                    onCopyCode = { code ->
                        copyToClipboard(context, code)
                        Toast.makeText(context, codeCopiedMessage, Toast.LENGTH_SHORT).show()
                    },
                    onDeleteCode = viewModel::showDeleteCodeDialog
                )
                1 -> UsersTab(
                    users = uiState.users,
                    isLoading = uiState.isLoadingUsers,
                    error = uiState.usersError,
                    onRefresh = viewModel::loadUsers,
                    onToggleRole = viewModel::toggleUserRole,
                    onToggleActive = viewModel::toggleUserActive,
                    onDelete = viewModel::deleteUser
                )
            }
        }
    }
}

@Composable
private fun InvitationCodesTab(
    codes: List<InvitationCode>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onCopyCode: (String) -> Unit,
    onDeleteCode: (InvitationCode) -> Unit
) {
    when {
        isLoading -> {
            HopSpotCenteredLoadingIndicator()
        }
        error != null -> {
            HopSpotErrorView(
                message = error,
                onRetry = onRefresh,
                modifier = Modifier.fillMaxSize()
            )
        }
        codes.isEmpty() -> {
            HopSpotEmptyView(
                icon = Icons.Default.Key,
                title = stringResource(R.string.empty_codes_title),
                subtitle = stringResource(R.string.empty_codes_subtitle),
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            val sortedCodes = codes.sortedBy { it.isRedeemed }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(HopSpotDimensions.Screen.padding),
                verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.sm)
            ) {
                items(sortedCodes) { code ->
                    InvitationCodeCard(
                        code = code,
                        onCopy = { onCopyCode(code.code) },
                        onDelete = { onDeleteCode(code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationCodeCard(
    code: InvitationCode,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = if (code.isRedeemed) {
                colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = code.code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (code.isRedeemed) colorScheme.onSurfaceVariant else colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
                    if (code.isRedeemed) {
                        Surface(
                            shape = HopSpotShapes.thumbnail,
                            color = colorScheme.outline
                        ) {
                            Text(
                                text = stringResource(R.string.admin_code_redeemed),
                                fontSize = 10.sp,
                                color = colorScheme.surface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = HopSpotShapes.thumbnail,
                            color = colorScheme.primary
                        ) {
                            Text(
                                text = stringResource(R.string.admin_code_active),
                                fontSize = 10.sp,
                                color = colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!code.comment.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))
                    Text(
                        text = code.comment,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))

                if (code.isRedeemed && code.redeemedBy != null) {
                    Text(
                        text = stringResource(R.string.admin_code_redeemed_by, code.redeemedBy.displayName),
                        fontSize = 12.sp,
                        color = colorScheme.outline
                    )
                } else {
                    Text(
                        text = stringResource(R.string.admin_code_created_by, code.createdBy.displayName),
                        fontSize = 12.sp,
                        color = colorScheme.outline
                    )
                }
            }

            if (!code.isRedeemed) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.cd_copy_code),
                        tint = colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_delete_code),
                        tint = colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun UsersTab(
    users: List<User>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onToggleRole: (User) -> Unit,
    onToggleActive: (User, Boolean) -> Unit,
    onDelete: (User) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var userToDelete by remember { mutableStateOf<User?>(null) }

    userToDelete?.let { user ->
        HopSpotDeleteConfirmationDialog(
            title = stringResource(R.string.dialog_delete_user_title),
            message = stringResource(R.string.dialog_delete_user_message, user.displayName),
            onConfirm = {
                onDelete(user)
                userToDelete = null
            },
            onDismiss = { userToDelete = null },
            icon = Icons.Default.PersonRemove
        )
    }

    when {
        isLoading -> {
            HopSpotCenteredLoadingIndicator()
        }
        error != null -> {
            HopSpotErrorView(
                message = error,
                onRetry = onRefresh,
                modifier = Modifier.fillMaxSize()
            )
        }
        users.isEmpty() -> {
            HopSpotEmptyView(
                icon = Icons.Default.People,
                title = stringResource(R.string.empty_users_title),
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(HopSpotDimensions.Screen.padding),
                verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.sm)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        onToggleRole = { onToggleRole(user) },
                        onToggleActive = { isActive -> onToggleActive(user, isActive) },
                        onDelete = { userToDelete = user }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onToggleRole: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAdmin = user.role == "admin"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive) {
                colorScheme.surfaceVariant
            } else {
                colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = HopSpotShapes.chip,
                    color = if (!user.isActive) {
                        colorScheme.outline
                    } else if (isAdmin) {
                        colorScheme.primary
                    } else {
                        colorScheme.secondary
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            color = colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isActive) colorScheme.onSurface else colorScheme.outline
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.xxs)) {
                    Surface(
                        shape = HopSpotShapes.thumbnail,
                        color = if (isAdmin) colorScheme.primary else colorScheme.outline
                    ) {
                        Text(
                            text = if (isAdmin) stringResource(R.string.admin_user_badge_admin) else stringResource(R.string.admin_user_badge_user),
                            fontSize = 10.sp,
                            color = if (isAdmin) colorScheme.onPrimary else colorScheme.surface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (!user.isActive) {
                        Surface(
                            shape = HopSpotShapes.thumbnail,
                            color = colorScheme.error
                        ) {
                            Text(
                                text = stringResource(R.string.admin_user_deactivated),
                                fontSize = 10.sp,
                                color = colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = { onToggleActive(!user.isActive) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isActive) Icons.Default.PersonOff else Icons.Default.PersonAdd,
                            contentDescription = if (user.isActive) stringResource(R.string.cd_deactivate) else stringResource(R.string.cd_activate),
                            tint = if (user.isActive) colorScheme.error else colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleRole,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.PersonRemove else Icons.Default.AdminPanelSettings,
                            contentDescription = if (isAdmin) stringResource(R.string.cd_make_user) else stringResource(R.string.cd_make_admin),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateCodeDialog(
    comment: String,
    onCommentChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_create_code_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_create_code_hint),
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text(stringResource(R.string.label_comment_optional)) },
                    singleLine = true,
                    shape = HopSpotShapes.textField,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = HopSpotShapes.button
            ) {
                if (isLoading) {
                    HopSpotLoadingIndicator(
                        size = LoadingSize.Small,
                        color = colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.common_create))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Einladungscode", text)
    clipboard.setPrimaryClip(clip)
}
