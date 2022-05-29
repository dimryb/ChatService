package ru.netology

object ChatService {
    private var chatId: Long = 0
    private val chats = sortedSetOf<Chat>({ o1, o2 -> o1.id.compareTo(o2.id) })
    private val messageService = MessageService

    fun add(userId: Long, directId: Long, text: String): Pair<Long, Long> {
        chatId++
        chats.add(Chat(id = this.chatId, creatorId = userId, secondUserId = directId))
        val messageId = addMessage(userId, chatId, text)
        return Pair(chatId, messageId)
    }

    fun delete(userId: Long, id: Long) {
        val chat = getById(userId, id)
        if (chat.creatorId == userId || chat.secondUserId == userId) {
            if (!chat.isDelete) {
                chats.remove(chat)
                chats.add(chat.copy(isDelete = true))
            }
        } else throw ChatServiceException("Chat with id $id not delete")
    }

    fun getById(userId: Long, id: Long): Chat {
        return chats
            .filter { it.creatorId == userId || it.secondUserId == userId }
            .find { it.id == id }
            ?: throw ChatServiceException("Chat with id $id not found")
    }

    fun get(userId: Long): List<Chat> {
        return chats
            .filter { it.creatorId == userId || it.secondUserId == userId }
            .filter { !it.isDelete }
            .toList()
    }

    fun addMessage(userId: Long, chatId: Long, text: String): Long {
        val chat = getById(userId, chatId)
        if (!chat.isDelete) {
            val directId = when(userId){
                chat.creatorId -> chat.secondUserId
                chat.secondUserId -> chat.creatorId
                else -> throw ChatServiceException("Incorrect user id $userId")
            }
            return messageService.add(ChatMessage(chatId = chat.id, userId = userId, directId = directId, text = text))
        } else throw ChatServiceException("You cannot add a message from a deleted chat")
    }

    fun editMessage(userId: Long, messageId :Long, newText: String) {
        val message = messageService.getById(messageId)
        if(!message.isDelete) {
            val chat = getById(userId, message.chatId)
            if (!chat.isDelete) {
                messageService.edit(message.copy(text = newText))
            } else throw ChatServiceException("You cannot edit a message from a deleted chat")
        } else throw ChatServiceException("Can't edit deleted messages")
    }

    fun deleteMessage(userId: Long, messageId: Long) {
        val message = messageService.getById(messageId)
        val chat = getById(userId, message.chatId)
        if (!chat.isDelete) {
            messageService.delete(message.id)
            if (messageService.getMessagesCount(userId, chat.id) == 0) {
                delete(userId, chat.id)
            }
        } else throw ChatServiceException("You cannot delete a message from a deleted chat")
    }

    fun getUnreadChatsCount(userId: Long): Int {
        return chats
            .filter { it.creatorId == userId || it.secondUserId == userId }
            .filter { !it.isDelete }
            .count { messageService.getUnreadMessagesCount(userId, it.id) > 0 }
    }

    fun getChats(userId: Long): List<Chat> {
        return chats
            .filter { it.creatorId == userId || it.secondUserId == userId }
            .filter { !it.isDelete }
            .filter { messageService.getUnreadMessagesCount(userId, it.id) > 0 }
    }

    fun getUnreadMessages(userId: Long, chatId: Long, lastMsgId: Long, quantity: Int): List<ChatMessage> {
        val chat = chats
            .filter { it.creatorId == userId || it.secondUserId == userId }
            .filter { !it.isDelete }
            .find { it.id == chatId } ?: throw ChatServiceException("Chat with id $chatId not found")

        return messageService.getUnreadMessages(userId, chat.id, lastMsgId, quantity)
    }

    fun getAllMessages(userId: Long, chatId: Long): List<ChatMessage>{
        val chat = chats
            .filter { it.creatorId == userId || it.secondUserId == userId }
            .filter { !it.isDelete }
            .find { it.id == chatId } ?: return emptyList<ChatMessage>()

        return messageService.get()
    }

    fun clean(): ChatService {
        chats.clear()
        chatId = 0
        messageService.clean()
        return this
    }
}