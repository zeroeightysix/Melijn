package me.melijn.jda.commands.util;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import me.duncte123.weebJava.WeebInfo;
import me.melijn.jda.Helpers;
import me.melijn.jda.Melijn;
import me.melijn.jda.blub.Category;
import me.melijn.jda.blub.Command;
import me.melijn.jda.blub.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;

import static me.melijn.jda.Melijn.OWNERID;
import static me.melijn.jda.Melijn.PREFIX;

public class InfoCommand extends Command {

    public InfoCommand() {
        this.commandName = "info";
        this.usage = PREFIX + this.commandName;
        this.description = "Shows you useful info about the bot itself";
        this.aliases = new String[]{"about", "botinfo", "author"};
        this.category = Category.UTILS;
        this.permissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getGuild() == null || Helpers.hasPerm(event.getMember(), this.commandName, 0)) {
            try {
                event.reply(new EmbedBuilder()
                        .setColor(Helpers.EmbedColor)
                        .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                        .addField("About", "" +
                                "\nI'm a powerful discord bot developed by **ToxicMushroom#2610**" +
                                "\nMore commands/features are still being added, you can even request them in the support server below" +
                                "\n\n**[Support Server](https://discord.gg/cCDnvNg)** • **[Invite](https://melijn.com/invite?perms=true)** • **[Website](https://melijn.com/)**" +
                                "\n\u200B", false)
                        .addField("Info", "" +
                                "\n**Operating System** " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version") +
                                "\n**Coded in** Java" +
                                "\n\u200B", false)
                        .addField("Libraries", "" +
                                "**Java Version** " + System.getProperty("java.version") +
                                "\n**JDA Version** " + JDAInfo.VERSION +
                                "\n**Lavaplayer Version** " + PlayerLibrary.VERSION +
                                "\n**Weeb.java Version** " + WeebInfo.VERSION +
                                "\n**MySQL Version** " + Melijn.mySQL.getMySQLVersion() +
                                "\n**MySQL Connector Version** " + Melijn.mySQL.getConnectorVersion(), false)
                        .setFooter("Requested by " + event.getFullAuthorName(), event.getAvatarUrl())
                        .build());
                if (event.getAuthor().getIdLong() == OWNERID) {
                    StringBuilder desc = new StringBuilder();
                    desc.append("```Less\n");
                    int blub = 0;
                    for (Guild guild : event.getJDA().asBot().getShardManager().getGuildCache()) {
                        if (guild.getAudioManager().isConnected() || guild.getAudioManager().isAttemptingToConnect())
                            desc.append("#").append(++blub).append(" - ").append(guild.getName()).append("\n");
                    }
                    desc.append("```");
                    if (desc.length() > 11)
                        event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(desc.toString()).queue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            event.reply("You need the permission `" + commandName + "` to execute this command.");
        }
    }
}
