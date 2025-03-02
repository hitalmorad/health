package com.example.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    errorMessage: String? = null // Optional validation message
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(/*modifier = Modifier.fillMaxWidth()*/) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp), // Rounded edges for modern UI
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF86D2F5), // Light blue for a wellness feel
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF86D2F5),
                focusedTextColor = Color.Black,    // Corrected text color
                unfocusedTextColor = Color.Black   // Corrected text color
            ),
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Password",
                            tint = Color.Gray
                        )
                    }
                }
            },
            modifier = Modifier
                //.fillMaxWidth()
                .background(Color.Transparent, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp)
        )

        // Show error message if exists
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
