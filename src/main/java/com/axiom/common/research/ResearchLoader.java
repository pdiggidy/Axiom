package com.axiom.common.research;

import com.axiom.Axiom;
import com.axiom.common.util.SimpleJsonReloadListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads research definitions from {@code data/<namespace>/axiom_research/*.json} on
 * resource-pack reload. Register via {@link net.minecraftforge.event.AddReloadListenerEvent}.
 *
 * <p>Validation rules enforced at load time:
 * <ul>
 *   <li>Any unlock entry with type {@code STAT_MODIFIER} causes a hard error and is rejected.
 *   <li>Invalid JSON or unknown fields produce actionable error logs but do not crash the game.
 * </ul>
 */
public final class ResearchLoader extends SimpleJsonReloadListener {

    public static final ResearchLoader INSTANCE = new ResearchLoader();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private volatile Map<ResourceLocation, ResearchDefinition> definitions = Collections.emptyMap();

    private ResearchLoader() {
        super(GSON, "axiom_research");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager manager, IProfiler profiler) {
        Map<ResourceLocation, ResearchDefinition> loaded = new HashMap<>();
        int errors = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                ResearchDefinition def = ResearchDefinition.fromJson(id, entry.getValue().getAsJsonObject());
                validateDefinition(def);
                loaded.put(id, def);
            } catch (Exception e) {
                errors++;
                Axiom.LOGGER.error("[Axiom Research] Failed to load '{}': {}", id, e.getMessage());
            }
        }

        this.definitions = Collections.unmodifiableMap(loaded);
        Axiom.LOGGER.info("[Axiom Research] Loaded {} definition(s), {} error(s).", loaded.size(), errors);
    }

    /**
     * Rejects any definition that attempts to encode stat modifiers.
     * All efficiency gains must come from physical world upgrades, not invisible research buffs.
     */
    private static void validateDefinition(ResearchDefinition def) {
        for (UnlockEntry unlock : def.getUnlocks()) {
            if (unlock.getType() == UnlockEntry.UnlockType.STAT_MODIFIER) {
                throw new IllegalArgumentException(
                        "Research '" + def.getId() + "' contains a STAT_MODIFIER unlock entry for '"
                                + unlock.getTarget() + "'. Research must only unlock capabilities, "
                                + "not invisibly modify machine stats. Use a physical tier upgrade instead.");
            }
        }
    }

    public Map<ResourceLocation, ResearchDefinition> getDefinitions() {
        return this.definitions;
    }

    public Optional<ResearchDefinition> getDefinition(ResourceLocation id) {
        return Optional.ofNullable(this.definitions.get(id));
    }
}
