//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.data;


import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;


public class ExtraCommandsData extends SavedData
{
    protected static final String DATA_NAME = "extra_commands_data";

    public Map<ResourceLocation, Boolean> paused = new HashMap<>();

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
        tag.put("pausedVaults", serializePaused());
        return tag;
    }


    /**
     * Method for fetching the settings from a Level
     *
     * @param level Preferably a ServerLevel, but not strictly required.
     * @return A populated WorldSettings instance
     */
    public static ExtraCommandsData get(Level level)
    {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER &&
            level instanceof ServerLevel serverLevel)
        {
            return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(ExtraCommandsData.load(serverLevel),
                ExtraCommandsData::new,
                "extra_commands_data");
        }

        return null;
    }


    /**
     * Generates a function for loading settings from NBT
     *
     * @param level Preferably the ServerLevel we load our data onto. In most cases the overworld since it is always
     * partially loaded.
     * @return Function for loading settings from NBT
     */
    public static Function<CompoundTag, ExtraCommandsData> load(ServerLevel level)
    {
        return (tag) ->
        {
            ExtraCommandsData data = new ExtraCommandsData();

            if (tag.contains("pausedVaults"))
            {
                CompoundTag paused = tag.getCompound("pausedVaults");

                paused.getAllKeys().forEach(key ->
                {
                    data.paused.put(ResourceLocation.tryParse(key), paused.getBoolean(key));
                });
            }

            return data;
        };
    }


    private CompoundTag serializePaused()
    {
        CompoundTag tag = new CompoundTag();
        this.paused.forEach(((key, value) -> tag.putBoolean(key.toString(), value)));
        return tag;
    }
}