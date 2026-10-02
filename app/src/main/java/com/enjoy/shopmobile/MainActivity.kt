package com.enjoy.shopmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enjoy.shopmobile.viewmodel.AuthState
import com.enjoy.shopmobile.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AuthTestScreen()
            }
        }
    }
}

@Composable
private fun AuthTestScreen(viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ENJ0Y Shop Authentication Test",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        when (val current = state) {
            AuthState.Unknown, AuthState.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Checking server session...")
            }

            is AuthState.Authenticated -> {
                Text("Authenticated")
                Spacer(Modifier.height(8.dp))
                Text("User: " + current.user.name)
                Text("Email: " + current.user.email)
                Spacer(Modifier.height(16.dp))
                Text("Session persistence test: PASS")
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::logout) {
                    Text("Log out")
                }
            }

            is AuthState.Unauthenticated -> {
                current.message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log in")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = viewModel::checkSession,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check /auth/me")
                }
            }
        }
    }
}
