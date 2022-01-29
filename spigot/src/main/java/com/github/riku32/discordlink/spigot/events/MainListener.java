package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.framework.eventbus.EventBus;
import com.github.riku32.discordlink.core.framework.eventbus.exceptions.EventPostException;
import com.github.riku32.discordlink.spigot.PlayerRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MainListener implements Listener {
    private final EventBus eventBus;
    private final PlayerRegistry playerRegistry;

    public MainListener(EventBus eventBus, PlayerRegistry playerRegistry) {
        this.eventBus = eventBus;
        this.playerRegistry = playerRegistry;
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent chatEvent) throws EventPostException {
        eventBus.post(new SpigotChatEvent(chatEvent, playerRegistry.getPlayer(chatEvent.getPlayer())));
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent deathEvent) throws EventPostException {
        eventBus.post(new SpigotDeathEvent(deathEvent, playerRegistry.getPlayer(deathEvent.getEntity())));
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent joinEvent) throws EventPostException {
        eventBus.post(new SpigotJoinEvent(joinEvent, playerRegistry.getPlayer(joinEvent.getPlayer())));
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent moveEvent) throws EventPostException {
        eventBus.post(new SpigotMoveEvent(moveEvent, playerRegistry.getPlayer(moveEvent.getPlayer())));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent quitEvent) throws EventPostException {
        eventBus.post(new SpigotQuitEvent(quitEvent, playerRegistry.getPlayer(quitEvent.getPlayer())));
    }
}