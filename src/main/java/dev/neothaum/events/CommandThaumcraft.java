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
				// the settings file read again, as after editing it by hand
				.then(Commands.literal("reload").executes(c -> reloadConfig(c.getSource())))
				.then(setting("hardnodes", "hard mode nodes", () -> NeoThaumConfig.current.hardNodes, v -> NeoThaumConfig.current.hardNodes = v))
				// NeoThaum Additions, the features not in TC4: off for TC4 as it was (their research and recipes follow on restart)
				.then(Commands.literal("additions")
					.executes(c -> {
						c.getSource().sendSystemMessage(Component.literal("§5NeoThaum Additions are " + (NeoThaumConfig.current.additionsEnabled ? "on" : "off")));
						return 1;
					})
					.then(Commands.argument("value", BoolArgumentType.bool()).executes(c -> {
						boolean v = BoolArgumentType.getBool(c, "value");
						NeoThaumConfig.current.additionsEnabled = v;
						additionsChanged(c.getSource(), "NeoThaum Additions are now " + (v ? "on" : "off"));
						return 1;
					})))
				.then(setting("wussmode", "wuss mode", () -> !NeoThaumConfig.current.allowWarp, v -> NeoThaumConfig.current.allowWarp = !v))
				.then(number("researchdifficulty", "research difficulty", -1, 1, () -> NeoThaumConfig.current.researchDifficulty,
					v -> NeoThaumConfig.current.researchDifficulty = v))
				.then(number("aspectcap", "aspect total cap", 1, 100000, () -> NeoThaumConfig.current.aspectTotalCap,
					v -> NeoThaumConfig.current.aspectTotalCap = v)))
			// Traveller's Totems for villages generated before they had them
			.then(totems("totems"))
			// one addition at a time: /neothaum additions [addition] [enable|disable]
			.then(Commands.literal("additions")
				.executes(c -> listAdditions(c.getSource()))
				.then(Commands.argument("addition", StringArgumentType.word())
					.suggests((c, b) -> SharedSuggestionProvider.suggest(dev.neothaum.addon.Additions.ALL, b))
					.executes(c -> addition(c.getSource(), StringArgumentType.getString(c, "addition"), null))
					.then(Commands.literal("enable").executes(c -> addition(c.getSource(), StringArgumentType.getString(c, "addition"), true)))
					.then(Commands.literal("disable").executes(c -> addition(c.getSource(), StringArgumentType.getString(c, "addition"), false)))))
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
				changed(c.getSource());
				c.getSource().sendSuccess(() -> Component.literal("§5" + label + " is now " + (v ? "on" : "off")), true);
				return 1;
			}));
	}

	/** /neothaum config <name> [value]: the same for a whole-number setting. */
	private static LiteralArgumentBuilder<CommandSourceStack> number(String name, String label, int min, int max, java.util.function.IntSupplier get,
		java.util.function.IntConsumer set) {
		return Commands.literal(name)
			.executes(c -> {
				c.getSource().sendSystemMessage(Component.literal("§5" + label + " is " + get.getAsInt()));
				return 1;
			})
			.then(Commands.argument("value", IntegerArgumentType.integer(min, max)).executes(c -> {
				int v = IntegerArgumentType.getInteger(c, "value");
				set.accept(v);
				changed(c.getSource());
				c.getSource().sendSuccess(() -> Component.literal("§5" + label + " is now " + v), true);
				return 1;
			}));
	}

	/** Saves neothaum.json and sends the settings the clients follow to everyone online. */
	private static void changed(CommandSourceStack source) {
		NeoThaumConfig.save();
		for (var player : source.getServer().getPlayerList().getPlayers()) {
			dev.neothaum.network.TVNetwork.sendConfig(player);
		}
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

	/**
	 * /neothaum config reload: reads config/neothaum.json again, then the research (which follows the additions' switches) and the data
	 * (recipes and aspects, which do too), and sends the settings to everyone online.
	 */
	private static int reloadConfig(CommandSourceStack src) {
		src.sendSystemMessage(Component.literal("§5Reading NeoThaum's settings again..."));
		NeoThaumConfig.load();
		refresh(src, "NeoThaum settings, research, recipes and aspects reloaded.");
		return 1;
	}

	/** Research, then the data (recipes and aspects), read again to follow the settings, which go out to everyone online. */
	private static void refresh(CommandSourceStack src, String done) {
		ResearchManager.reload();
		var server = src.getServer();
		changed(src);
		server.reloadResources(server.getPackRepository().getSelectedIds()).thenRun(() -> server.execute(
			() -> src.sendSuccess(() -> Component.literal("§5" + done), true)))
			.exceptionally(e -> {
				src.sendFailure(Component.literal("§cNeoThaum's settings changed, but reloading the data failed: " + e.getMessage()));
				return null;
			});
	}

	/** An additions switch changed: saved, and the research, recipes and aspects follow at once. */
	private static void additionsChanged(CommandSourceStack src, String done) {
		NeoThaumConfig.save();
		refresh(src, done);
	}

	/**
	 * /neothaum totems: villages [radius], for villages generated before they had totems; overworld [radius] [frequency], for
	 * wild totems on land generated before 1.2.4.
	 */
	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> totems(String name) {
		return Commands.literal(name)
			.then(Commands.literal("villages")
				.executes(c -> villageTotems(c.getSource(), 1024))
				.then(Commands.argument("radius", IntegerArgumentType.integer(16, 4096))
					.executes(c -> villageTotems(c.getSource(), IntegerArgumentType.getInteger(c, "radius")))))
			.then(Commands.literal("overworld")
				.executes(c -> wildTotems(c.getSource(), 1024, 1.0))
				.then(Commands.argument("radius", IntegerArgumentType.integer(16, 8192))
					.executes(c -> wildTotems(c.getSource(), IntegerArgumentType.getInteger(c, "radius"), 1.0))
					.then(Commands.argument("frequency", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0.01, 100.0))
						.executes(c -> wildTotems(c.getSource(), IntegerArgumentType.getInteger(c, "radius"),
							com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(c, "frequency"))))));
	}

	/**
	 * /neothaum totems overworld [radius] [frequency]: wild Traveller's Totems on overworld land already generated within the radius (1024
	 * unless given), each chunk with world generation's chance of one (1 in 3000) times the frequency (1 unless given). The same chunks win
	 * every time it is run, so running it again adds none; a higher frequency adds more. Never where players have spent time.
	 */
	private static int wildTotems(CommandSourceStack src, int radius, double frequency) {
		if (!dev.neothaum.addon.totems.TravellersTotems.enabled()) {
			src.sendFailure(Component.literal("§cTraveller's Totems are switched off (/neothaum additions travellers_totems enable)."));
			return 0;
		}
		var level = src.getServer().overworld();
		var centre = src.getLevel() == level ? net.minecraft.core.BlockPos.containing(src.getPosition()) : level.getRespawnData().pos();
		src.sendSystemMessage(Component.literal("§5Raising wild totems within " + radius + " blocks of " + centre.getX() + " " + centre.getZ()
			+ " (1 chunk in " + Math.round(dev.neothaum.addon.totems.TotemFeature.RARITY / frequency) + ")..."));
		var survey = dev.neothaum.addon.totems.WildTotems.raiseAround(level, centre, radius, frequency);
		for (var at : survey.raised()) {
			src.sendSystemMessage(raisedAt(at));
		}
		survey.passed().forEach((why, n) -> src.sendSystemMessage(Component.literal("§7  " + n + " passed over: " + why)));
		int raised = survey.raised().size();
		src.sendSuccess(() -> Component.literal("§5" + survey.chunks() + " chunks looked at, " + survey.won() + " drew a totem, " + raised
			+ " raised."), true);
		return raised;
	}

	/** "Raised a totem at x y z", the coordinates clickable: a click puts "/tp @s x y z" in the chat box, to go and see it. */
	private static Component raisedAt(net.minecraft.core.BlockPos at) {
		String xyz = at.getX() + " " + at.getY() + " " + at.getZ();
		return Component.literal("  Raised a totem at ").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE)
			.append(Component.literal("[" + xyz + "]").withStyle(style -> style.withColor(net.minecraft.ChatFormatting.LIGHT_PURPLE)
				.withUnderlined(true)
				.withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand("/tp @s " + at.getX() + " " + (at.getY() + 1) + " " + at.getZ()))
				.withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("Click to put a /tp to it in the chat box")))));
	}

	/**
	 * /neothaum totems villages [radius]: every village already generated within the radius (in blocks, 1024 unless given) of where the
	 * command is run that has no Traveller's Totem gets one, as a village generated now would.
	 */
	private static int villageTotems(CommandSourceStack src, int radius) {
		if (!dev.neothaum.addon.totems.TravellersTotems.enabled()) {
			src.sendFailure(Component.literal("§cTraveller's Totems are switched off (/neothaum additions travellers_totems enable)."));
			return 0;
		}
		src.sendSystemMessage(Component.literal("§5Looking for villages without a Traveller's Totem within " + radius + " blocks..."));
		var survey = dev.neothaum.addon.totems.VillageTotems.raiseAround(src.getLevel(), net.minecraft.core.BlockPos.containing(src.getPosition()),
			radius);
		for (var at : survey.raised()) {
			src.sendSystemMessage(raisedAt(at));
		}
		for (var at : survey.noRoom()) {
			src.sendSystemMessage(Component.literal("§c  No level open ground for a totem in the village at " + at.getX() + " " + at.getZ()));
		}
		int raised = survey.raised().size();
		src.sendSuccess(() -> Component.literal("§5" + survey.villages() + (survey.villages() == 1 ? " village" : " villages") + " found: "
			+ survey.had() + " had a totem already, " + raised + " raised" + (survey.noRoom().isEmpty() ? "." : ", " + survey.noRoom().size()
			+ " with no room.")), true);
		return raised;
	}

	/** /neothaum additions: each addition, whether it is on, and the master switch. */
	private static int listAdditions(CommandSourceStack src) {
		src.sendSystemMessage(Component.literal("§5NeoThaum Additions (features not in TC4)" + (NeoThaumConfig.current.additionsEnabled ? ""
			: ", all off by /neothaum config additions false") + ":"));
		for (String a : dev.neothaum.addon.Additions.ALL) {
			boolean on = dev.neothaum.addon.Additions.own(a);
			src.sendSystemMessage(Component.literal("  " + (on ? "§a" : "§c") + a + " §7" + (on ? "enabled" : "disabled")));
		}
		src.sendSystemMessage(Component.literal("§3Use /neothaum additions <addition> <enable|disable> to switch one."));
		return 1;
	}

	/** /neothaum additions <addition> [enable|disable]: shows or switches one addition. */
	private static int addition(CommandSourceStack src, String name, Boolean on) {
		if (!dev.neothaum.addon.Additions.ALL.contains(name)) {
			return invalid(src, "§cNo addition " + name + ": use /neothaum additions to list them");
		}
		if (on == null) {
			src.sendSystemMessage(Component.literal("§5" + name + " is " + (dev.neothaum.addon.Additions.own(name) ? "enabled" : "disabled")
				+ (NeoThaumConfig.current.additionsEnabled ? "" : " (but every addition is off by /neothaum config additions false)")));
			return 1;
		}
		dev.neothaum.addon.Additions.set(name, on);
		additionsChanged(src, name + " is now " + (on ? "enabled" : "disabled") + ".");
		return 1;
	}

	private static int help(CommandSourceStack src) {
		for (String line : new String[]{"§3You can also use /nt instead of /neothaum.", "§3Use this to give research to a player.",
			"  /neothaum research <list|player> <all|reset|<research>>", "§3Use this to give aspect research points to a player.",
			"  /neothaum aspect <player> <aspect|all> <amount>", "§3Use this to give set a players warp level.",
			"  /neothaum warp <player> <add|set> <amount> <PERM|TEMP>", "  not specifying perm or temp will just add normal warp",
			"§3Use this to see or change a server setting.", "  /neothaum config <hardnodes|wussmode|additions> [true|false]",
			"  /neothaum config researchdifficulty [-1|0|1]", "  /neothaum config aspectcap [amount]",
			"§3Use this after editing config/neothaum.json by hand.", "  /neothaum config reload",
			"§3Use this to give villages generated before 1.2.1 their Traveller's Totems.", "  /neothaum totems villages [radius]",
			"§3Use this to put wild Traveller's Totems on land generated before 1.2.4 (frequency 1 is 1 chunk in 3000).",
			"  /neothaum totems overworld [radius] [frequency]",
			"§3Use this to list the additions (features not in TC4), or switch one on or off.", "  /neothaum additions [addition] [enable|disable]"}) {
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
