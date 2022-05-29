package ru.netology

data class ChatMessage(
    val id: Long = UNDEFINED_ID,
    val chatId: Long,
    val userId: Long,
    val directId: Long,
    val text: String,
    val isRead: Boolean = false,
    val isDelete: Boolean = false,
){
    companion object {
        const val UNDEFINED_ID = -1L
    }
}
