package com.axiom.common.registry;

import com.axiom.Axiom;
import com.axiom.common.menu.BasicMachineContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, Axiom.MOD_ID);

    // Container registration is split out so GUI-backed machines can scale independently of block registration.
    public static final RegistryObject<ContainerType<BasicMachineContainer>> BASIC_MACHINE = CONTAINERS.register(
            "basic_machine",
            () -> IForgeContainerType.create(BasicMachineContainer::new)
    );

    private ModContainers() {
    }
}
