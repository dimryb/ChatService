package ru.netology

object ChatService {
    private var chatId: Long = 0
    private val chats = sortedSetOf<Chat>({ o1, o2 -> o1.id.compareTo(o2.id) })
    private val messageService = MessageService

    enum class Direct {
        OUTGOING,
        INCOMING,
    }

    fun add(chat: Chat, text: String, direct: Direct): Long {
        chatId++
        chats.add(chat.copy(id = this.chatId))
        addMessage(chatId, text, direct)
        return chatId
    }

    fun delete(id: Long){
        val chat = getById(id)
        if (!chat.isDelete) {
            chats.remove(chat)
            chats.add(chat.copy(isDelete = true))
        }
    }

    fun getById(id: Long): Chat {
        return chats.find {
            it.id == id
        } ?: throw ChatServiceException("Chat with id $id not found")
    }

    fun get(): List<Chat> {
        return chats.toList()
    }

    fun addMessage(chatId: Long, text: String, direct: Direct){
        val chat = getById(chatId)
        if(!chat.isDelete) {
            val userId = when (direct){
                Direct.OUTGOING -> chat.userId
                Direct.INCOMING -> chat.directId
            }
            messageService.add(ChatMessage(chatId = chat.id, userId = userId, text = text))
        }else throw ChatServiceException("You cannot add a message from a deleted chat")
    }

    fun editMessage(newMessage: ChatMessage){
        val chat = getById(newMessage.chatId)
        if(!chat.isDelete) {
            messageService.edit(newMessage)
        }else throw ChatServiceException("You cannot edit a message from a deleted chat")
    }

    fun deleteMessage(messageId: Long){
        val message = messageService.getById(messageId)
        val chat = getById(message.chatId)
        if(!chat.isDelete) {
            messageService.delete(message.id)
        }else throw ChatServiceException("You cannot delete a message from a deleted chat")
    }
}