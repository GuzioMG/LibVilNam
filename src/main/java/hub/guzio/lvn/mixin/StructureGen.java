package hub.guzio.lvn.mixin;

import hub.guzio.lvn.internal.Main;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ChunkGenerator.class)
public class StructureGen {
    @Inject(at = @At("HEAD"), method = "tryGenerateStructure")
    private void tryGenerateStructure(@NotNull StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
        //Before even touching anything, let's make sure that the structure we're trying to generate is a village(like) in the 1st place
        var structure = structureSelectionEntry.structure();
        var structureId = ResourceLocation.fromNamespaceAndPath("lvn", "null");
        var instance = Main.i();
        try { structureId = Objects.requireNonNull(structure.getKey()).location(); }
            catch (NullPointerException e) { instance.L.error("[mixin:ChunkGenerator/tryGenerateStructure] Got passed in an id-less structure {} (substituted for {}), error details:\n{}", structure, structureId, e); }
        if(!instance.getAPI().testForVillage(structureId, registryAccess)) return;

        //The following code more-or-less mirrors the behavior of tryGenerateStructure and Structure.generate in a single pass, and without actually generating anything
        var structureContent = structure.value();
        var that = (ChunkGenerator) (Object) this;
        var biomes = structureContent.biomes();
        if (Objects.isNull(biomes)) return;
        var point = structureContent.findValidGenerationPoint(new Structure.GenerationContext(registryAccess, that, getBiomeSource(), randomState, structureTemplateManager, l, chunkPos, chunkAccess, biomes::contains /*:: gets the function itself as lambda*/));
        if (point.isEmpty()) return;
        var worldStructure = new StructureStart(structureContent, chunkPos, fetchReferences(structureManager, chunkAccess, sectionPos, structureContent), point.get().getPiecesBuilder().build());
        if (!worldStructure.isValid()) return;

        //Custom code
        var level = ((StructureManagerLevelAccessor) structureManager).getLevelAccessor();
        var dim = ResourceLocation.fromNamespaceAndPath("lvn", "unknown");
        if (level instanceof Level) dim = ((Level) level).dimension().location();
        var village = instance.getAPI().placeVillage(structureId, worldStructure.getBoundingBox(), dim);
        instance.L.info("[mixin:ChunkGenerator/tryGenerateStructure] Generating a new {} called „{}” at {} (normalized to {}) in {}", structureId, village.name(), worldStructure.getBoundingBox(), village.locationXYZ(), dim);
    }

    @Shadow
    private static int fetchReferences(StructureManager structureManager, ChunkAccess chunkAccess, SectionPos sectionPos, Structure structure) { return 0; }

    @Shadow
    public BiomeSource getBiomeSource() { return null; }
}