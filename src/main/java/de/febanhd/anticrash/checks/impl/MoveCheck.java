package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Maps;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.concurrent.ConcurrentHashMap;

public class MoveCheck extends AbstractCheck implements Listener {

    private final ConcurrentHashMap<Player, Location> lastLocations;
    private ConcurrentHashMap<Player, Integer> packetsSent;

    public MoveCheck() {
        super("MoveCheck", PacketType.Play.Client.POSITION);
        this.lastLocations = new ConcurrentHashMap<>();
        this.packetsSent = new ConcurrentHashMap<>();
        Bukkit.getScheduler().runTaskTimerAsynchronously(AntiCrashPlugin.getPlugin(), () -> packetsSent.clear(), 0, 20);
    }

    // Fix - ytendx
    @Override
    public void onPacketReceiving(PacketEvent event) {
        if(event.getPacket().getType().equals(PacketType.Play.Client.POSITION)){
            double x = event.getPacket().getDoubles().readSafely(0);
            double y = event.getPacket().getDoubles().readSafely(1);
            double z = event.getPacket().getDoubles().readSafely(2);

            if(x >= Double.MAX_VALUE || y >= Double.MAX_VALUE || z >= Double.MAX_VALUE){
                this.sendCrashWarning(event.getPlayer(), event, "Position packet with max_val attributes");
                event.setCancelled(true);
            }

            if(packetsSent.containsKey(event.getPlayer())){
                if(packetsSent.get(event.getPlayer()) >= 30){
                    if(packetsSent.get(event.getPlayer()) == 30)
                    this.sendCrashWarning(event.getPlayer(), event, "Player sent too much position packets in one sec (>50)");
                    event.setCancelled(true);
                }
                packetsSent.replace(event.getPlayer(), packetsSent.get(event.getPlayer())+1);
            }else packetsSent.put(event.getPlayer(), 1);

            final Location location = new Location(event.getPlayer().getWorld(), x, y, z);
            int maxDistance = ConfigCache.getInstance().getValue("moveCheck.positionPacketDistance", 999, Integer.class);

            if(lastLocations.containsKey(event.getPlayer())){

                if(lastLocations.get(event.getPlayer()).distance(location) > maxDistance){
                    this.sendCrashWarning(event.getPlayer(), event, "Position packet with too high distance");
                    event.setCancelled(true);
                }

                double yDelta = Math.abs(y - lastLocations.get(event.getPlayer()).getY());

                if(yDelta == 9.0D){
                    this.sendCrashWarning(event.getPlayer(), event, "Position packet has suspicious y delta val");
                    event.setCancelled(true);
                }

            }else lastLocations.put(event.getPlayer(), new Location(event.getPlayer().getWorld(), x, y, z));
        }
        super.onPacketReceiving(event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        /*
         * This check is from Illigalspigot (Wich was shit cause the Server crashed cause of a move crash lol)
         */

        double distance = event.getFrom().distance(event.getTo());
        int maxDistance = ConfigCache.getInstance().getValue("moveCheck.flagDistance", 5, Integer.class);
        if(distance > maxDistance) {
            this.sendCrashWarning(event.getPlayer(), "Player moved to far in one tick (> " + maxDistance + ")");
            event.setCancelled(true); // Fix - ytendx
        }

        // Fix - ytendx
        if(event.getTo().getChunk() == null || !event.getTo().getChunk().isLoaded()){
            event.setCancelled(true);
        }
    }
}
