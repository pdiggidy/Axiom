package com.axiom.common.util;

import com.axiom.Axiom;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Common-code equivalent of the client-only {@code JsonReloadListener}, safe to use on
 * dedicated servers. Loads all {@code .json} files from a given data-pack subfolder and
 * calls {@link #apply} with the parsed results on reload.
 */
public abstract class SimpleJsonReloadListener implements IFutureReloadListener {

    private static final String JSON_SUFFIX = ".json";

    private final Gson gson;
    private final String folder;

    protected SimpleJsonReloadListener(Gson gson, String folder) {
        this.gson = gson;
        this.folder = folder;
    }

    @Override
    public CompletableFuture<Void> reload(
            IStage stage,
            IResourceManager manager,
            IProfiler prepareProfiler,
            IProfiler applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor) {

        CompletableFuture<Map<ResourceLocation, JsonElement>> prepareFuture =
                CompletableFuture.supplyAsync(() -> load(manager), prepareExecutor);

        return prepareFuture
                .thenCompose(stage::wait)
                .thenAcceptAsync(map -> apply(map, manager, applyProfiler), applyExecutor);
    }

    private Map<ResourceLocation, JsonElement> load(IResourceManager manager) {
        Map<ResourceLocation, JsonElement> results = new HashMap<>();
        int prefixLength = this.folder.length() + 1; // folder + "/"

        for (ResourceLocation file : manager.listResources(this.folder, name -> name.endsWith(JSON_SUFFIX))) {
            String path = file.getPath();
            ResourceLocation id = new ResourceLocation(
                    file.getNamespace(),
                    path.substring(prefixLength, path.length() - JSON_SUFFIX.length()));
            try (IResource resource = manager.getResource(file);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                JsonElement json = this.gson.fromJson(reader, JsonElement.class);
                if (json != null) {
                    results.put(id, json);
                }
            } catch (JsonParseException e) {
                Axiom.LOGGER.error("[Axiom] JSON parse error in {}: {}", file, e.getMessage());
            } catch (Exception e) {
                Axiom.LOGGER.error("[Axiom] Failed to read data file {}: {}", file, e.getMessage());
            }
        }
        return results;
    }

    protected abstract void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager manager, IProfiler profiler);
}
