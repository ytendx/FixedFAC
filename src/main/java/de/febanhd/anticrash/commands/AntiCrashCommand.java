package de.febanhd.anticrash.commands;

import com.comphenix.protocol.PacketType;
import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.impl.nbt.NBTTagCheck;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiCrashCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        Player player =  ((Player) sender);
        player.sendTitle("§4§lFAC", "§c§lMade by FebanHD <3");

        sender.sendMessage(AntiCrash.PREFIX + "§cFixedFAC §7by §eFebanHD §7fixed by §eytendx §8| §7AntiCrash & AntiDoS");
        sender.sendMessage(AntiCrash.PREFIX + "§cVersion: §7" + AntiCrashPlugin.getPlugin().getDescription().getVersion());
        sender.sendMessage(AntiCrash.PREFIX + "§cChecks: §7" + AntiCrash.getInstance().getChecks().size());
        sender.sendMessage(AntiCrash.PREFIX + "§cNBTChecks: §7" + ((NBTTagCheck)AntiCrash.getInstance().getCheck(NBTTagCheck.class)).getChecks().size());

        return false;
    }
}
