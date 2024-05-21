package io.github.thecsdev.betterstats.network;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.NETWORK_VERSION;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.S2C_I_HAVE_BSS;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.tcdcommons.api.hooks.entity.EntityHooks;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetworkReceiver.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Better statistics screen server play network handler.<br/>
 * Keeps track of {@link ServerPlayerEntity} data that is related to {@link BetterStats}.
 */
public final @Internal class BetterStatsServerPlayNetworkHandler
{
	// ==================================================
	public static final Identifier CUSTOM_DATA_ID = new Identifier(getModID(), "server_play_network_handler");
	// ==================================================
	private final ServerPlayerEntity player;
	// --------------------------------------------------
	/**
	 * When set to {@code true}, this should never be switched back to {@code false}.<br/>
	 * Indicates whether or not the associated player has {@link BetterStats} installed.
	 */
	public boolean hasBssInstalled = false;
	// --------------------------------------------------
	/**
	 * When set to true, the {@link BetterStatsNetwork} will
	 * automatically update the client on their stats changes, live.
	 */
	public boolean enableLiveStats = false;
	
	/**
	 * The timestamp at which the last live stats update was performed.
	 * Used to avoid packet spam.
	 */
	public long liveStatsLastUpdate = 0;
	// ==================================================
	private BetterStatsServerPlayNetworkHandler(ServerPlayerEntity player) throws NullPointerException
	{
		this.player = Objects.requireNonNull(player);
	}
	// --------------------------------------------------
	public final ServerPlayerEntity getPlayer() { return this.player; }
	// ==================================================
	public final void onPlayerConnected() { sendIHaveBss(); }
	
	public final void onIHaveBss(PacketContext ctx)
	{
		//obtain data buffer and make sure data is present
		final var buffer = ctx.getPacketBuffer();
		if(buffer.readableBytes() == 0) return;
		
		//obtain network version and compare it
		final int netVer = buffer.readIntLE();
		if(netVer != NETWORK_VERSION) return;
		
		//update prefs
		this.hasBssInstalled = true;
	}
	
	public final void onLiveStatsSetting(PacketContext ctx)
	{
		//check if bss installed first, then update the setting
		if(!this.hasBssInstalled) return;
		this.enableLiveStats = ctx.getPacketBuffer().readBoolean();
	}
	// --------------------------------------------------
	public final void sendIHaveBss()
	{
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeIntLE(NETWORK_VERSION);
		CustomPayloadNetwork.sendS2C(this.player, S2C_I_HAVE_BSS, data);
	}
	
	public final boolean sendLiveStatsAttepmt() //attempts to send live stats, if possible
	{
		//check prefs
		if(!this.enableLiveStats) return false;
		
		//check last update time, and avoid packet spam
		final long currentTime = System.currentTimeMillis();
		if(currentTime - this.liveStatsLastUpdate < 300) return false;
		
		//update last time, and send stats
		this.liveStatsLastUpdate = currentTime;
		this.player.getStatHandler().sendStats(player);
		return true;
	}
	// ==================================================
	/**
	 * Returns an instance of {@link BetterStatsServerPlayNetworkHandler} for a given
	 * {@link ServerPlayerEntity}. Creates one if it doesn't exist yet.
	 * @param player The {@link ServerPlayerEntity}.
	 */
	public static final BetterStatsServerPlayNetworkHandler of(ServerPlayerEntity player) throws NullPointerException
	{
		final var cd = EntityHooks.getCustomData(Objects.requireNonNull(player));
		@Nullable BetterStatsServerPlayNetworkHandler spnh = cd.getProperty(CUSTOM_DATA_ID);
		if(spnh == null)
		{
			spnh = new BetterStatsServerPlayNetworkHandler(player);
			cd.setProperty(CUSTOM_DATA_ID, spnh);
		}
		return spnh;
	}
	// ==================================================
}