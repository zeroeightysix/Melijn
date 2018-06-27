package com.pixelatedsource.jda.commands.management;

import com.pixelatedsource.jda.Helpers;
import com.pixelatedsource.jda.PixelSniper;
import com.pixelatedsource.jda.blub.Category;
import com.pixelatedsource.jda.blub.Command;
import com.pixelatedsource.jda.blub.CommandEvent;
import com.pixelatedsource.jda.utils.MessageHelper;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import static com.pixelatedsource.jda.PixelSniper.PREFIX;

public class MuteCommand extends Command {

    public MuteCommand() {
        this.commandName = "mute";
        this.description = "Mute user on your server and give them a nice message in pm.";
        this.usage = PREFIX + commandName + " <member> [reason]";
        this.category = Category.MANAGEMENT;
        this.aliases = new String[]{"permmute"};
        this.permissions = new Permission[]{
                Permission.MESSAGE_EMBED_LINKS,
                Permission.MANAGE_ROLES
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getGuild() != null) {
            if (Helpers.hasPerm(event.getMember(), commandName, 1)) {
                String[] args = event.getArgs().split("\\s+");
                Guild guild = event.getGuild();
                if (args.length >= 1) {
                    User target = Helpers.getUserByArgsN(event, args[0]);
                    String reason = event.getArgs().replaceFirst(args[0] + "\\s+|" + args[0], "");
                    if (target != null) {
                        if (SetMuteRoleCommand.muteRoles.getOrDefault(guild.getIdLong(), -1L) == -1) {
                            event.reply("**No mute role set!**\nCreating Role..");
                            TempMuteCommand.createMuteRole(guild);
                            event.reply("Role created. You can change the settings of the role to your desires in the role managment tab.\nThis role wil be added to the muted users so it should have no talk permissions!");
                        }
                        Role muteRole = guild.getRoleById(SetMuteRoleCommand.muteRoles.getOrDefault(guild.getIdLong(), -1L));
                        if (muteRole != null) {
                            guild.getController().addSingleRoleToMember(guild.getMember(target), muteRole).queue(s -> {
                                if (reason.length() <= 1000 && PixelSniper.mySQL.setPermMute(event.getAuthor(), target, guild, reason)) {
                                    event.getMessage().addReaction("\u2705").queue();
                                } else {
                                    event.getMessage().addReaction("\u274C").queue();
                                }
                            });
                        } else {
                            event.reply("Mute role is unset (cannot mute)");
                        }
                    } else {
                        event.reply("Unknown user");
                    }
                } else {
                    MessageHelper.sendUsage(this, event);
                }
            } else {
                event.reply("You need the permission `" + commandName + "` to execute this command.");
            }
        } else {
            event.reply(Helpers.guildOnly);
        }
    }
}
