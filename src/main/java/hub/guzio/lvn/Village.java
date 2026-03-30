package hub.guzio.lvn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;

public record Village(@NotNull String name, @NotNull ResourceLocation villageTypeId, @NotNull BoundingBox locationXYZ, @NotNull ResourceLocation locationDimensionId){
    @Override
    public @NotNull String toString() {
        return "Village „"+name+"” (of type "+villageTypeId+") at "+locationXYZ+" (in "+locationDimensionId+")";
    }

    public static Village fromCsvLine(String line, ResourceLocation locationDimensionId){
        var items = line.split(",", 6);
        return new Village(items[5], ResourceLocation.parse(items[0]), new BoundingBox(Integer.parseInt(items[1]), -API.WORLDHEIGHT, Integer.parseInt(items[3]), Integer.parseInt(items[2]), API.WORLDHEIGHT, Integer.parseInt(items[4])), locationDimensionId);
    }

    public String toCsvLine(){
        return villageTypeId+","+locationXYZ.minX()+","+locationXYZ.maxX()+","+locationXYZ.minZ()+","+locationXYZ.maxZ()+","+name;
    }
}