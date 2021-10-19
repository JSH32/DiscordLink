package com.github.riku32.discordlink.core.listeners;

import com.github.riku32.discordlink.core.framework.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.framework.eventbus.events.PlayerMoveEvent;
import com.github.riku32.discordlink.core.framework.PlatformPlayer;

import java.util.Set;

/**
 * Ensure all frozen players do not move
 */
public class MoveListener {
    private final Set<PlatformPlayer> frozenPlayers;

    public MoveListener(Set<PlatformPlayer> frozenPlayers) {
        this.frozenPlayers = frozenPlayers;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer()))
            event.setCancelled(true);
    }
}
