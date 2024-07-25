//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.util;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModItems;
import lv.id.bonne.vaulthunters.extracommands.ExtraCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;


public class Util
{
    public enum ItemType
    {
        SWORD(ModItems.SWORD),
        AXE(ModItems.AXE),
        BOOTS(ModItems.BOOTS),
        CHESTPLATE(ModItems.CHESTPLATE),
        HELMET(ModItems.HELMET),
        LEGGINGS(ModItems.LEGGINGS),
        SHIELD(ModItems.SHIELD),
        FOCUS(ModItems.FOCUS),
        WAND(ModItems.WAND),
        MAGNET(ModItems.MAGNET);

        ItemType(Item item)
        {
            this.item = item;
        }

        public Item item()
        {
            return this.item;
        }


        private final Item item;
    }


    /**
     * This method log error message.
     * @param message The message that need to be logged.
     * @return Runnable that will trigger message logging.
     */
    public static Runnable logError(String message)
    {
        return () -> ExtraCommands.LOGGER.error(message);
    }


    /**
     * This method returns random element from given list.
     * @param input List of input elements.
     * @return Optional of random element from list.
     */
    public static <T> Optional<T> getRandom(List<T> input)
    {
        int count = (int) (input.size() * Math.random());

        return input.stream().skip(count).findAny();
    }


    /**
     * This method sends all players in given level given message.
     * @param level Level that receives message.
     * @param text The message.
     */
    public static void sendGodMessageToAll(ServerLevel level, String text, VaultGod... god)
    {
        Util.sendGodMessageToAll(level,
            new TextComponent(text).withStyle(ChatFormatting.WHITE),
            god);
    }


    /**
     * This method sends message to the player.
     * @param player that receives message.
     * @param text The message.
     */
    public static void sendGodMessageToPlayer(ServerPlayer player, String text, VaultGod... god)
    {
        Util.sendGodMessageToPlayer(player,
            new TextComponent(text).withStyle(ChatFormatting.WHITE),
            god);
    }


    /**
     * This method sends all players in given level given message.
     * @param level Level that receives message.
     * @param text The message.
     */
    public static void sendGodMessageToAll(ServerLevel level, Component text, VaultGod... god)
    {
        Optional<VaultGod> randomGod;

        if (god == null || god.length == 0)
        {
            randomGod = Util.getRandom(Arrays.stream(VaultGod.values()).toList());
        }
        else
        {
            randomGod = Util.getRandom(Arrays.stream(god).toList());
        }

        randomGod.ifPresentOrElse(sender ->
        {
            TextComponent senderTxt = new TextComponent("[VG] ");

            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                append((new TextComponent(sender.getName())).withStyle(sender.getChatColor())).
                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));

            senderTxt.withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sender.getHoverChatComponent())));

            level.players().forEach(player -> player.sendMessage(
                senderTxt.append(text), ChatType.SYSTEM, net.minecraft.Util.NIL_UUID));
        }, Util.logError("Could not find a valid god to send message."));
    }


    /**
     * This method sends message to the player.
     * @param player that receives message.
     * @param text The message.
     */
    public static void sendGodMessageToPlayer(ServerPlayer player, Component text, VaultGod... god)
    {
        Optional<VaultGod> randomGod;

        if (god == null || god.length == 0)
        {
            randomGod = Util.getRandom(Arrays.stream(VaultGod.values()).toList());
        }
        else
        {
            randomGod = Util.getRandom(Arrays.stream(god).toList());
        }

        randomGod.ifPresentOrElse(sender ->
        {
            TextComponent senderTxt = new TextComponent("[VG] ");

            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                append((new TextComponent(sender.getName())).withStyle(sender.getChatColor())).
                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));

            senderTxt.withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sender.getHoverChatComponent())));

            player.sendMessage(senderTxt.append(text), ChatType.SYSTEM, net.minecraft.Util.NIL_UUID);
        }, Util.logError("Could not find a valid god to send message."));
    }
}
