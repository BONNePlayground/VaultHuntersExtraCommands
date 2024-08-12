//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.data;


import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;


public class ExtraCommandsData extends SavedData
{
    protected static final String DATA_NAME = "extra_commands_data";

    public Map<UUID, Integer> time = new ConcurrentHashMap<>();

    public ExtraCommandsData()
    {
    }


    /**
     * Writes our settings onto an NBT tag
     *
     * @param tag Tag we wish to write our settings onto
     * @return Modified NBT tag with our settings
     */
    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag)
    {
        tag.put("playerData", serializePlayerData());
        return tag;
    }


    /**
     * Return data file from minecraft server instance
     * @return ExtraCommandsData
     */
    public static ExtraCommandsData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(ExtraCommandsData.load(),
            ExtraCommandsData::new,
            DATA_NAME);
    }


    /**
     * Generates a function for loading settings from NBT
     *
     * @return Function for loading settings from NBT
     */
    public static Function<CompoundTag, ExtraCommandsData> load()
    {
        return (tag) ->
        {
            ExtraCommandsData data = new ExtraCommandsData();

            ListTag playerList = tag.getList("playerData", 10);

            playerList.forEach(dataTag ->
            {
                if (dataTag instanceof CompoundTag entry)
                {
                    data.time.put(entry.getUUID("uuid"), entry.getInt("time"));
                }
            });

            return data;
        };
    }


    private ListTag serializePlayerData()
    {
        ListTag returnList = new ListTag();

        this.time.forEach((uuid, count) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putInt("time", count);
            returnList.add(entry);
        });
        return returnList;
    }
}