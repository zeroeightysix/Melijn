package me.melijn.melijnbot.enums

enum class MessageType(val base: String, val messageTemplate: MessageTemplate? = null) {

    // Join/Leave things
    PRE_VERIFICATION_JOIN("PreVerificationJoin"),
    PRE_VERIFICATION_LEAVE("PreVerificationLeave"),
    JOIN("Join"),
    LEAVE("Leave"),
    BANNED("Banned"),
    KICKED("Kicked"),

    // Special Events
    BIRTHDAY("Birthday"),
    BOOST("Boost"),

    // Punishments
    BAN("Ban", MessageTemplate.BAN),
    TEMP_BAN("TempBan", MessageTemplate.TEMP_BAN),
    MASS_BAN("MassBan", MessageTemplate.MASS_BAN),
    SOFT_BAN("SoftBan", MessageTemplate.SOFT_BAN),
    UNBAN("Unban", MessageTemplate.UNBAN),

    MUTE("Mute"),
    TEMP_MUTE("TempMute"),
    UNMUTE("Unmute"),

    MASS_KICK("MassKick"),
    KICK("Kick"),
    WARN("Warn"),

    MASS_BAN_LOG("MassBanLog"),
    BAN_LOG("BanLog"),
    TEMP_BAN_LOG("TempBanLog"),
    SOFT_BAN_LOG("SoftBanLog"),
    UNBAN_LOG("UnbanLog"),

    MUTE_LOG("MuteLog"),
    TEMP_MUTE_LOG("TempMuteLog"),
    UNMUTE_LOG("UnmuteLog"),

    MASS_KICK_LOG("MassKickLog"),
    KICK_LOG("KickLog"),
    WARN_LOG("WarnLog"),

    VERIFICATION_LOG("VerificationLog");


    val text: String = "${base}Message"

    companion object {

        fun getMatchingTypesFromNode(node: String): List<MessageType> {
            return values().filter { msgType ->
                node.equals("all", true)
                    || msgType.text.equals(node, true)
                    || msgType.base.equals(node, true)
                    || msgType.toString().equals(node, true)
            }
        }
    }
}