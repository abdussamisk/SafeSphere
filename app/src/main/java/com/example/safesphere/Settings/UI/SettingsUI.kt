package com.example.safesphere.Settings.UI

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safesphere.Authy.TokenRepository.UserRepository
import com.example.safesphere.FakeCall.VolumeButtonService
import com.example.safesphere.IncidentReport.UI.SafeSphereColors
import com.example.safesphere.Settings.ViewModels.SettingsViewModel
import com.example.safesphere.Settings.ViewModels.SettingsViewModelFactory
import com.example.safesphere.Settings.ViewModels.UserDetailsUiState

@Composable
fun Setting(
    userRepository: UserRepository,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(userRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SafeSphereColors.Background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SafeSphereColors.Background)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Settings",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = SafeSphereColors.TextPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionLabel("PROFILE")
                UserDetailsCard(uiState = uiState)
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionLabel("QUICK ACTIONS")
                SaveContactCard(phoneNumberToSave = viewModel.getNumber())
            }

            Spacer(Modifier.weight(1f))

            LogoutButton(onLogout = { viewModel.logout() })

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SaveContactCard(
    phoneNumberToSave: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsActionCard(
            title = "Save Contact",
            subtitle = "Add this number to your address book",
            actionLabel = "Save",
            onActionClick = {
                val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                    type = ContactsContract.RawContacts.CONTENT_TYPE
                    putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumberToSave)
                }
                context.startActivity(intent)
            }
        )

        SettingsActionCard(
            title = "Add Permission",
            subtitle = "Grant permission for safety triggers",
            actionLabel = "Allow",
            onActionClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    val componentName = ComponentName(context, VolumeButtonService::class.java)
                    val serviceId = componentName.flattenToString()
                    putExtra(":settings:fragment_args_key", serviceId)
                    putExtra(":settings:show_fragment_args", Bundle().apply {
                        putString(":settings:fragment_args_key", serviceId)
                    })
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SafeSphereColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeSphereColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = SafeSphereColors.TextSecondary
                )
            }

            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeSphereColors.Charcoal,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SafeSphereColors.Error,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "Log out",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun UserDetailsCard(
    uiState: UserDetailsUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SafeSphereColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SafeSphereColors.Charcoal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    Text(
                        text = "Loading...",
                        fontSize = 14.sp,
                        color = SafeSphereColors.TextSecondary
                    )
                } else {
                    Text(
                        text = uiState.name.ifBlank { "Unknown User" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeSphereColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.phoneNumber.ifBlank { "No phone number" },
                        fontSize = 14.sp,
                        color = SafeSphereColors.TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))

                    val countText = if (uiState.guardianCount == "1") "1 guardian" else "${uiState.guardianCount} guardians"
                    Text(
                        text = countText,
                        fontSize = 13.sp,
                        color = SafeSphereColors.TextSecondary
                    )

                    uiState.error?.let { message ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = SafeSphereColors.Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = SafeSphereColors.TextSecondary
    )
}