package me.cbitler.raidbot;

import java.sql.ResultSet;
import me.cbitler.raidbot.commands.CommandRegistry;
import me.cbitler.raidbot.commands.EndRaidCommand;
import me.cbitler.raidbot.commands.HelpCommand;
import me.cbitler.raidbot.commands.InfoCommand;
import me.cbitler.raidbot.creation.CreationStep;
import me.cbitler.raidbot.database.Database;
import me.cbitler.raidbot.database.QueryResult;
import me.cbitler.raidbot.handlers.ChannelMessageHandler;
import me.cbitler.raidbot.handlers.DMHandler;
import me.cbitler.raidbot.handlers.ReactionHandler;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.selection.SelectionStep;
import me.cbitler.raidbot.utility.Values;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing the raid bot itself.
 * This stores the creation/roleSelection map data and also the list of pendingRaids
 * Additionally, it also stores the database in use by the bot and serves as a way
 * for other classes to access it.
 *
 * @author Christopher Bitler
 */
public class RaidBot {
    private static RaidBot instance;
    private JDA jda;

    HashMap<String, CreationStep> creation = new HashMap<String, CreationStep>();
    HashMap<String, PendingRaid> pendingRaids = new HashMap<String, PendingRaid>();
    HashMap<String, SelectionStep> roleSelection = new HashMap<String, SelectionStep>();

    //TODO: This should be moved to it's own settings thing
    HashMap<String, String> raidLeaderRoleCache = new HashMap<>();
    HashMap<String, String> raidBotChannelCache = new HashMap<>();

    Database db;

    /**
     * Create a new instance of the raid bot with the specified JDA api
     * @param jda The API for the bot to use
     */
    public RaidBot(JDA jda) {
        instance = this;

        this.jda = jda;
        jda.addEventListener(new DMHandler(this), new ChannelMessageHandler(), new ReactionHandler());
        db = new Database("raid.db");
        db.connect();
        RaidManager.loadRaids();
        this.loadCaches();

        CommandRegistry.addCommand("help", new HelpCommand());
        CommandRegistry.addCommand("info", new InfoCommand());
        CommandRegistry.addCommand("endRaid", new EndRaidCommand());

    }
    
    private void loadCaches(){
        QueryResult caches;
        try {
            caches = db.query("SELECT * FROM `serverSettings`", new String[] {});
            
            while(caches.getResults().next()) {
                String serverId = caches.getResults().getString("serverId");
                String leader = caches.getResults().getString("raid_leader_role");
                String channel = caches.getResults().getString("raid_bot_channel");
                raidLeaderRoleCache.put(serverId, leader);
                raidBotChannelCache.put(serverId, channel);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RaidBot.class.getName()).log(Level.SEVERE, null, ex);
        }

            
    }

    /**
     * Map of UserId -> creation step for people in the creation process
     * @return The map of UserId -> creation step for people in the creation process
     */
    public HashMap<String, CreationStep> getCreationMap() {
        return creation;
    }

    /**
     * Map of the UserId -> roleSelection step for people in the role selection process
     * @return The map of the UserId -> roleSelection step for people in the role selection process
     */
    public HashMap<String, SelectionStep> getRoleSelectionMap() {
        return roleSelection;
    }

    /**
     * Map of the UserId -> pendingRaid step for raids in the setup process
     * @return The map of UserId -> pendingRaid
     */
    public HashMap<String, PendingRaid> getPendingRaids() {
        return pendingRaids;
    }

    /**
     * Get the JDA server object related to the server ID
     * @param id The server ID
     * @return The server related to that that ID
     */
    public Guild getServer(String id) {
        return jda.getGuildById(id);
    }

    /**
     * Exposes the underlying library. This is mainly necessary for getting Emojis
     * @return The JDA library object
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * Get the database that the bot is using
     * @return The database that the bot is using
     */
    public Database getDatabase() {
        return db;
    }

