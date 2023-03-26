package dev.noeul.fabricmod.fabroxy.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.noeul.fabricmod.fabroxy.Fabroxy;
import dev.noeul.fabricmod.fabroxy.language.I18n;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@Environment(EnvType.SERVER)
public class ProxyCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				CommandManager.literal("proxy")
						.requires(source -> source.hasPermissionLevel(CommandManager.field_31841))
						.then(
								CommandManager.literal("reload")
										.executes(ProxyCommand::executeReload)
						)
		);
	}

	private static int executeReload(CommandContext<ServerCommandSource> context) {
		Fabroxy.config.load();
		context.getSource().sendFeedback(Text.of(I18n.translate("fabroxy.command.velocity.success", Fabroxy.NAME)), true);
		return Command.SINGLE_SUCCESS;
	}
}
