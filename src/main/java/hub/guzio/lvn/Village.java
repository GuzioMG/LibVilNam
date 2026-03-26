package hub.guzio.lvn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;

public record Village(@NotNull String name, @NotNull ResourceLocation villageTypeId, @NotNull BoundingBox locationXYZ, @NotNull ResourceLocation locationDimensionId){}