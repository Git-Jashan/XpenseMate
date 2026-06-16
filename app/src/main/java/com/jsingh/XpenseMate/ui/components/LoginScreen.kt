package com.jsingh.XpenseMate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.viewmodel.AuthState
import com.jsingh.XpenseMate.viewmodel.HomeViewModel

// 🚀 NEW SUPABASE IMPORTS FOR GOOGLE AUTH
import com.jsingh.XpenseMate.network.SupabaseNetwork
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth

@Composable
fun LoginScreen(
    viewModel: HomeViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Listen to Supabase State
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetAuthState()
            onLoginSuccess()
        }
    }

    val isLoading = authState is AuthState.Loading
    val errorMessage = if (authState is AuthState.Error) (authState as AuthState.Error).message else ""

    // 🚀 CREATE THE NATIVE GOOGLE SIGN-IN ACTION
    val composeAuth = SupabaseNetwork.client.composeAuth
    val googleSignInAction = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    viewModel.resetAuthState()
                    onLoginSuccess()
                }
                is NativeSignInResult.Error -> {
                    android.util.Log.e("SupabaseAuth", "Google Login Return Failed: ${result.message}")
                }
                else -> {} // Handle closed/cancelled dialogues silently
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("XpenseMate", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            text = if (isSignUpMode) "Create an account to get started." else "Track smarter, not harder.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 🚀 THE WIRED GOOGLE BUTTON
        OutlinedButton(
            onClick = { googleSignInAction.startFlow() }, // 👈 Triggers the native browser tunnel
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(" OR ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.resetAuthState() },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.resetAuthState() },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isSignUpMode) viewModel.signUpWithEmail(email, password)
                else viewModel.signInWithEmail(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isSignUpMode) "Create Account" else "Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isSignUpMode = !isSignUpMode
            viewModel.resetAuthState()
        }) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) { append(if (isSignUpMode) "Already have an account? " else "Don't have an account? ") }
                    withStyle(style = SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold)) {
                        append(if (isSignUpMode) "Log in here" else "Sign up here")
                    }
                }
            )
        }
    }
}