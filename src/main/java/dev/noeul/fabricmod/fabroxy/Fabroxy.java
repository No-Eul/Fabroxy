package dev.noeul.fabricmod.fabroxy;

import dev.noeul.fabricmod.fabroxy.command.ProxyCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.SERVER)
public class Fabroxy implements DedicatedServerModInitializer {
	public static final ModContainer mod = FabricLoader.getInstance().getModContainer("fabroxy").get();
	public static final String ID = mod.getMetadata().getId();
	public static final String NAME = mod.getMetadata().getName();
	public static final Logger logger = LogManager.getLogger(ID);
	public static final Configs config = new Configs();

	@Override
	public void onInitializeServer() {
		config.load();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (!environment.dedicated) return;
			ProxyCommand.register(dispatcher);
		});
	}
}
