//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extracommands.data;


import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;


/**
 * This data file stores if dimension is paused or not.
 */
public class ExtraCommandsWorldData  extends SavedData
{
    public ExtraCommandsWorldData()
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
        tag.putBoolean("paused", this.paused);
        tag.putBoolean("tick", this.tickStop);
        return tag;
    }


    /**
     * Return if dimension is paused or not.
     * @return value of paused.
     */
    public boolean isPaused()
    {
        return this.paused;
    }


    /**
     * Return if dimension is ticked stopped or not.
     * @return value of tickStop.
     */
    public boolean isTickStop()
    {
        return this.paused && this.tickStop;
    }


    /**
     * This method sets if vault is paused or not.
     * @param paused new paused value.
     */
    public void setPaused(boolean paused, boolean tickStop)
    {
        this.paused = paused;
        this.tickStop = tickStop;
    }


    /**
     * Return data file from minecraft server instance
     * @return ExtraCommandsData
     */
    public static ExtraCommandsWorldData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(ExtraCommandsWorldData.load(),
            ExtraCommandsWorldData::new,
            DATA_NAME);
    }


    /**
     * Load ExtraCommandsWorldData data from tag.
     * @return Function that would load data.
     */
    public static Function<CompoundTag, ExtraCommandsWorldData> load()
    {
        return (tag) ->
        {
            ExtraCommandsWorldData data = new ExtraCommandsWorldData();
            data.paused = tag.getBoolean("paused");
            data.tickStop = tag.getBoolean("tick");
            return data;
        };
    }


    /**
     * Indicates if world is paused or not.
     */
    private boolean paused;

    /**
     * Indicates if world is tick-stopped or not.
     */
    private boolean tickStop;

    /**
     * The datafile name.
     */
    protected static final String DATA_NAME = "extra_commands_world_data";
}
