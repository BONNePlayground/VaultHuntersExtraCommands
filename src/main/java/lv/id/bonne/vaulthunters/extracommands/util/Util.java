//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.util;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


public class Util
{
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
    public static void sendGodMessageToAll(ServerLevel level, String text)
    {
        getRandom(Arrays.stream(VaultGod.values()).toList()).ifPresent(sender ->
        {
            TextComponent senderTxt = new TextComponent("[VG] ");

            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                append((new TextComponent(sender.getName())).withStyle(sender.getChatColor())).
                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));

            senderTxt.withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sender.getHoverChatComponent())));

            level.players().forEach(player -> player.sendMessage(
                senderTxt.append(new TextComponent(text).withStyle(ChatFormatting.WHITE)), ChatType.SYSTEM, net.minecraft.Util.NIL_UUID));
        });
    }


    /**
     * This method sends message to the player.
     * @param player that receives message.
     * @param text The message.
     */
    public static void sendGodMessageToPlayer(ServerPlayer player, String text)
    {
        getRandom(Arrays.stream(VaultGod.values()).toList()).ifPresent(sender ->
        {
            TextComponent senderTxt = new TextComponent("[VG] ");

            senderTxt.withStyle(ChatFormatting.DARK_PURPLE).
                append((new TextComponent(sender.getName())).withStyle(sender.getChatColor())).
                append((new TextComponent(": ")).withStyle(ChatFormatting.WHITE));

            senderTxt.withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sender.getHoverChatComponent())));

            player.sendMessage(senderTxt.append(new TextComponent(text).withStyle(ChatFormatting.WHITE)),
                ChatType.SYSTEM,
                net.minecraft.Util.NIL_UUID);
        });
    }
}
