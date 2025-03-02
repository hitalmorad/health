package com.example.health

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.health.ui.theme.Black
import com.example.health.ui.theme.focusedTextFieldText
import com.example.health.ui.theme.textFieldContainer
import com.example.health.ui.theme.unfocusedTextFieldText


@Composable
fun LoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    trailing: String = "",
    isPassword: Boolean
) {
    val uiColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label, color = uiColor, style = MaterialTheme.typography.labelMedium) },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedPlaceholderColor = Color.Gray,
            focusedPlaceholderColor = Color.Black
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,

        trailingIcon = {
            if (trailing.isNotEmpty()) {
                TextButton(onClick = { /* Forgot Password Logic */ }) {
                    Text(text = trailing, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = uiColor)
                }
            }
        }
    )
}