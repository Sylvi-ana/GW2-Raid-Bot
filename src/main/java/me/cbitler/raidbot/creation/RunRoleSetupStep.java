package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

/**
 * Role setup step for the raid.
 * This one should take multiple inputs and as a result it doesn't finish until the user
 * types 'done'.
 * @author Christopher Bitler
 */
public class RunRoleSetupStep implements CreationStep {

    /**
     * Handle user input - should be in the format [number]:[role] unless it is 'done'.
     * @param e The direct message event
     * @return True if the user entered 'done', false otherwise
     */
    public boolean handleDM(PrivateMessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());

        if(e.getMessage().getRawContent().equalsIgnoreCase("done")) {
            if(raid.getRolesWithNumbers().size() > 0) {
                return true;
            } else {
                e.getChannel().sendMessage("You must add atleast one role.").queue();
                return false;
            }
        } else {
            String[] parts = e.getMessage().getRawContent().split(":");
            if(parts.length < 2) {
                e.getChannel().sendMessage("You need to specify the role in the format [amount]:[Role name]").queue();
            } else {
                try {
                    addRoles(e.getMessage().getRawContent(), raid);
                    e.getChannel().sendMessage("Role(s) added").queue();
                } catch (Exception ex) {
                    e.getChannel().sendMessage("Invalid input: Make sure it's in the format of [number]:[role]/[number]:[role], like 1:DPS/1:Tank"
                            + "Be careful, every role before the mistake is added to the raid!").queue();
                }
            }
            return false;
        }
    }
    
    public void addRoles(String s, PendingRaid raid){
        String[] roles = s.split("/");
        for (String role : roles){
            String[] parts = role.split(":");
            int amnt = Integer.parseInt(parts[0]);
            String roleName = parts[1].trim();
            raid.getRolesWithNumbers().add(new RaidRole(amnt, roleName));
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the roles for raid run (format: [amount]:[Role name]). Type done to finish entering roles."
                + "To enter multiple at once, make sure to use / between the roles for ex: 1:Chrono/1:Druid";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return null;
    }
}
