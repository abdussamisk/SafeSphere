package com.example.guardiannetworksystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safesphere.GuardianNetworkSystem.Data.Guardian
import com.example.safesphere.GuardianNetworkSystem.Viewmodel.GuardianViewModel


@Composable
fun GuardianDetailsScreen(
    guardian: Guardian,
    viewModel: GuardianViewModel,
    onBack: () -> Unit,
    onVerify: () -> Unit
) {

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // -----------------------------
        // TOP BAR
        // -----------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,

                    contentDescription =
                        "Back"
                )
            }

            Text(
                text = "Guardian Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(25.dp)
        )


        // -----------------------------
        // GUARDIAN PROFILE
        // -----------------------------

        Column(
            modifier = Modifier.fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFE8F5E9)
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Person,

                    contentDescription =
                        "Guardian",

                    tint =
                        Color(0xFF2E7D32),

                    modifier =
                        Modifier.size(60.dp)
                )
            }


            Spacer(
                modifier = Modifier.height(15.dp)
            )


            Text(
                text = guardian.name,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(30.dp)
        )


        // -----------------------------
        // DETAILS CARD
        // -----------------------------

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),

            shape =
                RoundedCornerShape(16.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color(0xFFF5F5F5)
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {

                // PHONE

                Text(
                    text = "Phone Number",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = guardian.phonenumber,
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Medium
                )


                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                // RELATIONSHIP

                Text(
                    text = "Relationship",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = guardian.relationship,
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Medium
                )


                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                // STATUS

                Text(
                    text = "Status",
                    fontSize = 14.sp,
                    color = Color.Gray
                )


                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )


                Text(
                    text =
                        if (guardian.is_verified)
                            "Verified"
                        else
                            "Pending",

                    fontSize = 18.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        if (guardian.is_verified)
                            Color(0xFF2E7D32)
                        else
                            Color(0xFFFF9800)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(25.dp)
        )


        // -----------------------------
        // VERIFY BUTTON
        // -----------------------------

        if (!guardian.is_verified) {

            Button(
                onClick = {
                    onVerify()
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(55.dp),

                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "Verify Guardian",
                    fontSize = 17.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )
        }


        // -----------------------------
        // DELETE BUTTON
        // -----------------------------

        Button(
            onClick = {
                showDeleteDialog = true
            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(55.dp),

            shape =
                RoundedCornerShape(12.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFFD32F2F)
                )
        ) {

            Icon(
                imageVector =
                    Icons.Default.Delete,

                contentDescription =
                    "Delete"
            )

            Spacer(
                modifier =
                    Modifier.size(8.dp)
            )

            Text(
                text = "Delete Guardian",
                fontSize = 17.sp
            )
        }
    }


    // -----------------------------
    // DELETE DIALOG
    // -----------------------------

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    text = "Delete Guardian?"
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to delete ${guardian.name}?"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog = false

                        viewModel.deleteGuardian(
                            guardianName =
                                guardian.name,

                            onSuccess = {
                                onBack()
                            }
                        )
                    }
                ) {

                    Text(
                        text = "Delete",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}