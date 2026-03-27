package com.axiom.common.research;

import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * Describes a named tier of a machine or infrastructure item.
 * Tiers are unlocked through research and represent physical item replacements
 * (e.g. upgrading a Standard Belt to a Fast Belt), never invisible stat multipliers.
 *
 * <p>JSON format (in {@code data/<namespace>/axiom_tiers/<name>.json}):
 * <pre>{@code
 * {
 *   "display_name": "Fast Belt",
 *   "tier": 2,
 *   "category": "belt",
 *   "unlocked_by": "axiom:fast_transport"
 * }
 * }</pre>
 */
public final class MachineTierDefinition {

    private final ResourceLocation id;
    private final String displayName;
    private final int tier;
    private final String category;
    private final ResourceLocation unlockedByResearch;

    public MachineTierDefinition(
            ResourceLocation id,
            String displayName,
            int tier,
            String category,
            ResourceLocation unlockedByResearch) {
        this.id = id;
        this.displayName = displayName;
        this.tier = tier;
        this.category = category;
        this.unlockedByResearch = unlockedByResearch;
    }

    public static MachineTierDefinition fromJson(ResourceLocation id, JsonObject json) {
        String displayName = JSONUtils.getAsString(json, "display_name", id.getPath());
        int tier = JSONUtils.getAsInt(json, "tier", 1);
        String category = JSONUtils.getAsString(json, "category", "generic");
        ResourceLocation unlockedByResearch = new ResourceLocation(JSONUtils.getAsString(json, "unlocked_by"));
        return new MachineTierDefinition(id, displayName, tier, category, unlockedByResearch);
    }

    public ResourceLocation getId() { return this.id; }
    public String getDisplayName() { return this.displayName; }
    public int getTier() { return this.tier; }
    public String getCategory() { return this.category; }
    public ResourceLocation getUnlockedByResearch() { return this.unlockedByResearch; }
}
