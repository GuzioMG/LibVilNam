package hub.guzio.lvn.mixin;

import hub.guzio.lvn.Main;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(net.minecraft.world.level.chunk.ChunkGenerator.class)
public class StructureGen {
    @Inject(at = @At("HEAD"), method = "tryGenerateStructure")
    private void tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
        //The following code more-or-less mirrors the behavior of tryGenerateStructure and Structure.generate in a single pass, and without actually generating anything
        var structure = structureSelectionEntry.structure().value();
        var that = (ChunkGenerator) (Object) this;
        var biomes = structure.biomes();
        if (Objects.isNull(biomes)) return;
        var point = structure.findValidGenerationPoint(new Structure.GenerationContext(registryAccess, that, getBiomeSource(), randomState, structureTemplateManager, l, chunkPos, chunkAccess, biomes::contains /*:: gets the function itself as lambda*/));
        if (point.isEmpty()) return;
        var worldStructure = new StructureStart(structure, chunkPos, fetchReferences(structureManager, chunkAccess, sectionPos, structure), point.get().getPiecesBuilder().build());
        if (!worldStructure.isValid()) return;

        //Custom code
        var level = ((StructureManagerLevelAccessor) structureManager).getLevelAccessor();
        var dim = ResourceLocation.fromNamespaceAndPath("minetrix", "unknown");
        if (level instanceof Level) dim = ((Level) level).dimension().location();
        Main.LOGGER.info("Generating a new {} at {} in {}", structureSelectionEntry.structure().getRegisteredName(), worldStructure.getBoundingBox(), dim);
    }

    @Shadow
    private static int fetchReferences(StructureManager structureManager, ChunkAccess chunkAccess, SectionPos sectionPos, Structure structure) {
        return 0;
    }

    @Shadow
    public BiomeSource getBiomeSource() {
        return null;
    }
}