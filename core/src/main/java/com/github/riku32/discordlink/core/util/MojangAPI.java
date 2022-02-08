package com.github.riku32.discordlink.core.util;

import com.github.riku32.discordlink.core.util.skinrenderer.RenderConfiguration;
import com.github.riku32.discordlink.core.util.skinrenderer.RenderType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
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
     */
    public CompletableFuture<String> getName(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://api.mojang.com/user/profiles/%s/names", uuid)))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JSONArray names = new JSONArray(body);
                    return names.getJSONObject(names.length() - 1).getString("name");
                });
    }

    public CompletableFuture<RenderConfiguration> getRenderConfiguration(UUID uuid, RenderType renderType) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", uuid)))
                .build();

        AtomicBoolean slim = new AtomicBoolean(false);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> new String(Base64.getDecoder().decode(new JSONObject(body)
                        .getJSONArray("properties")
                        .getJSONObject(0)
                        .getString("value"))))
                .thenApply(skinString -> {
                    JSONObject skinData = new JSONObject(skinString)
                            .getJSONObject("textures")
                            .getJSONObject("SKIN");

                    try {
                        slim.set(skinData.getJSONObject("metadata")
                                .getBoolean("model"));
                    } catch (Exception ignored) {}

                    return skinData.getString("url");
                }).thenCompose(url -> httpClient.sendAsync(HttpRequest.newBuilder()
                            .uri(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray()))
                .thenApply(res -> {
                    try {
                        return new RenderConfiguration(
                                    renderType,
                                    ImageIO.read(new ByteArrayInputStream(res.body())),
                                    slim.get());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}