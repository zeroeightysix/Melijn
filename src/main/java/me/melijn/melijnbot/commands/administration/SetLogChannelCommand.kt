package me.melijn.melijnbot.commands.administration

import kotlinx.coroutines.future.await
import me.melijn.melijnbot.enums.LogChannelType
import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.Translateable
import me.melijn.melijnbot.objects.utils.asTag
import me.melijn.melijnbot.objects.utils.getTextChannelByArgsNMessage
import me.melijn.melijnbot.objects.utils.sendMsg
import me.melijn.melijnbot.objects.utils.sendSyntax

class SetLogChannelCommand : AbstractCommand("command.setlogchannel") {

    init {
        id = 21
        name = "setLogChannel"
        aliases = arrayOf("slc")
        commandCategory = CommandCategory.ADMINISTRATION
    }

    override suspend fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            sendSyntax(context, syntax)
            return
        }

        val matchingEnums: List<LogChannelType> = LogChannelType.getMatchingTypesFromNode(context.args[0])
        if (matchingEnums.isEmpty()) {
            sendSyntax(context, syntax)
            return
        }
        if (matchingEnums.size > 1)
            handleEnums(context, matchingEnums)
        else handleEnum(context, matchingEnums[0])

    }

    private suspend fun handleEnum(context: CommandContext, logChannelType: LogChannelType) {
        if (context.args.size > 1) {
            setChannel(context, logChannelType)
        } else {
            displayChannel(context, logChannelType)
        }
    }

    private suspend fun displayChannel(context: CommandContext, logChannelType: LogChannelType) {
        val daoWrapper = context.daoManager.logChannelWrapper
        val pair = Pair(context.getGuildId(), logChannelType)
        val channelId = daoWrapper.logChannelCache.get(pair).await()
        val channel = context.getGuild().getTextChannelById(channelId)

        if (channelId != -1L && channel == null) {
            daoWrapper.removeChannel(pair.first, pair.second)
            return
        }

        val msg = (if (channel != null) {
            Translateable("$root.show.set.single").string(context)
                    .replace("%channel%", channel.asTag)
        } else {
            Translateable("$root.show.unset.single").string(context)
        }).replace("%logChannelType%", logChannelType.text)

        sendMsg(context, msg)
    }



    private suspend fun setChannel(context: CommandContext, logChannelType: LogChannelType) {
        if (context.args.size < 2) {
            sendSyntax(context, syntax)
            return
        }

        val daoWrapper = context.daoManager.logChannelWrapper
        val msg = if (context.args[1].equals("null", true)) {

            daoWrapper.removeChannel(context.getGuildId(), logChannelType)

            Translateable("$root.unset.single").string(context)
                    .replace("%logChannelType%", logChannelType.text)
        } else {
            val channel = getTextChannelByArgsNMessage(context, 1) ?: return
            daoWrapper.setChannel(context.getGuildId(), logChannelType, channel.idLong)

            Translateable("$root.set.single").string(context)
                    .replace("%logChannelType%", logChannelType.text)
                    .replace("%channel%", channel.asTag)

        }
        sendMsg(context, msg)
    }

    private suspend fun handleEnums(context: CommandContext, logChannelTypes: List<LogChannelType>) {
        if (context.args.size > 1) {
            setChannels(context, logChannelTypes)
        } else {
            displayChannels(context, logChannelTypes)
        }
    }

    private suspend fun displayChannels(context: CommandContext, logChannelTypes: List<LogChannelType>) {
        val daoWrapper = context.daoManager.logChannelWrapper
        val title = Translateable("$root.show.multiple").string(context)
                .replace("%channelCount%", logChannelTypes.size.toString())
                .replace("%logChannelTypeNode%", context.args[0])

        val lines = emptyList<String>().toMutableList()

        for (type in logChannelTypes) {
            val pair = Pair(context.getGuildId(), type)
            val channelId = daoWrapper.logChannelCache.get(pair).get()
            val channel = context.getGuild().getTextChannelById(channelId)

            if (channelId != -1L && channel == null) daoWrapper.removeChannel(pair.first, pair.second)
            lines += "${type.text}: " + (channel?.asMention ?: "/")
        }

        val content = lines.joinToString(separator = "\n", prefix = "\n")
        val msg = title + content
        sendMsg(context, msg)
    }

    private suspend fun setChannels(context: CommandContext, logChannelTypes: List<LogChannelType>) {
        if (context.args.size < 2) {
            sendSyntax(context, syntax)
            return
        }

        val daoWrapper = context.daoManager.logChannelWrapper
        val msg = if (context.args[1].equals("null", true)) {

            daoWrapper.removeChannels(context.getGuildId(), logChannelTypes)

            Translateable("$root.unset.multiple").string(context)
                    .replace("%channelCount%", logChannelTypes.size.toString())
                    .replace("%logChannelTypeNode%", context.args[0])
        } else {
            val channel = getTextChannelByArgsNMessage(context, 1) ?: return
            daoWrapper.setChannels(context.getGuildId(), logChannelTypes, channel.idLong)

            Translateable("$root.set.multiple").string(context)
                    .replace("%channelCount%", logChannelTypes.size.toString())
                    .replace("%logChannelTypeNode%", context.args[0])
                    .replace("%channel%", channel.asTag)

        }
        sendMsg(context, msg)
    }
}