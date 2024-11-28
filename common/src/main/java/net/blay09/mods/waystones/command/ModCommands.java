package net.blay09.mods.waystones.command;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.blay09.mods.balm.api.command.BalmCommands;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneStyle;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.comparator.WaystoneComparators;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;

public class ModCommands {
    private static final SimpleCommandExceptionType ERROR_WAYSTONE_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable(
            "commands.waystones.waystone_not_found"));

    public static void initialize(BalmCommands commands) {
        commands.register(dispatcher -> dispatcher.register(Commands.literal("waystones")
                .requires(source -> source.isPlayer() && source.hasPermission(2))
                .then(Commands.literal("activate")
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("pos", BlockPosArgument.blockPos()).executes(context -> {
                                    final var targets = EntityArgument.getPlayers(context, "targets");
                                    final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                                    final var foundWaystone = WaystonesAPI.getWaystoneAt(context.getSource().getLevel(), pos);
                                    if (foundWaystone.isPresent()) {
                                        final var waystone = foundWaystone.get();
                                        for (final var player : targets) {
                                            WaystonesAPI.activateWaystone(player, waystone);
                                        }

                                        if (targets.size() == 1) {
                                            context.getSource().sendSuccess(() -> Component.translatable("commands.waystones.activate.success.single",
                                                    waystone.getName(), targets.iterator().next().getDisplayName()), true);
                                        } else {
                                            context.getSource()
                                                    .sendSuccess(() -> Component.translatable("commands.waystones.activate.success.multiple",
                                                            waystone.getName(),
                                                            targets.size()), true);
                                        }
                                    } else {
                                        throw ERROR_WAYSTONE_NOT_FOUND.create();
                                    }
                                    return targets.size();
                                }))))
                .then(Commands.literal("forget")
                        .then(argument("targets", EntityArgument.players())
                                .then(argument("pos", BlockPosArgument.blockPos()).executes(context -> {
                                    final var targets = EntityArgument.getPlayers(context, "targets");
                                    final var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                                    final var foundWaystone = WaystonesAPI.getWaystoneAt(context.getSource().getLevel(), pos);
                                    if (foundWaystone.isPresent()) {
                                        final var waystone = foundWaystone.get();
                                        for (final var player : targets) {
                                            WaystonesAPI.deactivateWaystone(player, waystone);
                                        }

                                        if (targets.size() == 1) {
                                            context.getSource().sendSuccess(() -> Component.translatable("commands.waystones.forget.success.single",
                                                    waystone.getName(), targets.iterator().next().getDisplayName()), true);
                                        } else {
                                            context.getSource()
                                                    .sendSuccess(() -> Component.translatable("commands.waystones.forget.success.multiple",
                                                            waystone.getName(),
                                                            targets.size()), true);
                                        }
                                    } else {
                                        throw ERROR_WAYSTONE_NOT_FOUND.create();
                                    }
                                    return targets.size();
                                }))
                                .then(Commands.literal("all").executes(context -> {
                                    final var targets = EntityArgument.getPlayers(context, "targets");
                                    int totalDeactivated = 0;
                                    for (final var player : targets) {
                                        final var waystones = PlayerWaystoneManager.getActivatedWaystones(player);
                                        for (final var waystone : waystones) {
                                            WaystonesAPI.deactivateWaystone(player, waystone);
                                        }
                                        totalDeactivated += waystones.size();
                                    }

                                    if (targets.size() == 1) {
                                        context.getSource()
                                                .sendSuccess(() -> Component.translatable("commands.waystones.forget.all.success.single",
                                                        targets.iterator().next().getDisplayName()), true);
                                    } else {
                                        context.getSource()
                                                .sendSuccess(() -> Component.translatable("commands.waystones.forget.all.success.multiple",
                                                        targets.size()), true);
                                    }
                                    return totalDeactivated;
                                }))))
                .then(Commands.literal("count")
                        .then(argument("player", EntityArgument.player()).executes(new CountWaystonesCommand())))
                .then(Commands.literal("list")
                        .then(argument("player", EntityArgument.player()).executes(ctx -> {
                                    final var caller = ctx.getSource().getPlayerOrException();
                                    final var target = ctx.getArgument("player", EntitySelector.class).findSinglePlayer(ctx.getSource());
                                    final var waystones = PlayerWaystoneManager.getActivatedWaystones(target)
                                            .stream()
                                            .filter(it -> it.isOwner(target))
                                            .sorted(WaystoneComparators.forAdminInspection(caller, target))
                                            .toList();
                                    ctx.getSource().sendSystemMessage(Component.translatable("commands.waystones.list.header", target.getScoreboardName()));
                                    for (var waystone : waystones) {
                                        ctx.getSource().sendSystemMessage(componentForWaystoneList(caller, target, waystone));
                                    }
                                    final var result = Component.translatable("commands.waystones.list.footer", waystones.size());
                                    ctx.getSource().sendSuccess(() -> result, false);
                                    return waystones.size();
                                })
                                .then(Commands.literal("all").executes(ctx -> {
                                    final var caller = ctx.getSource().getPlayerOrException();
                                    final var target = ctx.getArgument("player", EntitySelector.class).findSinglePlayer(ctx.getSource());
                                    final var waystones = PlayerWaystoneManager.getActivatedWaystones(target)
                                            .stream()
                                            .sorted(WaystoneComparators.forAdminInspection(caller, target))
                                            .toList();
                                    ctx.getSource().sendSystemMessage(Component.translatable("commands.waystones.list.all.header", target.getScoreboardName()));
                                    for (var waystone : waystones) {
                                        ctx.getSource().sendSystemMessage(componentForWaystoneList(caller, target, waystone));
                                    }
                                    final var ownedCount = waystones.stream().filter(it -> it.isOwner(target)).count();
                                    final var result = Component.translatable("commands.waystones.list.all.footer", waystones.size(), ownedCount);
                                    ctx.getSource().sendSuccess(() -> result, false);
                                    return waystones.size();
                                }))))
                .then(Commands.literal("gui")
                        .then(argument("player", EntityArgument.player()).executes(new OpenPlayerWaystonesGuiCommand())))
        ));
    }

    private static Component componentForWaystoneList(ServerPlayer caller, ServerPlayer target, Waystone waystone) {
        final var waystoneDimensionId = waystone.getDimension().location();
        final var waystonePos = waystone.getPos();
        Component location;
        if (waystone.getDimension() != caller.level().dimension()) {
            location = Component.translatable("commands.waystones.list.in_dimension", waystoneDimensionId);
        } else {
            final var distance = (int) caller.position().distanceTo(waystonePos.getCenter());
            location = Component.translatable("commands.waystones.list.at_distance", distance);
        }

        final var suggestedCommand = String.format("/execute in %s run teleport %d %d %d",
                waystoneDimensionId,
                waystonePos.getX(),
                waystonePos.getY(),
                waystonePos.getZ());

        final var coordinates = Component.translatable("commands.waystones.list.coordinates", waystonePos.getX(), waystonePos.getY(), waystonePos.getZ())
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestedCommand)));

        final var waystoneName = waystone.getName().copy()
                .withStyle(ChatFormatting.GREEN)
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestedCommand)));

        final var entryKey = waystone.isOwner(target) ? "commands.waystones.list.entry.owned" : "commands.waystones.list.entry.activated";
        return Component.translatable(entryKey, location, coordinates, waystoneName);
    }
}
