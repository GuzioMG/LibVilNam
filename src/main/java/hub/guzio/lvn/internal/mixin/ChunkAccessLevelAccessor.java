package hub.guzio.lvn.internal.mixin;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkAccess.class)
interface ChunkAccessLevelAccessor {
    @Accessor("levelHeightAccessor")
    LevelHeightAccessor getLevelHeightAccessor();
}