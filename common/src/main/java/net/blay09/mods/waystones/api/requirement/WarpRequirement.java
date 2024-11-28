package net.blay09.mods.waystones.api.requirement;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface WarpRequirement {
    boolean canAfford(Player player);

    void consume(Player player);

    void rollback(Player player);


    void appendHoverText(Player player, List<Component> tooltip);

    default boolean isEmpty() {
        return false;
    }
}
