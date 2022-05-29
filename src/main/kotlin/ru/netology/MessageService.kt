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
        messages.add(oldMessage.copy(text = entity.text))
    }

    override fun read(): List<ChatMessage> {
        return messages.toList()
    }

    override fun getById(id: Long): ChatMessage {
        return messages.find {
            it.id == id
        } ?: throw MessageServiceException("Comment with id $id not found")
    }

    override fun restore(id: Long) {
        val message = getById(id)
        if (message.isDelete) {
            messages.remove(message)
            messages.add(message.copy(isDelete = false))
        }
    }
}