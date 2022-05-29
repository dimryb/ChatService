package ru.netology

object MessageService : CrudService<ChatMessage> {
    private var id: Long = 0
    private val messages = sortedSetOf<ChatMessage>({ o1, o2 -> o1.id.compareTo(o2.id) })

    override fun add(entity: ChatMessage): Long {
        id++
        messages.add(entity.copy(id = id))
        return id
    }

    override fun delete(id: Long) {
        val message = getById(id)
        if (!message.isDelete) {
            messages.remove(message)
            messages.add(message.copy(isDelete = true))
        }
    }

    override fun edit(entity: ChatMessage) {
        val oldMessage = getById(entity.id)
        if (entity.isDelete) throw MessageServiceException("The deleted Message is not editable")
        messages.remove(oldMessage)
        messages.add(oldMessage.copy(text = entity.text, isRead = entity.isRead))
    }

    override fun read(): List<ChatMessage> {
        return messages.toList()
    }

    override fun getById(id: Long): ChatMessage {
        return messages.find {
            it.id == id
        } ?: throw MessageServiceException("Comment with id $id not found")
    }

    fun get(): List<ChatMessage> {
        return messages.toList()
    }

    override fun restore(id: Long) {
        val message = getById(id)
        if (message.isDelete) {
            messages.remove(message)
            messages.add(message.copy(isDelete = false))
        }
    }

    fun getUnreadMessagesCount(userId: Long, chatId: Long): Int{
        return messages
            .filter { it.chatId == chatId }
            .filter { it.directId == userId }
            .filter { !it.isDelete }
            .count { !it.isRead }
    }

    fun getUnreadMessages(userId: Long, chatId: Long, lastMsgId: Long, quantity: Int): List<ChatMessage>{
        val unreadMessages = messages
            .filter { it.chatId == chatId }
            .filter { it.directId == userId }
            .filter { !it.isDelete }
            .filter { !it.isRead }
            .filter { it.id > lastMsgId }
            .take(quantity)
        unreadMessages.forEach { setAsRead(it.id) }
        return unreadMessages
    }

    private fun setAsRead(id: Long){
        val message = getById(id)
        edit(message.copy(isRead = true))
    }

    fun getMessagesCount(userId: Long, chatId: Long): Int{
        return messages
            .filter { it.chatId == chatId }
            .count { !it.isDelete }
    }

    fun clean(): MessageService {
        messages.clear()
        id = 0
        return this
    }
}