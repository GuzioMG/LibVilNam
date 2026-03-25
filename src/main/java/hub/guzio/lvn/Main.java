package hub.guzio.lvn;

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
    public final Logger LOGGER;
    private API API;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Main(IEventBus modEventBus, ModContainer modContainer) {
        if (INSTANCE.isPresent()) throw new IllegalStateException("Attempted to re-initialize an already-started LibVilNam.");

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        INSTANCE = Optional.of(this);
        LOGGER = LogUtils.getLogger();
    }

    public API getAPI(){
        return API;
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        this.API = new API(Config.PADDING.getAsInt(), LOGGER);
    }

    public static Main i(){
        if (INSTANCE.isEmpty()) throw new IllegalStateException("Attempted to access LibVilNam prior to its initialization.");
        else return INSTANCE.get();
    }

    public static Logger lg(){
        return i().LOGGER;
    }
}