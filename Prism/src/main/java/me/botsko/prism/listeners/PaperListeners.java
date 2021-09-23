package me.botsko.prism.listeners;

import io.papermc.paper.event.block.TargetHitEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.actions.ActionType;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

/**
 * This class carries listeners that will only work currently with paper.
 *
 * @author Narimm on 1/01/2021.
 */
public class PaperListeners implements Listener {

    final Prism plugin;

    public PaperListeners(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * React to a target hit event.
     * @param event the TargetHitEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTargetHitEvent(TargetHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (shooter instanceof Player) {
            if (!Prism.getIgnore().event(ActionType.TARGET_HIT, (Player) shooter)) {
                return;
            }
            Block block = event.getHitBlock();
            RecordingQueue.addToQueue(ActionFactory.createBlock(ActionType.TARGET_HIT,block,(Player) shooter));
        }
    }

    /**
     * TradeEvent - Paper Only.
     * @param event PlayerTradeEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTrade(PlayerTradeEvent event) {
        Player player = event.getPlayer();
        if (!Prism.getIgnore().event(ActionType.PLAYER_TRADE, player)) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createEntity(ActionType.PLAYER_TRADE,event.getVillager(),player));
        ItemStack result = event.getTrade().getResult();
        RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_RECEIVE,result,result.getAmount(),
                -1,result.getEnchantments(),event.getVillager().getLocation(),player));

    }

    /**
     * AsyncPlayerChatEvent.
     *
     * @param event AsyncPlayerChatEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(final AsyncChatEvent event) {

        if (!Prism.getIgnore().event(ActionType.PLAYER_CHAT, event.getPlayer())) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createPlayer(ActionType.PLAYER_CHAT, event.getPlayer(),
                PlainComponentSerializer.plain().serialize(event.message())));
    }

}
