package com.pixelatedsource.jda.commands.util;

import com.pixelatedsource.jda.Helpers;
import com.pixelatedsource.jda.blub.Category;
import com.pixelatedsource.jda.blub.Command;
import com.pixelatedsource.jda.blub.CommandEvent;

import static com.pixelatedsource.jda.PixelSniper.PREFIX;

public class TexttoemojiCommand extends Command {

    public TexttoemojiCommand() {
        this.commandName = "t2e";
        this.description = "Converts input text and numbers to emotes";
        this.usage = PREFIX + this.commandName + " <text>";
        this.aliases = new String[]{"TextToEmotes", "Text2Emotes", "TextToEmojies", "Text2Emojies"};
        this.category = Category.UTILS;
    }

    @Override
    protected void execute(CommandEvent event) {
        boolean acces = false;
        if (event.getGuild() == null) acces = true;
        if (!acces) acces = Helpers.hasPerm(event.getGuild().getMember(event.getAuthor()), this.commandName, 0);
        if (acces) {
            StringBuilder sb = new StringBuilder();
            for (String s : event.getArgs().split("")) {
                if (Character.isLetter(s.toLowerCase().charAt(0))) {
                    sb.append(":regional_indicator_").append(s.toLowerCase()).append(":");
                } else if (Character.isDigit(s.charAt(0))) {
                    sb.append(":").append(Helpers.numberToString(Integer.valueOf(s))).append(":");
                } else {
                    if (" ".equals(s)) sb.append(" ");
                    sb.append(s);
                }
            }
            event.reply(sb.toString());
        }
    }
}
