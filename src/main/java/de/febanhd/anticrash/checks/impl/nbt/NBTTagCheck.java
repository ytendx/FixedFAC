package de.febanhd.anticrash.checks.impl.nbt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.collect.Lists;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.checks.CheckResult;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class NBTTagCheck extends AbstractCheck {

    private ArrayList<INBTCheck> checks = Lists.newArrayList();

    public NBTTagCheck() {
        super("NBTTagCheck", PacketType.Play.Client.BLOCK_PLACE, PacketType.Play.Client.SET_CREATIVE_SLOT);
        this.checks.add(new MobSpawnerNBTCheck());
        this.checks.add(new SkullNBTCheck());
        this.checks.add(new MapNBTCheck());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();

        if(event.getPacket().getType().equals(PacketType.Play.Client.SET_CREATIVE_SLOT)
        && player.getGameMode() != GameMode.CREATIVE){
            this.sendInvalidPacketWarning(player, event, "Sent creative slot packet without being in creative mode");
            event.setCancelled(true);
        }

        PacketContainer packet = event.getPacket();
        ItemStack stack = packet.getItemModifier().readSafely(0);
        if(stack == null) return;
        Material itemType = stack.getType();
        if(itemType == Material.AIR) return;
        NbtCompound tag = (NbtCompound) NbtFactory.fromItemTag(stack);
        if(tag != null) {
            this.checks.forEach(check -> {
                if(check.material().contains(itemType)) {
                    CheckResult result = check.isValid(tag);
                    if(!result.check()) {
                        player.getInventory().clear();
                        this.sendInvalidPacketWarning(player, event, "Invalid NBT-Tag: " + result.getReason() + "!");
                        event.setCancelled(true);
                    }
                }
            });
        }
    }

    public ArrayList<INBTCheck> getChecks() {
        return checks;
    }
}
