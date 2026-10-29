package dev.neothaum.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Port of thaumcraft.common.lib.events.CommandThaumcraft: /neothaum (or /nt) research, aspect and warp for a player. */
public final class CommandThaumcraft {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = dispatcher.register(build("neothaum"));
		for (String alias : new String[]{"nt"}) {
			dispatcher.register(Commands.literal(alias).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).redirect(root));
		}
	}

	private static LiteralArgumentBuilder<CommandSourceStack> build(String name) {
		return Commands.literal(name).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.executes(c -> invalid(c.getSource(), "§cUse /neothaum help to get help"))
			.then(Commands.literal("help").executes(c -> help(c.getSource())))
			.then(Commands.literal("research")
				.then(Commands.literal("list").executes(c -> listResearch(c.getSource())))
				.then(Commands.argument("player", EntityArgument.player())
					.then(Commands.argument("research", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(researchSuggestions(), b))
						.executes(CommandThaumcraft::research))))
			.then(Commands.literal("aspect")
				.then(Commands.argument("player", EntityArgument.player())
					.then(Commands.argument("aspect", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(aspectSuggestions(), b))
						.then(Commands.argument("amount", IntegerArgumentType.integer(1))
							.executes(c -> giveAspect(c.getSource(), EntityArgument.getPlayer(c, "player"), StringArgumentType.getString(c, "aspect"),
								IntegerArgumentType.getInteger(c, "amount")))))))
			.then(Commands.literal("config")
				.then(setting("hardnodes", "hard mode nodes", () -> NeoThaumConfig.current.hardNodes, v -> NeoThaumConfig.current.hardNodes = v))
				.then(setting("wussmode", "wuss mode", () -> !NeoThaumConfig.current.allowWarp, v -> NeoThaumConfig.current.allowWarp = !v)))
			.then(Commands.literal("warp")
				.then(Commands.argument("player", EntityArgument.player())
					.then(Commands.literal("set")
						.then(Commands.argument("amount", IntegerArgumentType.integer(0))
							.executes(c -> warp(c, true, ""))
							.then(Commands.argument("type", StringArgumentType.word())
								.suggests((c, b) -> SharedSuggestionProvider.suggest(new String[]{"PERM", "TEMP"}, b))
								.executes(c -> warp(c, true, StringArgumentType.getString(c, "type"))))))
					.then(Commands.literal("add")
						.then(Commands.argument("amount", IntegerArgumentType.integer(-100, 100))
							.executes(c -> warp(c, false, ""))
							.then(Commands.argument("type", StringArgumentType.word())
								.suggests((c, b) -> SharedSuggestionProvider.suggest(new String[]{"PERM", "TEMP"}, b))
								.executes(c -> warp(c, false, StringArgumentType.getString(c, "type"))))))));
	}

	/**
	 * /neothaum config <name> [true|false]: shows or sets one of TC4's server settings and saves neothaum.json. Hard mode nodes gives hungry,
	 * tainted and sinister nodes their nastier effects; wuss mode turns warp off (no warp events, none from research or crafting).
	 */
	private static LiteralArgumentBuilder<CommandSourceStack> setting(String name, String label, java.util.function.BooleanSupplier get,
		java.util.function.Consumer<Boolean> set) {
		return Commands.literal(name)
			.executes(c -> {
				c.getSource().sendSystemMessage(Component.literal("§5" + label + " is " + (get.getAsBoolean() ? "on" : "off")));
				return 1;
			})
			.then(Commands.argument("value", BoolArgumentType.bool()).executes(c -> {
				boolean v = BoolArgumentType.getBool(c, "value");
				set.accept(v);
				NeoThaumConfig.save();
				c.getSource().sendSuccess(() -> Component.literal("§5" + label + " is now " + (v ? "on" : "off")), true);
				return 1;
			}));
	}

	private static String[] researchSuggestions() {
		return java.util.stream.Stream.concat(java.util.stream.Stream.of("all", "reset"), ResearchManager.entries().stream().map(r -> r.key))
			.toArray(String[]::new);
	}

	private static String[] aspectSuggestions() {
		return java.util.stream.Stream.concat(java.util.stream.Stream.of("all"), Aspect.aspects.keySet().stream()).toArray(String[]::new);
	}

	private static int invalid(CommandSourceStack src, String usage) {
		src.sendSystemMessage(Component.literal("§cInvalid arguments"));
		src.sendSystemMessage(Component.literal(usage));
		return 0;
	}

	private static int help(CommandSourceStack src) {
		for (String line : new String[]{"§3You can also use /nt instead of /neothaum.", "§3Use this to give research to a player.",
			"  /neothaum research <list|player> <all|reset|<research>>", "§3Use this to give aspect research points to a player.",
			"  /neothaum aspect <player> <aspect|all> <amount>", "§3Use this to give set a players warp level.",
			"  /neothaum warp <player> <add|set> <amount> <PERM|TEMP>", "  not specifying perm or temp will just add normal warp",
			"§3Use this to see or change a server setting.", "  /neothaum config <hardnodes|wussmode> [true|false]"}) {
			src.sendSystemMessage(Component.literal(line));
		}
		return 1;
	}

	private static void success(CommandSourceStack src, ServerPlayer player, String told) {
		player.sendSystemMessage(Component.literal("§5" + src.getTextName() + told));
		src.sendSystemMessage(Component.literal("§5Success!"));
	}

	private static int listResearch(CommandSourceStack src) {
		for (ResearchItem ri : ResearchManager.entries()) {
			src.sendSystemMessage(Component.literal("§5" + ri.key));
		}
		return 1;
	}

	private static int research(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		CommandSourceStack src = c.getSource();
		ServerPlayer player = EntityArgument.getPlayer(c, "player");
		String key = StringArgumentType.getString(c, "research");
		if (key.equalsIgnoreCase("all")) {
			for (ResearchItem ri : ResearchManager.entries()) {
				ResearchManager.completeResearch(player, ri.key);
			}
			success(src, player, " has given you all research.");
		} else if (key.equalsIgnoreCase("reset")) {
			PlayerKnowledge k = ResearchManager.knowledge(player).copy();
			k.research.clear();
			ResearchManager.setKnowledge(player, k);
			ResearchManager.grantStartingKnowledge(player);
			success(src, player, " has reset you research.");
		} else if (ResearchManager.getResearch(key) != null) {
			giveRecursiveResearch(player, key);
			success(src, player, " gave you " + key + " research and its requisites.");
		} else {
			src.sendSystemMessage(Component.literal("§cResearch does not exist."));
			return 0;
		}
		return 1;
	}

	private static void giveRecursiveResearch(ServerPlayer player, String key) {
		ResearchItem ri = ResearchManager.getResearch(key);
		if (ri == null || !ResearchManager.completeResearch(player, key)) {
			return;
		}
		for (String p : ri.parents) {
			giveRecursiveResearch(player, p);
		}
		for (String p : ri.parentsHidden) {
			giveRecursiveResearch(player, p);
		}
		for (String p : ri.siblings) {
			giveRecursiveResearch(player, p);
		}
	}

	private static int giveAspect(CommandSourceStack src, ServerPlayer player, String name, int amount) {
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		if (name.equalsIgnoreCase("all")) {
			for (Aspect a : Aspect.aspects.values()) {
				k.addAspectPool(a, amount);
			}
			ResearchManager.setKnowledge(player, k);
			success(src, player, " gave you " + amount + " of all the aspects.");
			return 1;
		}
		Aspect aspect = Aspect.getAspect(name);
		if (aspect == null) {
			for (Aspect a : Aspect.aspects.values()) {
				if (name.equalsIgnoreCase(a.getName())) {
					aspect = a;
					break;
				}
			}
		}
		if (aspect == null) {
			src.sendSystemMessage(Component.literal("§cAspect does not exist."));
			return 0;
		}
		k.addAspectPool(aspect, amount);
		ResearchManager.setKnowledge(player, k);
		success(src, player, " gave you " + amount + " " + aspect.getName());
		return 1;
	}

	/** Warp types as TC4's PacketSyncWarp numbers them: 0 permanent, 1 normal (sticky), 2 temporary. */
	private static int warp(CommandContext<CommandSourceStack> c, boolean set, String type) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(c, "player");
		int amount = IntegerArgumentType.getInteger(c, "amount");
		int kind = type.equalsIgnoreCase("PERM") ? 0 : type.equalsIgnoreCase("TEMP") ? 2 : 1;
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		switch (kind) {
			case 0 -> k.warp = Math.max(0, set ? amount : k.warp + amount);
			case 2 -> k.warpTemp = Math.max(0, set ? amount : k.warpTemp + amount);
			default -> k.warpSticky = Math.max(0, set ? amount : k.warpSticky + amount);
		}
		ResearchManager.setKnowledge(player, k);
		if (set) {
			success(c.getSource(), player, " set your warp to " + amount);
		} else {
			ServerPlayNetworking.send(player, new TVNetwork.WarpMessage(kind, amount));
			success(c.getSource(), player, " added " + amount + " warp to your total.");
		}
		return 1;
	}

	private CommandThaumcraft() {
	}
}
