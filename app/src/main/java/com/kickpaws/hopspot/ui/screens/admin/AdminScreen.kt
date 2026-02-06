package com.kickpaws.hopspot.ui.screens.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.InvitationCode
import com.kickpaws.hopspot.domain.model.User

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

    // Handle created code - copy to clipboard
    LaunchedEffect(uiState.createdCode) {
        uiState.createdCode?.let { code ->
            copyToClipboard(context, code)
            Toast.makeText(context, codeCopiedFormatMessage.replace("%1\$s", code), Toast.LENGTH_LONG).show()
            viewModel.clearCreatedCode()
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    // Create Code Dialog
    if (uiState.showCreateCodeDialog) {
        CreateCodeDialog(
            comment = uiState.newCodeComment,
            onCommentChange = viewModel::setNewCodeComment,
            onConfirm = viewModel::createInvitationCode,
            onDismiss = viewModel::hideCreateCodeDialog,
            isLoading = uiState.isCreatingCode
        )
    }

    // Delete Code Dialog
    if (uiState.showDeleteCodeDialog && uiState.codeToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteCodeDialog,
            title = { Text(stringResource(R.string.dialog_delete_code_title)) },
            text = {
                Text(stringResource(R.string.dialog_delete_code_message, uiState.codeToDelete?.code ?: ""))
            },
            confirmButton = {
                Button(
                    onClick = viewModel::deleteInvitationCode,
                    enabled = !uiState.isDeletingCode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    if (uiState.isDeletingCode) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colorScheme.onError
                        )
                    } else {
                        Text(stringResource(R.string.common_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteCodeDialog) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
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
            // Tab Row
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

            // Tab Content
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
    val colorScheme = MaterialTheme.colorScheme

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("😕", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onRefresh) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
        codes.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔑", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.empty_codes_title), color = colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.empty_codes_subtitle), fontSize = 14.sp, color = colorScheme.outline)
                }
            }
        }
        else -> {
            // First active codes, then inactive codes
            val sortedCodes = codes.sortedBy { it.isRedeemed }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                .padding(16.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    if (code.isRedeemed) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
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
                            shape = RoundedCornerShape(4.dp),
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = code.comment,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

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

            // Action Buttons - nur für nicht eingelöste Codes
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

    // Delete Confirmation Dialog
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_user_title)) },
            text = { Text(stringResource(R.string.dialog_delete_user_message, user.displayName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(user)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("😕", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onRefresh) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
        users.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.empty_users_title), color = colorScheme.onSurfaceVariant)
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                .padding(16.dp)
        ) {
            // Top Row: Avatar + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    shape = RoundedCornerShape(50),
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

                Spacer(modifier = Modifier.width(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Badges + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isAdmin) colorScheme.primary else colorScheme.outline
                    ) {
                        Text(
                            text = if (isAdmin) stringResource(R.string.admin_user_badge_admin) else stringResource(R.string.admin_user_badge_user),
                            fontSize = 10.sp,
                            color = if (isAdmin) colorScheme.onPrimary else colorScheme.surface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Inactive Badge
                    if (!user.isActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
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

                // Action Buttons
                Row {
                    // Toggle Active/Inactive
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

                    // Toggle Role
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

                    // Delete
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_create_code_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_create_code_hint),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text(stringResource(R.string.label_comment_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
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
