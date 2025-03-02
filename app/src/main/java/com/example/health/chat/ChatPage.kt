package com.example.health.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.R
import com.example.health.ui.theme.ColorModelMessage
import com.example.health.ui.theme.ColorUserMessage
import com.example.health.ui.theme.Purple80

@RequiresApi(35)
@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF8F9FA)) // Light background
    ) {
        ChatHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList
        )
        MessageInput(
            viewModel = viewModel,
            onMessageSend = { viewModel.sendMessage(it) }
        )
    }
}

@Composable
fun ChatHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "virtual consultation",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Chat Icon",
                tint = Purple80
            )
            Text(text = "Ask me anything!", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageBubble(messageModel = message)
            }
        }
    }
}

@Composable
fun MessageBubble(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isModel) ColorModelMessage else ColorUserMessage)
                .padding(12.dp)
        ) {
            SelectionContainer {
                Text(
                    text = messageModel.message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(viewModel: ChatViewModel, onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(18.dp)
            .clip(RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type a message...") },
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .clip(RoundedCornerShape(5.dp)),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF6200EE)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = {
            viewModel.startListening { recognizedText ->
                message = recognizedText
                onMessageSend(recognizedText)
            }
        }) {
            Icon(
                painter = painterResource(id = R.drawable.mic),
                contentDescription = "Voice Input",
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF6200EE))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
