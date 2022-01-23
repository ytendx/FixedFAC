package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.concurrent.ConcurrentHashMap;

public class ChatMessageCheck extends AbstractCheck implements Listener {

    private ConcurrentHashMap<Player, Long> lastMessage;

    public ChatMessageCheck() {
        super("Chat Message Check");
        lastMessage = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, AntiCrashPlugin.getPlugin());
    }

    @EventHandler
    public void handleChat(AsyncPlayerChatEvent event){
        if(lastMessage.containsKey(event.getPlayer())){
            if(System.currentTimeMillis() - lastMessage.get(event.getPlayer()) < 100){
                this.sendInvalidPacketWarning(event.getPlayer(), "Sent messages too fast (<100ms)");
                event.setCancelled(true);
            }
            lastMessage.replace(event.getPlayer(), System.currentTimeMillis());
        }else lastMessage.put(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void handlePreCommand(PlayerCommandPreprocessEvent event){
        if(event.getMessage().contains("/calc") || event.getMessage().contains("/solve") || event.getMessage().contains("/eval") || event.getMessage().contains("/desc")){
            if(event.getMessage().contains("(")
                    || event.getMessage().contains(")")
                    || event.getMessage().contains("[")
                    || event.getMessage().contains("]")
                    || event.getMessage().contains("{")
                    || event.getMessage().contains("}")
                    || event.getMessage().contains("?")
                    || event.getMessage().contains(":")
                    || event.getMessage().contains(";")){
                this.sendInvalidPacketWarning(event.getPlayer(), "The player executed a dangerous WorldEdit Crash Command. (Calculation)");
                event.setCancelled(true);
            }else if(event.getMessage().length() > 20){
                this.sendInvalidPacketWarning(event.getPlayer(), "The player executed a dangerous WorldEdit Crash Command. (Calculation)");
                event.setCancelled(true);
            }
        }

        if(event.getMessage().contains("mv") && (event.getMessage().contains("\n") || event.getMessage().contains(".*.*")) || event.getMessage().contains(String.valueOf((char) 775))){
            this.sendInvalidPacketWarning(event.getPlayer(), "The player executed a Crash Command. (MV Crasher / Bad Char)");
            event.setCancelled(true);
        }

        if(event.getMessage().contains("mv") && (event.getMessage().contains("/") || event.getMessage().contains("\\"))){
            this.sendInvalidPacketWarning(event.getPlayer(), "The player executed a dangerous Multiverse Command. (Path Navigation)");
            event.setCancelled(true);
        }

        if((event.getMessage().contains("pex promote a a") || event.getMessage().contains("pex demote a a")) && event.getMessage().startsWith("/")){
            this.sendInvalidPacketWarning(event.getPlayer(), "The player executed a permission ex crash command. (Promote/Demote)");
            event.setCancelled(true);
        }
    }
}
