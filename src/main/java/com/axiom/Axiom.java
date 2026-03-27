package com.axiom;

import com.axiom.common.network.AxiomNetwork;
import com.axiom.common.recipe.ModRecipes;
import com.axiom.common.registry.ModBlocks;
import com.axiom.common.registry.ModContainers;
import com.axiom.common.registry.ModItems;
import com.axiom.common.registry.ModTileEntities;
import com.axiom.common.research.ResearchLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Axiom.MOD_ID)
public class Axiom {

    public static final String MOD_ID = "axiom";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Axiom() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        context.getModEventBus().addListener(this::commonSetup);
        context.getModEventBus().addListener(this::clientSetup);

        // Core game objects
        ModBlocks.BLOCKS.register(context.getModEventBus());
        ModItems.ITEMS.register(context.getModEventBus());
        ModTileEntities.TILE_ENTITIES.register(context.getModEventBus());
        ModContainers.CONTAINERS.register(context.getModEventBus());

        // Recipe serializers (T02)
        ModRecipes.register();

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Axiom mod initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Network channel and packet registration (T01)
        AxiomNetwork.registerPackets();
        LOGGER.info("Axiom common setup complete.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Axiom client setup complete.");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("Axiom server starting.");
    }

    /** Register data-pack reload listeners so research definitions are hot-reloadable. */
    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(ResearchLoader.INSTANCE);
    }
}
