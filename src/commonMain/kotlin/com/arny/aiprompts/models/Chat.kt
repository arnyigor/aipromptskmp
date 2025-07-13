package com.arny.aiprompts.models

data class Chat(
    val id: String,
    val name: String,
    val timestamp: Long,
    val lastMessage: String
)