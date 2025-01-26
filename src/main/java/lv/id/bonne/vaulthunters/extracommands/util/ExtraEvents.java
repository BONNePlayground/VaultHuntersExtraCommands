//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.vaulthunters.extracommands.util;


import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.Event;


public class ExtraEvents
{
    public static final EntityTargetEvent ENTITY_TARGET_EVENT = register(new EntityTargetEvent());

    private static <T extends Event<?, ?>> T register(T event) {
        CommonEvents.REGISTRY.add(event);
        return event;
    }

    public static void init()
    {
    }
}
