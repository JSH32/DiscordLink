package com.github.riku32.discordlink.spigot.events;

import com.github.riku32.discordlink.core.platform.PlatformPlayer;
import com.github.riku32.discordlink.core.eventbus.events.PlayerChatEvent;
import com.github.riku32.discordlink.spigot.SpigotPlayer;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class SpigotChatEvent extends PlayerChatEvent {
    private static class RecipientSet<E extends PlatformPlayer> implements Set<E> {
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

        public @NotNull Iterator<E> iterator() {
            return new RecipientIterator<>(playerSet.iterator());
        }

        public Object @NotNull [] toArray() {
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
                    .map(p -> (((PlatformPlayer) p).getPlatformPlayer()))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private static class RecipientIterator<E extends PlatformPlayer> implements Iterator<E> {
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

    private final AsyncPlayerChatEvent chatEvent;
    private final PlatformPlayer player;

    public SpigotChatEvent(AsyncPlayerChatEvent chatEvent, PlatformPlayer player) {
        this.chatEvent = chatEvent;
        this.player = player;
    }

    @Override
    public PlatformPlayer getPlayer() {
        return player;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
        chatEvent.setCancelled(true);
    }

    @Override
    public String getMessage() {
        return chatEvent.getMessage();
    }

    @Override
    public void setMessage(String message) {
        chatEvent.setMessage(message);
    }

    @Override
    public void setFormat(String format) {
        chatEvent.setFormat(format);
    }

    @Override
    public Set<PlatformPlayer> getRecipients() {
        return new RecipientSet<>(chatEvent.getRecipients());
    }
}