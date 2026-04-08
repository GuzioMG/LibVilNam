package hub.guzio.lvn.internal;

import com.mojang.brigadier.CommandDispatcher;
import hub.guzio.lvn.API;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

import java.util.Optional;

public class Main implements ModInitializer {
    public static final String ID = "lvn";
    private static Optional<Main> INSTANCE = Optional.empty();

    public final Logger L;
    private Optional<StatefulAPI> API;

    public Main() {
        L = LoggerFactory.getLogger("LibVilNam");
        L.info("[Main/_] Constructing LibVilNam...");
        if (INSTANCE.isPresent()) throw new IllegalStateException("Attempted to re-initialize an already-started LibVilNam!");

        /*/ Register events
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::commandSetup);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);*/

        INSTANCE = Optional.of(this);
        L.info("[Main/_] Mod constructed.");
    }

    @Override
    public void onInitialize() {
        L.info("[Main/commonSetup] Registering command event...");
        CommandRegistrationCallback.EVENT.register(((dispatcher, _, _) -> commandSetup(dispatcher)));
        L.info("[Main/commonSetup] Initializing LibLilNam API...");
        //this.API = Optional.of(new StatefulAPI(Config.PADDING.getAsInt(), Config.DO_VILLAGELIKE.getAsBoolean(), Config.DATASET.get(), Config.LANG.get(), L));
        L.info("[Main/commonSetup] Ready to name some villages!");
    }

    private void commandSetup(CommandDispatcher<CommandSourceStack> src) {
        L.info("[Main/commandSetup] Loading commands...");
        var cmd = new LvnCommands(src, L, getAPI());
        cmd.getVillage();
        cmd.renameVillage();
        L.info("[Main/commandSetup] All commands up!");
    }

    public static @NotNull Main i(){
        if (INSTANCE.isEmpty()) throw new IllegalStateException("Attempted to access LibVilNam prior to its construction.");
        else return INSTANCE.get();
    }

    public @NotNull API getAPI(){
        if (API.isEmpty()) throw new IllegalStateException("Attempted to access LibVilNam's API prior to its construction.");
        return API.get();
    }

    public @NotNull SaveState getSaves(){
        if (API.isEmpty()) throw new IllegalStateException("Attempted to access LibVilNam API'S save system prior to the API's construction.");
        return API.get();
    }
}