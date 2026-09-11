package com.example.safesphere.GuardianNetworkSystem.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.safesphere.GuardianNetworkSystem.Data.Guardian
import com.example.safesphere.GuardianNetworkSystem.ViewModel.GuardianViewModel
import kotlin.math.abs

@Composable
fun GuardiansScreen(
    viewModel: GuardianViewModel,
    onAddGuardian: () -> Unit,
    onGuardianClick: (Guardian) -> Unit
) {
    val guardians by viewModel.guardians.collectAsState()
    var selectedGuardian by remember {
        mutableStateOf<Guardian?>(null)
    }
    LaunchedEffect(Unit) {
        viewModel.getGuardians()
    }
    /*
     * NORMAL GUARDIANS SCREEN
     */
    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FB))
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Guardians",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202124)
                    )

                    Text(
                        text = "${guardians.size} connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8993A0)
                    )
                }

                // ADD GUARDIAN BUTTON
                IconButton(
                    onClick = onAddGuardian,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF3D5AFE),
                                    Color(0xFF303B52)
                                )
                            )
                        )
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Guardian",
                        tint = Color.White
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            // CONNECTED CONTACTS HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "CONNECTED CONTACTS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF66727D)
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Color(0xFFD9DEE5)
                        )
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                // SHIELD ICON
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFFE9EDFA)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Guardians",
                        tint = Color(0xFF5368B5),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // GUARDIAN LIST
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(guardians) { guardian ->

                    GuardianCard(
                        guardian = guardian,
                        onClick = {
                            onGuardianClick(guardian)
                        }
                    )
                }

                // SAFETY CARD
                item {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                    SafetyCard()
                }
            }
        }
    }
}
//AVATAR COLOURS
val AVATAR_PALETTE: List<Pair<Color, Color>> = listOf(
    Color(0xFFE8F0FE) to Color(0xFF315EA8),
    Color(0xFFFBE9E7) to Color(0xFFC04B3A),
    Color(0xFFFFF1D6) to Color(0xFFB7791F),
    Color(0xFFF0E8FA) to Color(0xFF6B46A1),
    Color(0xFFE5F3F0) to Color(0xFF287565),
    Color(0xFFFDE7F3) to Color(0xFFB23A73),
    Color(0xFFE9F7E3) to Color(0xFF4C8C2B),
    Color(0xFFE3F1FB) to Color(0xFF2E7EB0),
    Color(0xFFFBEFE0) to Color(0xFF9C6B1F),
    Color(0xFFEDEAFB) to Color(0xFF5A4FB0)
)
fun avatarColorsFor(
    guardian: Guardian
): Pair<Color, Color> {
    val key = "${guardian.name}#${guardian.relationship}"
    var hash = 0
    for (c in key) {
        hash = (hash * 31 + c.code) xor (hash ushr 7)
    }
    val index = abs(hash) % AVATAR_PALETTE.size
    return AVATAR_PALETTE[index]
}
//INITIALS
fun initialsFor(
    name: String
): String {

    return name
        .split(" ")
        .filter {
            it.isNotBlank()
        }
        .take(2)
        .map {
            it.first().uppercaseChar()
        }
        .joinToString("")
}
// GUARDIAN CARD
@Composable
fun GuardianCard(
    guardian: Guardian,
    onClick: () -> Unit
) {
    val isVerified = guardian.is_verified
    val (
        avatarBackground,
        avatarTextColor
    ) = avatarColorsFor(guardian)
    val initials = initialsFor(guardian.name)
    val statusColor =
        if (isVerified) {
            Color(0xFF3BA76B)
        } else {
            Color(0xFFF2B01E)
        }
    val statusText =
        if (isVerified) {
            "Active"
        } else {
            "Pending approval"
        }
    val statusTextColor =
        if (isVerified) {
            Color(0xFF39835F)
        } else {
            Color(0xFF737B80)
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 16.dp,
                vertical = 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // AVATAR
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(
                    avatarBackground
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = initials,
                fontWeight = FontWeight.Bold,
                color = avatarTextColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(
            modifier = Modifier.width(16.dp)
        )
        // NAME + STATUS
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${guardian.name} (${guardian.relationship})",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(
                modifier = Modifier.height(6.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(
                            statusColor
                        )
                )
                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusTextColor
                )
            }
        }
        // ARROW
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Guardian details",
            tint = Color(0xFF8993A0),
            modifier = Modifier.size(26.dp)
        )
    }
}
//SAFETY CARD
@Composable
fun SafetyCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFEEF3FA),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFFE0E8F7)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Safety",
                tint = Color(0xFF5368B5),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(
            modifier = Modifier.width(18.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Your safety, our priority",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF273B61),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(
                modifier = Modifier.height(6.dp)
            )
            Text(
                text = "Guardians you add can help you in critical situations.",
                color = Color(0xFF66727D),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}