package com.github.riku32.discordlink.core.events;

import com.github.riku32.discordlink.core.eventbus.annotation.EventHandler;
import com.github.riku32.discordlink.core.eventbus.events.PlayerMoveEvent;
import com.github.riku32.discordlink.core.platform.PlatformPlayer;

import java.util.Set;

public class MoveEvent {
    private final Set<PlatformPlayer> frozenPlayers;

    public MoveEvent(Set<PlatformPlayer> frozenPlayers) {
        this.frozenPlayers = frozenPlayers;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer()))
            event.setCancelled(true);
    }
}
