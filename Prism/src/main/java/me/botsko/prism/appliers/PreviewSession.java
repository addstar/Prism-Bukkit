package me.botsko.prism.appliers;

import org.bukkit.entity.Player;

public class PreviewSession {

    protected final Player player;
    protected final Previewable previewer;
    protected final long queryTime;

    /**
     * Create a preview session.
     *
     * @param player    the player
     * @param previewer the the Preview - able group.
     */
    public PreviewSession(Player player, Previewable previewer) {
        this.player = player;
        this.previewer = previewer;
        final java.util.Date date = new java.util.Date();
        this.queryTime = date.getTime();
    }

    /**
     * Get the player.
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the preview items.
     * @return the previewer
     */
    public Previewable getPreviewer() {
        return previewer;
    }

    /**
     * Get the time for the query.
     * @return the queryTime
     */
    public long getQueryTime() {
        return queryTime;
    }
}