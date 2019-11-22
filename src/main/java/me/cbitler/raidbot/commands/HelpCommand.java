package me.cbitler.raidbot.commands;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class HelpCommand implements Command {
    private final String helpMessage = "GW2-Raid-Bot Help:\n" +
            "Commands:\n" +
            "**!setRaidLeaderRole [role]** - Set the role that serves as a raid leader. This is only usable by people with the manage server permission\n" +
            "**!setRaidChannel [channel]** - Set a channel where the bot has to post the raids. If this option is not set then the bot will ask you during the creation of the raid for a channel . This is only usable by people with the manage server permission.  \n" +
            "**!setRaidChannel null** - Let the channel be picked during the creation of a raid\n" +
            "**!createRaid** - Start the raid creation process. Usable by people with the raid leader role\n" +
            "**!removeFromRaid [raid id] [name]** - Remove a player from a raid. Only usable by raid leaders\n" +
            "**!endRaid [raid id] [log link 1] [log link 2] ...** - End a raid, removing the message and DM'ing the users in the raid with log links. The log links are optional arguments\n" +
            "**!help** - You are looking at it\n" +
            "**!info** - Information about the bot and it's author\n" +
            "\n\n" +
            "Help information:\n" +
            "To use this bot, set the raid leader role, and then anyone with that role can use !createRaid. This will take them through" +
            " a raid setup process with the bot prompting them for information. After that, it will create the raid in the channel specified" +
            " Once that is there, people can join it by clicking on the reaction for their specialization, and then responding to the bot with the role" +
            " that they want. If a role is full, the person will be added as a back-up (max amount of back-ups for that role is the same as the original role)";
    @Override
    public void handleCommand(String command, String[] args, TextChannel channel, User author) {
        channel.sendMessage(helpMessage).queue();
    }
}
