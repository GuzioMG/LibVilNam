package hub.guzio.lvn;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record VillageNotifyResult(BoundingBox computed, String name){}