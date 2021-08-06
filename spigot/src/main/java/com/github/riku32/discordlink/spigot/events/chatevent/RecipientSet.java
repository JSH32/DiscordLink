package com.github.riku32.discordlink.spigot.events.chatevent;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.spigot.SpigotPlayer;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipientSet<E extends PlatformPlayer> implements Set<E> {
    private final Set<Player> playerSet;

    public RecipientSet(Set<Player> playerSet) {
        this.playerSet = playerSet;
    }

    public int size() {
        return playerSet.size();
    }

    public boolean isEmpty() {
        return playerSet.isEmpty();
    }

    public boolean contains(Object o) {
        if (o instanceof PlatformPlayer)
            return playerSet.contains(((PlatformPlayer) o).getPlatformPlayer());

        return false;
    }

    public Iterator<E> iterator() {
        return new RecipientIterator<>(playerSet.iterator());
    }

    public Object[] toArray() {
        return playerSet.stream().map(SpigotPlayer::new).collect(Collectors.toUnmodifiableSet()).toArray();
    }

    @NotNull
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        throw new NotImplementedException();
    }

    public boolean add(E o) {
        return playerSet.add((Player) o.getPlatformPlayer());
    }

    public boolean remove(Object o) {
        if (o instanceof PlatformPlayer)
            return playerSet.remove(((PlatformPlayer) o).getPlatformPlayer());

        return false;
    }

    public boolean addAll(@NotNull Collection c) {
        return playerSet.addAll(playerCollectionFromPlatform(c));
    }

    public void clear() {
        playerSet.clear();
    }

    public boolean removeAll(@NotNull Collection c) {
        return playerSet.removeAll(playerCollectionFromPlatform(c));
    }

    public boolean retainAll(@NotNull Collection c) {
        return playerSet.retainAll(playerCollectionFromPlatform(c));
    }

    public boolean containsAll(@NotNull Collection c) {
        return playerSet.containsAll(playerCollectionFromPlatform(c));
    }


    @SuppressWarnings("unchecked")
    private Collection<? extends Player> playerCollectionFromPlatform(@NotNull Collection collection) {
        return (Collection<? extends Player>) collection.stream()
                .filter(PlatformPlayer.class::isInstance)
                .map(p -> (((PlatformPlayer) p).getPlatformPlayer()))
                .collect(Collectors.toUnmodifiableSet());
    }
}