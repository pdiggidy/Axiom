package com.axiom.common.network;

import com.axiom.common.util.AxiomIds;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Central registration point for all Axiom network packets.
 * Call {@link #registerPackets()} once during common setup.
 *
 * <p>Packet index assignments are stable — do not reorder registrations
 * once clients and servers are in use, as indices are part of the protocol.</p>
 */
public final class AxiomNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            AxiomIds.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextPacketId = 0;

    public static void registerPackets() {
        // client→server: player changes a face's IO mode from the machine GUI
        CHANNEL.registerMessage(
                nextPacketId++,
                SideConfigPacket.class,
                SideConfigPacket::encode,
                SideConfigPacket::decode,
                SideConfigPacket::handle
        );
    }

    private AxiomNetwork() {}
}
