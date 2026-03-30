package hub.guzio.lvn.internal;

import hub.guzio.lvn.API;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Optional;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Main.ID)
public class Main {
    public static final String ID = "lvn";
    private static Optional<Main> INSTANCE = Optional.empty();

    public final Logger L;
    private Optional<StatefulAPI> API;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Main(IEventBus modEventBus, ModContainer modContainer) {
        L = LogUtils.getLogger();
        L.info("[Main/_] Constructing LibVilNam...");
        if (INSTANCE.isPresent()) throw new IllegalStateException("Attempted to re-initialize an already-started LibVilNam!");

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        INSTANCE = Optional.of(this);
        L.info("[Main/_] Mod constructed.");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        L.info("[Main/commonSetup] Initializing LibLilNam API...");
        this.API = Optional.of(new StatefulAPI(Config.PADDING.getAsInt(), Config.DO_VILLAGELIKE.getAsBoolean(), Config.DATASET.get(), Config.LANG.get(), L));
        L.info("[Main/commonSetup] Ready to name some villages!");
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