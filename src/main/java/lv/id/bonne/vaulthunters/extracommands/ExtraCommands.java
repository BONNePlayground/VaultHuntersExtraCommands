package lv.id.bonne.vaulthunters.extracommands;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import lv.id.bonne.vaulthunters.extracommands.commands.*;
import lv.id.bonne.vaulthunters.extracommands.configs.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;


// The value here should match an entry in the META-INF/mods.toml file
@Mod("vault_hunters_extra_commands")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExtraCommands
{
    public ExtraCommands()
    {
        MinecraftForge.EVENT_BUS.register(this);

        ExtraCommands.CONFIGURATION = new Configuration();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.GENERAL_SPEC, "vault_hunters_extra_commands.toml");
    }


    /**
     * Forge Event Bus
     */
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents
    {
        /**
         * Registers the mod's commands
         * @param event The event holding the command dispatcher
         */
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event)
        {
            ModifiersCommand.register(event.getDispatcher());
            VaultTimerCommand.register(event.getDispatcher());
            BountyCompleteCommand.register(event.getDispatcher());
            VaultPylonCommand.register(event.getDispatcher());
            GodAltarCompleteCommand.register(event.getDispatcher());
            VaultAltarCompleteCommand.register(event.getDispatcher());
            ClearCommand.register(event.getDispatcher());
            KickCommand.register(event.getDispatcher());
            VaultPermanentTimeCommand.register(event.getDispatcher());
            ProficiencyCommand.register(event.getDispatcher());
            ModelUnlockCommand.register(event.getDispatcher());
            TrinketUnlockCommand.register(event.getDispatcher());
            AlchemyUnlockCommand.register(event.getDispatcher());
            WorkbenchUnlockCommand.register(event.getDispatcher());
            LocateAngelBlockCommand.register(event.getDispatcher());
        }
    }


    public static Configuration CONFIGURATION;

    /**
     * The logger for this mod.
     */
    public static final Logger LOGGER = LogUtils.getLogger();
}
