package hub.guzio.lvn;

import com.ibm.icu.util.ULocale;
import de.tfelix.namegen.MarkovChain;
import de.tfelix.namegen.model.ModelBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Random;

public class API {
    public static final int CHUNK = 16;
    public static final int WORLDHEIGHT = 2032;

    public final boolean doVillagelike;
    public final Locale lang;
    public final int expansion;
    public final MarkovChain<Random> markov;

    private final Logger lg;
    private final MarkovChain<Random> markov;
    private final Locale lang;

    public API(int padding, boolean doVillagelike, @NotNull String dataset, @NotNull String langCode, @NotNull Logger logger) {
        logger.info("[API/<init>] Setting simple params...");
        this.doVillagelike = doVillagelike;
        lg = logger;
        expansion = CHUNK*padding;
        villages = initialState;

        logger.info("[API/<init>] Computing language data...");
        var lang = ULocale.forLanguageTag(langCode);
        this.lang = lang.toLocale();

        logger.info("[API/<init>] Building a new Markov Chain...");
        markov = new MarkovChain<>(new Random(), new ModelBuilder<>(3, 0, 0, lang, logger).from(dataset).build());

        lg.info("[API/<init>] API constructed!");
    }

    public @NotNull Village placeVillage(@NotNull ResourceLocation villageTypeId, @NotNull BoundingBox locationXYZ, @NotNull ResourceLocation locationDimensionId) {

        return new Village(normalize(markov.toString()), villageTypeId, normalize(locationXYZ), locationDimensionId);
    }

    public boolean testForVillage(@NotNull ResourceLocation id, @NotNull RegistryAccess in) {
        var registry = in.registry(Registries.STRUCTURE);
        if (registry.isEmpty()){
            lg.warn("[API/testForVillage] Won't find anything; the registry is empty!");
            return false;
        }

        if (testForStructure(id, registry.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "village"))) return true;
        else return doVillagelike &&testForStructure(id, registry.get(), ResourceLocation.fromNamespaceAndPath("lvn", "villagelike"));
    }

    public boolean testForStructure(@NotNull ResourceLocation structureId, @NotNull Registry<Structure> in, @NotNull ResourceLocation tagId) {
        var searchspace = in.getTag(TagKey.create(in.key(), tagId));

        if (searchspace.isPresent()) {
            for (var holder : searchspace.get()) if (holder.is(structureId)) return true;
        }
        else lg.warn("[API/testForStructure] Won't find anything; the searchspace is empty!");

        return false;
    }

    public @NotNull String normalize(@NotNull String string){
        return string.substring(0,1).toUpperCase(lang) + string.substring(1).toLowerCase(lang);
    }

    public @NotNull BoundingBox normalize(@NotNull BoundingBox box) {
        return new BoundingBox(decrementUntilDivisible(CHUNK, box.minX())-expansion, -WORLDHEIGHT, decrementUntilDivisible(CHUNK, box.minZ())-expansion,   incrementUntilDivisible(CHUNK, box.maxX())+expansion, WORLDHEIGHT, incrementUntilDivisible(CHUNK, box.maxZ())+expansion);
    }

    public static int incrementUntilDivisible(int by, int x){
        var modulo = x % by;
        var backshift = 0;
        if(x < 0) backshift = 16;
        return x + 16-(modulo==0 ? 16:modulo) - 1-backshift; //The ternary is needed to make sure that we don't grow an extra chunk by going x+16-0=x+16 when already at a chunk border, and instead we go x+16-16=x+0=x
    }

    public static int decrementUntilDivisible(int by, int x){
        var backshift = 0;
        if(x < 0) backshift = 16;
        var modulo = x % by;
        return x-modulo-backshift;
    }
}