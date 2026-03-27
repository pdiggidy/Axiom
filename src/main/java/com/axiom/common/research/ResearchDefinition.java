package com.axiom.common.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable definition of a single research node loaded from JSON.
 *
 * <p>JSON format (in {@code data/<namespace>/axiom_research/<name>.json}):
 * <pre>{@code
 * {
 *   "display_name": "Basic Automation",
 *   "prerequisites": ["axiom:surveying_basics"],
 *   "unlocks": [
 *     { "type": "machine", "target": "axiom:basic_machine" }
 *   ],
 *   "catalysts": ["axiom:research_catalyst_iron"]
 * }
 * }</pre>
 *
 * <p>Catalysts are optional acceleration items, never mandatory prerequisites.</p>
 */
public final class ResearchDefinition {

    private final ResourceLocation id;
    private final String displayName;
    private final List<ResourceLocation> prerequisites;
    private final List<UnlockEntry> unlocks;
    private final List<ResourceLocation> catalysts;

    public ResearchDefinition(
            ResourceLocation id,
            String displayName,
            List<ResourceLocation> prerequisites,
            List<UnlockEntry> unlocks,
            List<ResourceLocation> catalysts) {
        this.id = id;
        this.displayName = displayName;
        this.prerequisites = Collections.unmodifiableList(new ArrayList<>(prerequisites));
        this.unlocks = Collections.unmodifiableList(new ArrayList<>(unlocks));
        this.catalysts = Collections.unmodifiableList(new ArrayList<>(catalysts));
    }

    public static ResearchDefinition fromJson(ResourceLocation id, JsonObject json) {
        String displayName = JSONUtils.getAsString(json, "display_name", id.getPath());

        List<ResourceLocation> prerequisites = new ArrayList<>();
        if (json.has("prerequisites")) {
            JsonArray prereqArr = JSONUtils.getAsJsonArray(json, "prerequisites");
            for (int i = 0; i < prereqArr.size(); i++) {
                prerequisites.add(new ResourceLocation(prereqArr.get(i).getAsString()));
            }
        }

        List<UnlockEntry> unlocks = new ArrayList<>();
        if (json.has("unlocks")) {
            JsonArray unlockArr = JSONUtils.getAsJsonArray(json, "unlocks");
            for (int i = 0; i < unlockArr.size(); i++) {
                unlocks.add(parseUnlock(id, unlockArr.get(i).getAsJsonObject()));
            }
        }

        List<ResourceLocation> catalysts = new ArrayList<>();
        if (json.has("catalysts")) {
            JsonArray catArr = JSONUtils.getAsJsonArray(json, "catalysts");
            for (int i = 0; i < catArr.size(); i++) {
                catalysts.add(new ResourceLocation(catArr.get(i).getAsString()));
            }
        }

        return new ResearchDefinition(id, displayName, prerequisites, unlocks, catalysts);
    }

    private static UnlockEntry parseUnlock(ResourceLocation defId, JsonObject obj) {
        String typeStr = JSONUtils.getAsString(obj, "type").toUpperCase();
        UnlockEntry.UnlockType type;
        try {
            type = UnlockEntry.UnlockType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown unlock type '" + typeStr + "' in research " + defId);
        }
        ResourceLocation target = new ResourceLocation(JSONUtils.getAsString(obj, "target"));
        return new UnlockEntry(type, target);
    }

    public ResourceLocation getId() { return this.id; }
    public String getDisplayName() { return this.displayName; }
    public List<ResourceLocation> getPrerequisites() { return this.prerequisites; }
    public List<UnlockEntry> getUnlocks() { return this.unlocks; }
    public List<ResourceLocation> getCatalysts() { return this.catalysts; }
}
