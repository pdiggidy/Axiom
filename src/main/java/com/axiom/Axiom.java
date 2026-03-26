package com.axiom;

import net.minecraftforge.common.MinecraftForge;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Axiom mod initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Axiom common setup complete.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Axiom client setup complete.");
    }

    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("Axiom server starting.");
    }
}
