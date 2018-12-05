package io.github.wulkanowy.api.repository

import io.github.wulkanowy.api.messages.Message
import io.github.wulkanowy.api.messages.Recipient
import io.github.wulkanowy.api.messages.ReportingUnit
import io.github.wulkanowy.api.service.MessagesService
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class MessagesRepository(private val userId: Int, private val api: MessagesService) {

    private val reportingUnit by lazy {
        getReportingUnits().map { list -> list.first { it.senderId == userId } }
    }

    private val recipients by lazy {
        getRecipients(2).map { list ->
            list.map {
                Pair(it, it.name.split("[").last().split("]").first())
            }
        }
    }

    fun getReportingUnits(): Single<List<ReportingUnit>> {
        return api.getUserReportingUnits().map { it.data }
    }

    fun getRecipients(role: Int): Single<List<Recipient>> {
        return reportingUnit.map { unit -> api.getRecipients(unit.id, role).map { it.data } }.flatMap { it }
    }

    fun getReceivedMessages(startDate: LocalDateTime?, endDate: LocalDateTime?): Single<List<Message>> {
        return api.getReceived(getDate(startDate), getDate(endDate))
                .map { res ->
                    res.data?.asSequence()
                            ?.map { it.copy(folderId = 1) }
                            ?.sortedBy { it.date }?.toList()
                }
    }

    fun getSentMessages(startDate: LocalDateTime?, endDate: LocalDateTime?): Single<List<Message>> {
        return api.getSent(getDate(startDate), getDate(endDate))
                .map { res -> res.data?.asSequence()?.map { it.copy(folderId = 2) }?.sortedBy { it.date }?.toList() }
                .flatMapObservable { Observable.fromIterable(it) }
                .flatMap { message ->
                    recipients.flatMapObservable {
                        Observable.fromIterable(it.filter { recipient ->
                            recipient.second == message.recipient?.split("[")?.last()?.split("]")?.first()
                        })
                    }.map {
                        message.copy(recipient = it.first.name.split(" [").first(), messageId = message.id).apply {
                            recipientId = it.first.loginId
                        }
                    }
                }
                .toList()
    }

    fun getDeletedMessages(startDate: LocalDateTime?, endDate: LocalDateTime?): Single<List<Message>> {
        return api.getDeleted(getDate(startDate), getDate(endDate))
                .map { res -> res.data?.map { it.apply { removed = true } }?.sortedBy { it.date } }
    }

    fun getMessage(messageId: Int, folderId: Int, read: Boolean, id: Int?): Single<String> {
        return api.getMessage(messageId, folderId, read, id).map { it.data?.content }
    }

    private fun getDate(date: LocalDateTime?): String {
        if (date == null) return ""
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
