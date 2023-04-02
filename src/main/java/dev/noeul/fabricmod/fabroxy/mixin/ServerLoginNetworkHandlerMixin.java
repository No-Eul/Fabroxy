package dev.noeul.fabricmod.fabroxy.mixin;

import com.destroystokyo.paper.proxy.VelocityProxy;
import com.mojang.authlib.GameProfile;
import dev.noeul.fabricmod.fabroxy.Fabroxy;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadLocalRandom;

@Environment(EnvType.SERVER)
@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
	private int velocityLoginMessageId = -1; // Paper - Velocity support

	@Shadow static @Final Logger LOGGER;
	@Shadow @Final ClientConnection connection;
	@Shadow @Nullable GameProfile profile;
	@Shadow ServerLoginNetworkHandler.State state;

	@Shadow public abstract void disconnect(Text reason);

	@Inject(
			method = "onHello",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;state:Lnet/minecraft/server/network/ServerLoginNetworkHandler$State;",
					opcode = Opcodes.PUTFIELD,
					ordinal = 2
			),
			cancellable = true
	)
	private void inject$onHello(LoginHelloC2SPacket packet, CallbackInfo callbackInfo) {
		if (Fabroxy.config.enabled) {
			this.velocityLoginMessageId = ThreadLocalRandom.current().nextInt();
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION);
			this.connection.send(new LoginQueryRequestS2CPacket(this.velocityLoginMessageId, VelocityProxy.PLAYER_INFO_CHANNEL, buf));
			callbackInfo.cancel();
		}
	}

	@Inject(method = "onQueryResponse", at = @At("HEAD"), cancellable = true)
	private void inject$onQueryResponse(LoginQueryResponseC2SPacket packet, CallbackInfo callbackInfo) {
		if (Fabroxy.config.enabled && packet.getQueryId() == this.velocityLoginMessageId) {
			PacketByteBuf buf = packet.getResponse();
			if (buf == null) {
				this.disconnect(Text.of("This server requires you to connect with Velocity."));
				callbackInfo.cancel();
				return;
			}

			if (!VelocityProxy.checkIntegrity(buf)) {
				this.disconnect(Text.of("Unable to verify player details"));
				callbackInfo.cancel();
				return;
			}

			int version = buf.readVarInt();
			if (version > VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION)
				throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted upto " + VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION);

			SocketAddress listening = this.connection.getAddress();
			int port = listening instanceof InetSocketAddress address ? address.getPort() : 0;
			((ClientConnectionAccessor) this.connection).address(new InetSocketAddress(VelocityProxy.readAddress(buf), port));

			this.profile = VelocityProxy.createProfile(buf);

			if (this.velocityLoginMessageId == -1 && Fabroxy.config.enabled) {
				this.disconnect(Text.of("This server requires you to connect with Velocity."));
				callbackInfo.cancel();
				return;
			}

			ServerLoginNetworkHandlerMixin.LOGGER.info("UUID of player {} is {}", this.profile.getName(), this.profile.getId());
			this.state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;

			callbackInfo.cancel();
		}
	}
}
