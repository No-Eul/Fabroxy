package dev.noeul.fabricmod.fabroxy.mixin;

import dev.noeul.fabricmod.fabroxy.language.LanguagedServerPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements LanguagedServerPlayerEntity {
	private String language = Language.DEFAULT_LANGUAGE;

	@Inject(method = "setClientSettings", at = @At("HEAD"))
	private void inject$setClientSettings(ClientSettingsC2SPacket packet, CallbackInfo callbackInfo) {
		this.language = packet.language();
	}

	@Override
	public String getLanguage() {
		return this.language;
	}
}
