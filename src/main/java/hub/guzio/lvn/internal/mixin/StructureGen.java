package hub.guzio.lvn.internal.mixin;

import hub.guzio.lvn.internal.Main;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(Structure.class)
class StructureGen {
    @Inject(at = @At("RETURN"), method = "generate")
    public void generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, int references, LevelHeightAccessor levelSize, Predicate<Holder<Biome>> validBiome, @NotNull CallbackInfoReturnable<StructureStart> cir) {
        var that = (Structure) (Object) this;
        var structureId = ResourceLocation.fromNamespaceAndPath(Main.ID, "unknown");
        var maybeStructureId = registryAccess.registry(Registries.STRUCTURE).get().getResourceKey(that);
        var instance = Main.i();
        var start = cir.getReturnValue();
        var dim = ResourceLocation.fromNamespaceAndPath(Main.ID, "unknown");

        //General validation
        if (!start.isValid()) return;
        var bounds = start.getBoundingBox(); //Can't call this until we know the start is valid.
        //Structure ID getting
        if (maybeStructureId.isPresent()) structureId = maybeStructureId.get().location();
        else instance.L.warn("[mixin:Structure/generate] Got passed in an id-less structure {} at roughly {}, so it got substituted for {}. You may want to go there to investigate which mod is naughty and places structures without IDs.", that, bounds, structureId);
        //LibVilNam validation
        if(!instance.getAPI().testForVillage(structureId, registryAccess)) return;
        //Dim ID getting - stage one (clearing stupid layers of wrapping from levelSize)
        var level = levelSize;
        if (levelSize instanceof ChunkAccess) level = ((ChunkAccess) level).getLevel();
        if (Objects.isNull(level) /*getLevel() only works if ChunkAccess is an instance of LevelChunk (else null is returned, like here) - will have to get „creative” for other cases (notably, ProtoChunk, which is what's used for /locate)*/) level = ((ChunkAccessLevelAccessor) levelSize).getLevelHeightAccessor();
        //Dim ID getting - stage two (actually turning a level into a dimension)
        if (level instanceof Level /*Takes care of the vast majority of cases*/) dim = ((Level) level).dimension().location();
        else if (level instanceof ServerLevelAccessor /*Takes care of WorldGenRegion (and technically ServerLevel, but that's already handled above)*/) dim = ((ServerLevelAccessor) level).getLevel().dimension().location();
        else /*There technically are some other „special snowflakes” from which level extraction is not possible (like PathNavigationRegion, or it's used directly in below-Bedrock retro-gen), tho they're ULTRA-unlikely to find their way here (eg. PathNavigationRegion is used for entity pathfinding (which is needed way after structure-gen), or you're not gonna have retro-gen on a fresh modpack (which is probably the majority of LibVilNam's use-cases), and even if you install it on your forever world for some reason, you're not gonna have village(like)s generate underground unless you also explicitly add Ancient Cities as village-likes or have a datapack that adds underground villages). Still - this is a (very-)edge-case that needs at least a heads-up to the player. And then there's also the fact that WorldGenRegion apparently deprecates its implementation of ServerLevelAccessor? So that could get awkward in the future...*/ instance.L.warn("[mixin:Structure/generate] We're currently trying to generate a {}, but it was not possible to obtain the associated dimension ID, as the level is of type {}. This structure will be assigned to {}.", structureId, level.getClass(), dim);

        var village = instance.getAPI().placeVillage(structureId, bounds, dim);
        instance.L.info("[mixin:Structure/generate] Generating a new {} called „{}” at {} (normalized to {}) in {}", village.villageTypeId(), village.name(), start.getBoundingBox(), village.locationXYZ(), dim);
    }
}