package com.github.riku32.discordlink.spigot.events.chatevent;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.spigot.SpigotPlayer;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class RecipientIterator<E extends PlatformPlayer> implements Iterator<E> {
    private final Iterator<Player> playerIterator;

    public RecipientIterator(Iterator<Player> playerIterator) {
        this.playerIterator = playerIterator;
    }

    @Override
    public boolean hasNext() {
        return playerIterator.hasNext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
        return (E) new SpigotPlayer(playerIterator.next());
    }
}
