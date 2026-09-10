package com.example.safesphere.Authy.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.safesphere.R
import com.example.safesphere.Authy.UI.ViewModel.SigninViewModel
import com.example.safesphere.ui.theme.SafeSphereTheme
import com.example.safesphere.ui.theme.AccentDeep
import com.example.safesphere.ui.theme.Border
import com.example.safesphere.ui.theme.AccentSoft
import com.example.safesphere.ui.theme.Cream
import com.example.safesphere.ui.theme.TextMuted
import com.example.safesphere.ui.theme.TextPrimary
import com.example.safesphere.ui.theme.TextSecondary


@Composable
fun SignUp(
    viewModel: SigninViewModel =viewModel(),
    navController: NavController
) {
    if (viewModel.isSignedUp) {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("signup") {
                    inclusive = true
                }
            }
        }
    }
    Scaffold(containerColor = Cream) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            val context=LocalContext.current
            SignUpAuthHero()

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Text(
                    text = "Create your account",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Join SafeSphere to keep your circle connected and safe.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(top = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SignUpAuthTextField(
                    label = "Full Name",
                    value = viewModel.name.text.toString(),
                    onValueChange = { newValue ->
                        viewModel.name.edit {
                            replace(0, length, newValue)
                        }
                    },
                    placeholder = "Your full name"
                )
                SignUpAuthTextField(
                    label = "Phone",
                    value = viewModel.phoneno.text.toString(),
                    onValueChange = { newValue ->
                        viewModel.phoneno.edit {
                            replace(0, length, newValue)
                        }
                    },
                    placeholder = "+91 98765 43210",
                    keyboardType = KeyboardType.Phone,
                )

                SignUpAuthTextField(
                    label = "Password",
                    value = viewModel.password.text.toString(),
                    onValueChange = { newValue ->
                        viewModel.password.edit {
                            replace(0, length, newValue)
                        }
                    },
                    placeholder = "Create a password",
                    isPassword = true,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(top = 28.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { viewModel.userSignup(context) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentDeep, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                ) {
                    Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp),
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Border)
                    Text(
                        text = "OR CONTINUE WITH",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Divider(modifier = Modifier.weight(1f), color = Border)
                }

                SignUpGoogleButton(
                    onClick = { viewModel.performGoogleSignIn(context) }
                )

                Row(modifier = Modifier.padding(top = 20.dp)) {
                    Text(text = "Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "Log in",
                        color = AccentDeep,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { navController.navigate("login") },
                        ),
                    )
                }
            }

        }
    }
}

/** Soft blurred gradient hero + circular "S" wordmark used at the top of the auth screens. */
@Composable
private fun SignUpAuthHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Cream),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(360.dp)
                .graphicsLayer { translationX = -260f; translationY = -420f }
                .blur(60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentDeep.copy(alpha = 0.9f), AccentDeep.copy(alpha = 0f)),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer { translationX = 320f; translationY = -120f }
                .blur(50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentSoft.copy(alpha = 0.85f), AccentSoft.copy(alpha = 0f)),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AccentDeep, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SignUpAuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted) },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Border,
                focusedBorderColor = AccentDeep,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Single full-width "Sign up with Google" button with official multi-color Google G logo.
 */
@Composable
private fun SignUpGoogleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        border = BorderStroke(1.dp, Border),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Sign up with Google", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}



/*@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignUpScreenPreview() {
    SafeSphereTheme {
        SignUp()
    }
}*/