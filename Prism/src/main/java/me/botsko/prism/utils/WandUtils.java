package me.botsko.prism.utils;

import me.botsko.prism.Prism;
import me.botsko.prism.wands.Wand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class WandUtils {

    /**
     * True if uses wand on click.
     * @param player Player
     * @param loc Location.
     * @return boolean
     */
    public static boolean playerUsesWandOnClick(Player player, Location loc) {

        if (Prism.playersWithActiveTools.containsKey(player.getName())) {

            final Wand wand = Prism.playersWithActiveTools.get(player.getName());

            if (wand == null) {
                return false;
            }

            final Material item = wand.getItem();
            if (player.getInventory().getItemInMainHand().getType() == item) {
                // Left click is for current location
                wand.playerLeftClick(player, loc);
                return true;
            }
        }

        return false;

    }

}