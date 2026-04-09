package com.astaro.mixGame.config;

import com.astaro.mixGame.Game.Arena;
import com.astaro.mixGame.Game.ArenaController;
import com.astaro.mixGame.MixGame;
import com.astaro.mixGame.data.PlayerStats;
import com.astaro.mixGame.data.SetupSession;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import com.mojang.brigadier.Command;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;


public class CommandManager {

    private static final MixGame plugin = MixGame.instance;
    private static LiteralArgumentBuilder<CommandSourceStack> mixgame = Commands.literal("mixgame");

    public static void createPlayerCommands(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.requires(s -> s.getSender().hasPermission("mixgame.default"))
                .then(Commands.literal("join").then(Commands.argument("arena_name", StringArgumentType.word()).suggests(
                        (ctx, b) -> {
                            Collection<ArenaController> allArenas = plugin.getArenaManager().getAllArenas();
                            for (ArenaController arena : allArenas) {
                                b.suggest(arena.getSettings().arenaName());
                            }
                            return b.buildFuture();
                        }
                ).executes(
                        ctx -> {
                            String arena_name = StringArgumentType.getString(ctx, "arena_name");
                            Collection<ArenaController> allArenas = plugin.getArenaManager().getAllArenas();
                            Player p = checkConsole(ctx.getSource().getSender());
                            if (p == null) return Command.SINGLE_SUCCESS;
                            if (allArenas.isEmpty()) {
                                plugin.getChatService().sendMessage(p, "ErrorMessages.noArenas");
                                return Command.SINGLE_SUCCESS;
                            }
                            plugin.getArenaManager().joinPlayer(p, arena_name);
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1f, 1f);
                            return Command.SINGLE_SUCCESS;

                        }
                )))
                .then(Commands.literal("leave").executes(
                        ctx -> {
                            Player p = checkConsole(ctx.getSource().getSender());
                            if (p == null) return Command.SINGLE_SUCCESS;
                            ArenaController arena = plugin.getArenaManager().getArenaByPlayer(p);
                            if (arena != null) {
                                String arenaName = arena.getSettings().arenaName();
                                plugin.getChatService().sendMessage(p, "PlayerMessages.leave", "%arenaName%", arenaName);
                                plugin.getArenaManager().leavePlayer(p);
                            } else {
                                plugin.getChatService().sendMessage(p, "ErrorMessages.notInArena");
                                p.playSound(p.getLocation(), Sound.ENTITY_BAT_HURT, 1f, 1f);
                            }

                            return Command.SINGLE_SUCCESS;
                        }
                ))
                .then(Commands.literal("stats").executes(
                        ctx -> {
                            Player p = checkConsole(ctx.getSource().getSender());
                            if (p == null) return Command.SINGLE_SUCCESS;
                            handleStats(p);
                            return Command.SINGLE_SUCCESS;
                        }
                ))
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            Player p = checkConsole(ctx.getSource().getSender());
                            if (p == null) return Command.SINGLE_SUCCESS;
                            if (p.hasPermission("mixgame.admin")) {
                                plugin.getChatService().sendMessage(p, "HelpMessages.adminHelp");
                            } else {
                                plugin.getChatService().sendMessage(p, "HelpMessages.playerHelp");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
    // TODO: Add EDIT part
    public static void createAdminCommands(LiteralArgumentBuilder<CommandSourceStack> root) {

        LiteralArgumentBuilder<CommandSourceStack> adminNode = Commands.literal("admin")
                .requires(s -> s.getSender().hasPermission("mixgame.admin"));

        adminNode.then(Commands.literal("create")
                .then(Commands.argument("arenaName", StringArgumentType.word())
                        .executes(
                                ctx -> {
                                    String arenaName = StringArgumentType.getString(ctx, "arenaName");
                                    Player p = checkConsole(ctx.getSource().getSender());
                                    if (plugin.getSetupManager() == null) {
                                        p.sendMessage("§cОшибка: SetupManager не инициализирован!");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (p == null) return Command.SINGLE_SUCCESS;

                                    plugin.getSetupManager().startSession(p.getUniqueId(), arenaName);
                                    plugin.getChatService().sendMessage(p, "AdminMessages.arenaCreated", "%arena%", arenaName);
                                    plugin.getChatService().sendMessage(p, "AdminMessages.sessionStart");

                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                ));
        adminNode.then(Commands.literal("setMaterial")
                .then(Commands.argument("material", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                                    Set.of("WOOL", "CONCRETE", "TERRACOTTA").forEach(builder::suggest);
                                    return builder.buildFuture();
                                }
                        ).executes(ctx -> {
                                    Player p = checkConsole(ctx.getSource().getSender());
                                    if (p == null) return Command.SINGLE_SUCCESS;
                                    SetupSession session = checkAndGetSession(p);
                                    session.setFloorMaterial(StringArgumentType.getString(ctx, "material"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
        adminNode.then(Commands.literal("setSpawn").executes(
                ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    session.setSpawn(p.getLocation());
                    plugin.getChatService().sendMessage(p, "AdminMessages.setSpawnLocation");
                    return Command.SINGLE_SUCCESS;
                }
        ));
        adminNode.then(Commands.literal("setLobby").executes(ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    session.setLobby(p.getLocation());
                    plugin.getChatService().sendMessage(p, "AdminMessages.setSpawnLocation");
                    return Command.SINGLE_SUCCESS;
                }
        ));
        adminNode.then(Commands.literal("setFirstPos").executes(
                ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    session.setLoc1(p.getLocation());
                    plugin.getChatService().sendMessage(p, "AdminMessages.setFirstPos");
                    return Command.SINGLE_SUCCESS;
                }
        ));
        adminNode.then(Commands.literal("setSecondPos").executes(
                ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    session.setLoc2(p.getLocation());
                    plugin.getChatService().sendMessage(p, "AdminMessages.setSecondPos");
                    return Command.SINGLE_SUCCESS;
                }
        ));
        adminNode.then(Commands.literal("setEndLoc").executes(
                ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    session.setEnd(p.getLocation());
                    plugin.getChatService().sendMessage(p, "AdminMessages.setEndLocation");
                    return Command.SINGLE_SUCCESS;
                }
        ));
        adminNode.then(Commands.literal("setMinPlayers")
                .then(Commands.argument("minPlayers", IntegerArgumentType.integer(2))
                        .executes(ctx -> {
                                    Player p = checkConsole(ctx.getSource().getSender());
                                    if (p == null) return Command.SINGLE_SUCCESS;
                                    SetupSession session = checkAndGetSession(p);
                                    int minPlayers = IntegerArgumentType.getInteger(ctx, "minPlayers");
                                    if (session.getMinPlayers() < 2 || session.getMaxPlayers() < minPlayers) {
                                        plugin.getChatService().sendMessage(p, "ErrorMessages.playerCountError");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    session.setMinPlayers(minPlayers);
                                    plugin.getChatService().sendMessage(p, "AdminMessages.setMinPlayers");
                                    return Command.SINGLE_SUCCESS;
                                }

                        )
                )
        );
        adminNode.then(Commands.literal("setMaxPlayers")
                .then(Commands.argument("maxPlayers", IntegerArgumentType.integer(2))
                        .executes(ctx -> {
                                    Player p = checkConsole(ctx.getSource().getSender());
                                    if (p == null) return Command.SINGLE_SUCCESS;
                                    SetupSession session = checkAndGetSession(p);
                                    int maxPlayers = IntegerArgumentType.getInteger(ctx, "maxPlayers");
                                    if (session.getMaxPlayers() > 16 || session.getMinPlayers() > maxPlayers) {
                                        plugin.getChatService().sendMessage(p, "ErrorMessages.playerCountError");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    session.setMaxPlayers(maxPlayers);
                                    plugin.getChatService().sendMessage(p, "AdminMessages.setMaxPlayers");
                                    return Command.SINGLE_SUCCESS;
                                }

                        )
                )
        );
        adminNode.then(Commands.literal("setDifficulty")
                .then(Commands.argument("difficulty", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                                    Player p = checkConsole(ctx.getSource().getSender());
                                    if (p == null) return Command.SINGLE_SUCCESS;
                                    SetupSession session = checkAndGetSession(p);
                                    int level = IntegerArgumentType.getInteger(ctx, "difficulty");
                                    if (level < 1 || level > 5) {
                                        plugin.getChatService().sendMessage(p, "ErrorMessages.levelError");
                                    }
                                    session.setSectionSize(level);
                                    plugin.getChatService().sendMessage(p, "AdminMessages.setMinPlayers");
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
        adminNode.then(Commands.literal("cancel").executes(
                ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    if (session != null) {
                        plugin.getSetupManager().removeSession(p.getUniqueId());
                        plugin.getChatService().sendMessage(p, "AdminMessages.cancel");
                    }
                    return Command.SINGLE_SUCCESS;

                }
        ));
        adminNode.then(Commands.literal("finish").executes(
                ctx -> {
                    Player p = checkConsole(ctx.getSource().getSender());
                    if (p == null) return Command.SINGLE_SUCCESS;
                    SetupSession session = checkAndGetSession(p);
                    Arena newArena = session.build();
                    if (newArena == null) {
                        plugin.getChatService().sendMessage(p, "ErrorMessages.setupIncomplete");
                        return Command.SINGLE_SUCCESS;
                    }
                    plugin.getArenaManager().saveArena(newArena);
                    plugin.getSetupManager().removeSession(p.getUniqueId());
                    plugin.getChatService().sendMessage(p, "AdminMessages.arenaCreated", "%name%", newArena.arenaName());
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    return Command.SINGLE_SUCCESS;

                }
        ));
        root.then(adminNode);
    }


    private static void handleStats(Player player) {
        plugin.getDatabase().getUserStats(player.getName()).thenAccept(optStats -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (optStats.isEmpty()) {
                    plugin.getChatService().sendMessage(player, "ErrorMessages.noFoundPlayerInBase");
                    return;
                }

                PlayerStats stats = optStats.get();
                plugin.getChatService().sendMessage(player, "PlayerMessages.statsInfo",
                        "%points%", String.valueOf(stats.points()),
                        "%won%", String.valueOf(stats.won()),
                        "%lost%", String.valueOf(stats.lost()),
                        "%wlr%", String.format("%.2f", stats.getWLR()),
                        "%played%", String.valueOf(stats.getPlayedGames())
                );
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
            });
        });
    }


    private static SetupSession checkAndGetSession(Player player) {
        SetupSession session = plugin.getSetupManager().getSession(player.getUniqueId());
        if (session == null) {
            plugin.getChatService().sendMessage(player, "ErrorMessages.noActiveSession");
        }
        return session;
    }

    private static Player checkConsole(CommandSender sender) {
        if (sender instanceof Player p) {
            return p;
        }
        sender.sendRichMessage("<red>You can't use this command from console");
        return null;
    }


    public LiteralCommandNode<CommandSourceStack> registerCommands() {
        createPlayerCommands(mixgame);
        createAdminCommands(mixgame);
        return mixgame.build();
    }
}
