package dev.noeul.fabricmod.fabroxy.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.net.SocketAddress;

@Environment(EnvType.SERVER)
@Mixin(ClientConnection.class)
public interface ClientConnectionAccessor {
	@Accessor("address")
	void address(SocketAddress address);
}
