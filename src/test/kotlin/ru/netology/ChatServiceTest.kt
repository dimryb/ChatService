package ru.netology

import org.junit.Test

import org.junit.Assert.*

class ChatServiceTest {

    @Test
    fun add_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Привет, давай общаться!"
        val (chatId, _) = service.add(userId, interlocutorId, messageText)

        val chat = service.get(userId).find { it.id == chatId }
        assertNotNull(chat)
        val messages = service.getAllMessages(userId, chatId)
        assertTrue(messages.size == 1)
        assertTrue(messages[0].text == messageText)
    }

    @Test
    fun delete_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Привет, давай общаться!"
        val (chatId, _) = service.add(userId, interlocutorId, messageText)
        service.delete(userId, chatId)

        val chat = service.get(userId).find { it.id == chatId }
        assertNull(chat)
    }

    @Test(expected = ChatServiceException::class)
    fun delete_badChatId() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Привет, давай общаться!"
        service.add(userId, interlocutorId, messageText)
        val badChatId = 10L
        service.delete(userId, badChatId)
    }

    @Test
    fun getById_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Привет, давай общаться!"
        val (chatId, _) = service.add(userId, interlocutorId, messageText)

        val chat = service.getById(userId, chatId)
        assertTrue(chat.id == chatId)
        assertTrue(chat.creatorId == userId)
        assertTrue(chat.secondUserId == interlocutorId)
        assertFalse(chat.isDelete)
    }

    @Test
    fun get_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorIdFirst = 2L
        val interlocutorIdSecond = 2L
        val messageText = "Привет, давай общаться!"
        service.add(userId, interlocutorIdFirst, messageText)
        service.add(userId, interlocutorIdSecond, messageText)

        val chats = service.get(userId)
        assertTrue(chats.size == 2)
    }

    @Test
    fun addMessage_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Привет, давай общаться!"
        val (chatId, _) = service.add(userId, interlocutorId, messageText)

        val messageTextAnswer = "Привет! Ну, давай"
        service.addMessage(interlocutorId, chatId, messageTextAnswer)

        val messagesUser1 = service.getAllMessages(userId, chatId)
        assertTrue(messagesUser1.size == 2)
        assertTrue(messagesUser1[0].text == messageText)
        assertTrue(messagesUser1[1].text == messageTextAnswer)

        val messagesUser2 = service.getAllMessages(interlocutorId, chatId)
        assertTrue(messagesUser2.size == 2)
        assertTrue(messagesUser2[0].text == messageText)
        assertTrue(messagesUser2[1].text == messageTextAnswer)
    }

    @Test
    fun editMessage_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Ghbdtn? lfdfq j,ofnmcz!"
        val (chatId, messageId) = service.add(userId, interlocutorId, messageText)
        val messageTextEdited = "Привет, давай общаться!"
        service.editMessage(userId = userId, messageId = messageId, newText = messageTextEdited)

        val message = service.getAllMessages(userId, chatId).find { it.id == messageId }
        assertNotNull(message)
        assertTrue(message?.text == messageTextEdited)
    }

    @Test
    fun deleteMessage_deleteAllMessage() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val messageText = "Привет, давай общаться!"
        val (chatId, messageId1) = service.add(userId, interlocutorId, messageText)

        val messageTextAnswer = "Привет! Ну, давай"
        val messageId2 = service.addMessage(interlocutorId, chatId, messageTextAnswer)

        service.deleteMessage(userId = userId, messageId = messageId1)

        val message1 = service.getAllMessages(userId, chatId).find { it.id == messageId1 }
        assertTrue(message1?.isDelete == true)

        service.deleteMessage(userId = interlocutorId, messageId = messageId2)
        val chat = service.getById(userId = interlocutorId, chatId)
        assertTrue(chat.isDelete)

        val message2 = service.getAllMessages(userId, chatId).find { it.id == messageId2 }
        assertNull(message2)
    }

    @Test
    fun getUnreadChatsCount_normal() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorIdFirst = 2L
        val interlocutorIdSecond = 50L
        val (chatIdFirst, _) = service.add(userId, interlocutorIdFirst, "Привет, Первый давай общаться!")
        service.addMessage(interlocutorIdFirst, chatIdFirst, "Привет! Ну, давай")
        service.addMessage(interlocutorIdFirst, chatIdFirst, "О чем общаться")
        service.addMessage(interlocutorIdFirst, chatIdFirst, "А?")

        val (chatIdSecond, _) = service.add(userId, interlocutorIdSecond, "Привет, Второй давай общаться!")
        service.addMessage(interlocutorIdSecond, chatIdSecond, "Привет! Не хочу..")

        val count = service.getUnreadChatsCount(userId)
        assertTrue(count == 2)
    }

    @Test
    fun getChats() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorIdFirst = 2L
        val interlocutorIdSecond = 50L
        val (chatIdFirst, _) = service.add(userId, interlocutorIdFirst, "Привет, Первый давай общаться!")
        service.addMessage(interlocutorIdFirst, chatIdFirst, "Привет! Ну, давай")
        service.addMessage(interlocutorIdFirst, chatIdFirst, "О чем общаться")
        service.addMessage(interlocutorIdFirst, chatIdFirst, "А?")

        val (chatIdSecond, _) = service.add(userId, interlocutorIdSecond, "Привет, Второй давай общаться!")
        service.addMessage(interlocutorIdSecond, chatIdSecond, "Привет! Не хочу..")

        val chats = service.getChats(userId)
        assertTrue(chats.size == 2)

        val chatsFirst = service.getChats(interlocutorIdFirst)
        assertTrue(chatsFirst.size == 1)

        val chatsSecond = service.getChats(interlocutorIdSecond)
        assertTrue(chatsSecond.size == 1)
    }

    @Test
    fun getUnreadMessages() {
        val service = ChatService.clean()

        val userId = 1L
        val interlocutorId = 2L
        val (chatId, _) = service.add(userId, interlocutorId, "Привет, давай общаться!")
        service.addMessage(interlocutorId, chatId, "Привет! Ну, давай")
        service.addMessage(interlocutorId, chatId, "О чем общаться")
        service.addMessage(interlocutorId, chatId, "А?")

        val unreadMessages = service.getUnreadMessages(userId, chatId, lastMsgId = 0, quantity = 10)
        assertTrue(unreadMessages.size == 3)

        val unreadMessages2 = service.getUnreadMessages(userId, chatId, lastMsgId = 0, quantity = 10)
        assertTrue(unreadMessages2.isEmpty())
    }
}