    /**
     * Get the raid leader role for a specific server.
     * This works by caching the role once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the role that is considered the raid leader for that server
     */
    public String getRaidLeaderRole(String serverId) {
        if (raidLeaderRoleCache.get(serverId) != null) {
            return raidLeaderRoleCache.get(serverId);
        } else {
            try {
                QueryResult results = db.query("SELECT `raid_leader_role` FROM `serverSettings` WHERE `serverId` = ?",
                        new String[]{serverId});
                if (results.getResults().next()) {
                    raidLeaderRoleCache.put(serverId, results.getResults().getString(Values.DEFAULTRAIDLEADER));
                    return raidLeaderRoleCache.get(serverId);
                } else {
                    return Values.DEFAULTRAIDLEADER;
                }
            } catch (Exception e) {
                return Values.DEFAULTRAIDLEADER;
            }
        }
    }

    /**
     * Set the raid leader role for a server. This also updates it in SQLite
     * @param serverId The server ID
     * @param role The role name
     */
    public void setRaidLeaderRole(String serverId, String role) {
        raidLeaderRoleCache.put(serverId, role);
        try {
            db.update("INSERT INTO `serverSettings` (`serverId`,`raid_leader_role`, `raid_bot_channel`) VALUES (?,?,?)",
                    new String[] { serverId, role, Values.DEFAULTRAIDCHANNEL});
        } catch (SQLException e) {
            //TODO: There is probably a much better way of doing this
            try {
                db.update("UPDATE `serverSettings` SET `raid_leader_role` = ? WHERE `serverId` = ?",
                        new String[] { role, serverId });
            } catch (SQLException e1) {
                // Not much we can do if there is also an insert error
            }
        }
    }

    /**
     * Get the raid leader role for a specific server.
     * This works by caching the role once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the role that is considered the raid leader for that server
     */
    public String getRaidBotChannel(String serverId) {
        if (raidBotChannelCache.get(serverId) != null) {
            return raidBotChannelCache.get(serverId);
        } else {
            try {
                QueryResult results = db.query("SELECT `raid_bot_channel` FROM `serverSettings` WHERE `serverId` = ?",
                        new String[]{serverId});
                if (results.getResults().next()) {
                    raidBotChannelCache.put(serverId, results.getResults().getString(Values.DEFAULTRAIDCHANNEL));
                    return raidBotChannelCache.get(serverId);
                } else {
                    return Values.DEFAULTRAIDCHANNEL;
                }
            } catch (Exception e) {
                return Values.DEFAULTRAIDCHANNEL;
            }
        }
    }

    /**
     * Set the raid channel for a server. If the channel is 'null' a channel has to be chosen. This also updates it in SQLite
     * @param serverId The server ID
     * @param channel The channel name
     */
    public void setRaidBotChannel(String serverId, String channel) {
        String channelWithoutHash = channel.replace("#","");
        boolean validChannel = false;
        RaidBot bot = RaidBot.getInstance();
        for (TextChannel c : bot.getServer(serverId).getTextChannels()) {
            if(c.getName().replace("#","").equalsIgnoreCase(channelWithoutHash)) {
                validChannel = true;
            }
        }

        if(!validChannel) {
            throw new IllegalArgumentException("Please enter an existing channel or check the permissions of the bot");
        } else {
            raidBotChannelCache.put(serverId, channel);
            try {
                db.update("INSERT INTO `serverSettings` (`serverId`, `raid_leader_role`, `raid_bot_channel`) VALUES (?,?,?)",
                        new String[] { serverId, Values.DEFAULTRAIDLEADER, channel});
            } catch (SQLException e) {
                //TODO: There is probably a much better way of doing this
                try {
                    db.update("UPDATE `serverSettings` SET `raid_bot_channel` = ? WHERE `serverId` = ?",
                            new String[] { channel, serverId });
                } catch (SQLException e1) {
                    // Not much we can do if there is also an insert error
                }
            }
                }


    }

    /**
     * Get the current instance of the bot
     * @return The current instance of the bot.
     */
    public static RaidBot getInstance() {
        return instance;
    }

}
