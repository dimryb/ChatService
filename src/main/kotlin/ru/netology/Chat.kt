package ru.netology

data class Chat(
    val id: Long = UNDEFINED_ID,
    val userId: Long,
    val directId: Long,
    val isDelete: Boolean = false,
){
    companion object {
        const val UNDEFINED_ID = -1L
    }
}
