package lv.id.bonne.vaulthunters.extracommands.util;


import java.util.function.Consumer;

import iskallia.vault.core.event.ForgeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;


public class EntityTargetEvent extends ForgeEvent<EntityTargetEvent, LivingChangeTargetEvent>
{
    public EntityTargetEvent()
    {
    }


    protected EntityTargetEvent(EntityTargetEvent parent)
    {
        super(parent);
    }


    @Override
    protected void initialize()
    {
        for (EventPriority priority : EventPriority.values())
        {
            MinecraftForge.EVENT_BUS.addListener(priority, true, (Consumer<LivingChangeTargetEvent>) event -> this.invoke(event));
        }
    }


    public EntityTargetEvent createChild()
    {
        return new EntityTargetEvent(this);
    }
}