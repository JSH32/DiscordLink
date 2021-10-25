package com.github.riku32.discordlink.core.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Mojang API utility to access data from the Mojang API
 *
 * TODO: Stop using this, offline servers wont work, store in a DB cache instead
 */
public class MojangAPI {
    // HTTP client used by the Mojang API instance
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Since the player may be offline and does not always have an offline player cache.
     * This asynchronously queries the Mojang API for the players name
     *
     * @param uuid of the player to query
     * @param then a callback that accepts their name
     */
    public void getName(UUID uuid, Consumer<String> then, Runnable error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://api.mojang.com/user/profiles/%s/names", uuid)))
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    if (body.isEmpty()) {
                        if (error != null) error.run();
                        return;
                    }

                    JSONArray names = new JSONArray(body);
                    then.accept(names.getJSONObject(names.length() - 1).getString("name"));
                })
                .join();
    }
}