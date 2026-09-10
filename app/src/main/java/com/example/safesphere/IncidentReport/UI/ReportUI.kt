package com.example.safesphere.IncidentReport.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safesphere.IncidentReport.ViewModels.ReportViewModel

object SafeSphereColors {
    val Background = Color(0xFFF5F3EE)   // warm cream app background
    val Surface = Color(0xFFFFFFFF)      // white input fields / cards
    val Charcoal = Color(0xFF2B2925)     // primary buttons ("Get started", "Save entry")
    val Forest = Color(0xFF2C4A3B)       // secondary accent (auth screens, avatars)
    val TextPrimary = Color(0xFF221F1C)
    val TextSecondary = Color(0xFF8C8880)
    val Border = Color(0xFFE7E3DB)
    val Error = Color(0xFFB3261E)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportUI(
    viewModel: ReportViewModel = viewModel(),
    onBack: () -> Unit = {},
    onCancel: () -> Unit = onBack
) {
    Scaffold(
        containerColor = SafeSphereColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Entry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = SafeSphereColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SafeSphereColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafeSphereColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SafeSphereColors.Background)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Report an incident",
                fontSize = 15.sp,
                color = SafeSphereColors.TextSecondary
            )

            Spacer(Modifier.height(24.dp))

            SafeSphereField(
                label = "Title",
                state = viewModel.title,
                placeholder = "e.g. Suspicious activity"
            )

            Spacer(Modifier.height(20.dp))

            SafeSphereField(
                label = "Address",
                state = viewModel.address,
                placeholder = "Mayurinagar, Hyderabad"
            )

            Spacer(Modifier.height(20.dp))

            SafeSphereField(
                label = "Impact",
                state = viewModel.impact,
                placeholder = "High/Medium/Low",
                minLines = 1
            )

            if (viewModel.errorReporting) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Something went wrong. Please try again.",
                    color = SafeSphereColors.Error,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onReportClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeSphereColors.Charcoal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save entry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Cancel",
                color = SafeSphereColors.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCancel() }
                    .padding(8.dp)
            )
        }
    }

    // ---- Success popup ----
    if (viewModel.reportSuccess) {
        ReportSuccessDialog(
            onDismiss = {
                viewModel.onSuccessDialogDismissed()
            }
        )
    }
}

@Composable
private fun ReportSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SafeSphereColors.Surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SafeSphereColors.Forest,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Report successful",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SafeSphereColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Your incident has been logged.",
                fontSize = 14.sp,
                color = SafeSphereColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeSphereColors.Charcoal,
                    contentColor = Color.White
                )
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafeSphereField(
    label: String,
    state: TextFieldState,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SafeSphereColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            state = state,
            placeholder = {
                Text(
                    text = placeholder,
                    color = SafeSphereColors.TextSecondary.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            lineLimits = if (maxLines > 1)
                TextFieldLineLimits.MultiLine(minHeightInLines = minLines, maxHeightInLines = maxLines)
            else
                TextFieldLineLimits.SingleLine,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SafeSphereColors.Surface,
                focusedContainerColor = SafeSphereColors.Surface,
                unfocusedBorderColor = SafeSphereColors.Border,
                focusedBorderColor = SafeSphereColors.Forest,
                cursorColor = SafeSphereColors.Forest,
                focusedTextColor = SafeSphereColors.TextPrimary,
                unfocusedTextColor = SafeSphereColors.TextPrimary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportScreenPreview() {
    MaterialTheme {
        ReportUI()
    }
}