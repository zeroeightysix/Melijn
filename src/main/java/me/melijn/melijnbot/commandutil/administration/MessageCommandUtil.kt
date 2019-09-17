package me.melijn.melijnbot.commandutil.administration

import kotlinx.coroutines.future.await
import me.melijn.melijnbot.database.message.MessageWrapper
import me.melijn.melijnbot.database.message.ModularMessage
import me.melijn.melijnbot.enums.MessageType
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.PLACEHOLDER_ARG
import me.melijn.melijnbot.objects.translation.PLACEHOLDER_TYPE
import me.melijn.melijnbot.objects.translation.i18n
import me.melijn.melijnbot.objects.utils.getColorFromArgNMessage
import me.melijn.melijnbot.objects.utils.sendMsg
import me.melijn.melijnbot.objects.utils.toHex
import net.dv8tion.jda.api.EmbedBuilder
import org.json.JSONObject

object MessageCommandUtil {

    suspend fun removeMessageIfEmpty(guildId: Long, type: MessageType, message: ModularMessage, messageWrapper: MessageWrapper): Boolean {
        return if (messageWrapper.shouldRemove(message)) {
            messageWrapper.removeMessage(guildId, type)
            true
        } else{
            false
        }
    }

    suspend fun setMessageContent(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val oldMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()
        val arg = context.rawArg


        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeMessageContent(oldMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.content.set.unset")
        } else {
            messageWrapper.setMessageContent(oldMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.content.set")
                .replace("%arg%", arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showMessageContent(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val content = modularMessage?.messageContent
        val msg = if (content == null) {
            i18n.getTranslation(language,"message.content.show.unset")
        } else {
            i18n.getTranslation(language,"message.content.show.set")
                .replace("%content%", content)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedDescription(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val oldMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val arg = context.rawArg


        val msg = if (arg.equals("null", true)) {
            if (oldMessage != null) {
                messageWrapper.removeEmbedDescription(oldMessage, context.getGuildId(), type)
            }
            i18n.getTranslation(language,"message.embed.description.unset")
        } else {
            messageWrapper.setEmbedDescription(oldMessage ?: ModularMessage(), context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.description.set")
                .replace("%arg%", arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedDescription(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val description = modularMessage?.embed?.description
        val msg = if (description == null) {
            i18n.getTranslation(language,"message.embed.description.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.description.show.set")
                .replace("%content%", description)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun clearEmbed(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()

        if (modularMessage != null) {
            messageWrapper.clearEmbed(modularMessage, context.getGuildId(), type)
        }

        val msg = i18n.getTranslation(language,"message.embed.clear")
            
            .replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)

    }

    suspend fun listAttachments(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()

        val msg = if (modularMessage == null || modularMessage.attachments.isEmpty()) {
            i18n.getTranslation(language,"message.attachments.list.empty")
                .replace(PLACEHOLDER_TYPE, type.text)

        } else {
            val title = i18n.getTranslation(language,"message.attachments.list.title")
                .replace(PLACEHOLDER_TYPE, type.text)
            var content = "\n```INI"
            for ((index, attachment) in modularMessage.attachments.entries.withIndex()) {
                content += "\n$index - [${attachment.key}] - ${attachment.value}"
            }
            content += "```"
            (title + content)
        }
        sendMsg(context, msg)
    }

    suspend fun addAttachment(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val newMap = modularMessage.attachments.toMutableMap()
        newMap[context.args[0]] = context.args[1]

        modularMessage.attachments = newMap.toMap()

        messageWrapper.setMessage(context.getGuildId(), type, modularMessage)
        val msg = i18n.getTranslation(language,"message.attachments.add")
            .replace(PLACEHOLDER_TYPE, type.text)
            .replace("%attachment%", context.args[0])
            .replace("%file%", context.args[1])

        sendMsg(context, msg)
    }

    suspend fun removeAttachment(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val attachments = modularMessage.attachments.toMutableMap()
        val file = if (attachments.containsKey(context.args[0])) attachments[context.args[0]] else null
        attachments.remove(context.args[0])

        modularMessage.attachments = attachments.toMap()

        val msg =
            if (file == null) {
                i18n.getTranslation(language,"message.attachments.remove.notanattachment")

            } else {
                messageWrapper.setMessage(context.getGuildId(), type, modularMessage)
                i18n.getTranslation(language,"message.attachments.remove.success")
                    .replace("%file%", file)
            }.replace(PLACEHOLDER_ARG, context.args[0])
                .replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedColor(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val oldMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val color = oldMessage?.embed?.color

        val msg = if (color == null) {
            i18n.getTranslation(language,"message.embed.color.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.color.show.set")
                .replace("%color%", color.toHex())
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedColor(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedColor(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.color.unset")
        } else {
            val color = getColorFromArgNMessage(context, 0) ?: return
            messageWrapper.setEmbedColor(modularMessage, context.getGuildId(), type, color)
            i18n.getTranslation(language,"message.embed.color.set")
                .replace("%arg%", color.toHex())
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedTitle(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedTitleContent(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.title.unset")
        } else {
            messageWrapper.setEmbedTitleContent(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.title.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedTitle(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val title = modularMessage?.embed?.title

        val msg = if (title == null) {
            i18n.getTranslation(language,"message.embed.title.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.title.show.set")
                .replace("%title%", title)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedTitleUrl(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val url = modularMessage?.embed?.url

        val msg = if (url == null) {
            i18n.getTranslation(language,"message.embed.titleurl.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.titleurl.show.set")
                .replace("%url%", url)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedTitleUrl(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedTitleURL(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.titleurl.show.unset")
        } else {
            messageWrapper.setEmbedTitleURL(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.titleurl.show.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedAuthor(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val url = modularMessage?.embed?.author?.name

        val msg = if (url == null) {
            i18n.getTranslation(language,"message.embed.authorname.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.authorname.show.set")
                .replace("%name%", url)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedAuthor(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedAuthorContent(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.authorname.unset")
        } else {
            messageWrapper.setEmbedAuthorContent(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.authorname.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedAuthorIcon(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val url = modularMessage?.embed?.author?.iconUrl

        val msg = if (url == null) {
            i18n.getTranslation(language,"message.embed.authoricon.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.authoricon.show.set")
                .replace("%url", url)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedAuthorIcon(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedAuthorIconURL(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.authoricon.unset")
        } else {
            messageWrapper.setEmbedAuthorIconURL(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.authoricon.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedAuthorUrl(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val url = modularMessage?.embed?.author?.url

        val msg = if (url == null) {
            i18n.getTranslation(language,"message.embed.authorurl.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.authorurl.show.set")
                .replace("%url", url)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedAuthorUrl(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedAuthorURL(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.authorurl.unset")
        } else {
            messageWrapper.setEmbedAuthorURL(modularMessage, context.getGuildId(), type, context.rawArg)
            i18n.getTranslation(language,"message.embed.authorurl.set")
                .replace(PLACEHOLDER_ARG, context.rawArg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedThumbnail(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val url = modularMessage?.embed?.thumbnail?.url

        val msg = if (url == null) {
            i18n.getTranslation(language,"message.embed.thumbnail.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.thumbnail.show.set")
                .replace("%url", url)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedThumbnail(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedThumbnail(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.thumbnail.unset")
        } else {
            messageWrapper.setEmbedThumbnail(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.thumbnail.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedImage(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val url = modularMessage?.embed?.image?.url

        val msg = if (url == null) {
            i18n.getTranslation(language,"message.embed.image.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.image.show.set")
                .replace("%url", url)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedImage(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedImage(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.image.unset")
        } else {
            messageWrapper.setEmbedImage(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.image.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedFooter(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val footer = modularMessage?.embed?.footer?.text

        val msg = if (footer == null) {
            i18n.getTranslation(language,"message.embed.footer.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.footer.show.set")
                .replace("%url", footer)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedFooter(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedFooterContent(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.footer.unset")
        } else {
            messageWrapper.setEmbedFooterContent(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.footer.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedFooterIcon(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val footerUrl = modularMessage?.embed?.footer?.iconUrl

        val msg = if (footerUrl == null) {
            i18n.getTranslation(language,"message.embed.footericon.show.unset")
        } else {
            i18n.getTranslation(language,"message.embed.footericon.show.set")
                .replace("%url", footerUrl)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedFooterIcon(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val arg = context.rawArg

        val msg = if (arg.equals("null", true)) {
            messageWrapper.removeEmbedFooterURL(modularMessage, context.getGuildId(), type)
            i18n.getTranslation(language,"message.embed.footericon.show.unset")
        } else {
            messageWrapper.setEmbedFooterURL(modularMessage, context.getGuildId(), type, arg)
            i18n.getTranslation(language,"message.embed.footericon.show.set")
                .replace(PLACEHOLDER_ARG, arg)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun addEmbedField(title: String, value: String, inline: Boolean, context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()
        val embed = modularMessage.embed ?: EmbedBuilder().build()
        val embedBuilder = EmbedBuilder(embed)
        embedBuilder.addField(title, value, inline)
        modularMessage.embed = embedBuilder.build()

        messageWrapper.setMessage(context.getGuildId(), type, modularMessage)
        val inlineString = i18n.getTranslation(language,if (inline) "yes" else "no")
        val msg = i18n.getTranslation(language,"message.embed.field.add")
            .replace("%title%", title)
            .replace("%value%", value)
            .replace("%inline%", inlineString)
            .replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun setEmbedFieldTitle(index: Int, title: String, context: CommandContext, type: MessageType) {
        setEmbedFieldPart(index, "title", title, context, type)
    }

    suspend fun setEmbedFieldValue(index: Int, value: String, context: CommandContext, type: MessageType) {
        setEmbedFieldPart(index, "value", value, context, type)
    }

    suspend fun setEmbedFieldInline(index: Int, inline: Boolean, context: CommandContext, type: MessageType) {
        setEmbedFieldPart(index, "inline", inline, context, type)
    }

    suspend fun setEmbedFieldPart(index: Int, partName: String, value: Any, context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        var modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val json = JSONObject(modularMessage.toJSON())
        val embedJSON = json.getJSONObject("embed")
        val fieldsJSON = embedJSON.getJSONArray("fields")
        val field = fieldsJSON.getJSONObject(index)
        field.put(partName, value)
        fieldsJSON.put(index, field)
        embedJSON.put("fields", fieldsJSON)
        json.put("embed", embedJSON)
        modularMessage = ModularMessage.fromJSON(json.toString(4))

        messageWrapper.setMessage(context.getGuildId(), type, modularMessage)
        val partValue: String = when (value) {
            is Boolean -> i18n.getTranslation(language,if (value) "yes" else "no")
            else -> value.toString()
        }
        val msg = i18n.getTranslation(language,"message.embed.field$partName.set")
            .replace("%index%", index.toString())
            .replace("%$partName%", partValue)
            .replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun removeEmbedField(index: Int, context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        var modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
            ?: ModularMessage()

        val json = JSONObject(modularMessage.toJSON())
        val embedJSON = json.getJSONObject("embed")
        val fieldsJSON = embedJSON.getJSONArray("fields")
        fieldsJSON.remove(index)
        embedJSON.put("fields", fieldsJSON)
        json.put("embed", embedJSON)
        modularMessage = ModularMessage.fromJSON(json.toString(4))

        messageWrapper.setMessage(context.getGuildId(), type, modularMessage)
        val msg = i18n.getTranslation(language,"message.embed.field.removed")
            .replace("%index%", index.toString())
            .replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }

    suspend fun showEmbedFields(context: CommandContext, type: MessageType) {
        val language = context.getLanguage()
        val messageWrapper = context.daoManager.messageWrapper
        val modularMessage = messageWrapper.messageCache.get(Pair(context.getGuildId(), type)).await()
        val fields = modularMessage?.embed?.fields

        val msg = if (fields == null || fields.isEmpty()) {
            i18n.getTranslation(language,"message.embed.field.list.empty")
        } else {
            val title = i18n.getTranslation(language,"message.embed.field.list.title")
            var desc = "```INI"
            for ((index, field) in fields.withIndex()) {
                desc += "\n$index - [${field.name}] - [${field.value}] - ${if (field.isInline) "true" else "\nfalse"}"
            }
            desc += "```"
            (title + desc)
        }.replace(PLACEHOLDER_TYPE, type.text)

        sendMsg(context, msg)
    }
}