package com.axiom.common.network;

import com.axiom.common.tile.base.ISideConfig;
import com.axiom.common.tile.base.SideMode;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client→server packet: player requests a change to one face's {@link SideMode}.
 * The server validates position reachability before applying the change.
 */
public class SideConfigPacket {

    private final BlockPos pos;
    private final Direction side;
    private final SideMode mode;

    public SideConfigPacket(BlockPos pos, Direction side, SideMode mode) {
        this.pos = pos;
        this.side = side;
        this.mode = mode;
    }

    public static void encode(SideConfigPacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.side.get3DDataValue());
        buf.writeByte(packet.mode.ordinal());
    }

    public static SideConfigPacket decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        Direction side = Direction.from3DDataValue(buf.readByte());
        SideMode mode = SideMode.fromOrdinal(buf.readByte());
        return new SideConfigPacket(pos, side, mode);
    }

    public static void handle(SideConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            net.minecraft.entity.player.ServerPlayerEntity player = ctx.getSender();
            if (player == null) return;
            ServerWorld world = player.getLevel();
            // Basic reach check: 8-block radius to match container stillValid distance.
            if (player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > 64.0) {
                return;
            }
            TileEntity te = world.getBlockEntity(packet.pos);
            if (te instanceof ISideConfig) {
                ((ISideConfig) te).setSideMode(packet.side, packet.mode);
                // Trigger a block update so clients receive the new state via getUpdatePacket().
                world.sendBlockUpdated(packet.pos, te.getBlockState(), te.getBlockState(), 3);
            }
        });
        ctx.setPacketHandled(true);
    }
}
