package hub.guzio.lvn;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class API {
    private final int Padding;
    private final boolean DoVillagelike;
    private final Logger lg;

    public API(int padding, boolean doVillagelike, Logger logger){
        Padding = padding;
        DoVillagelike = doVillagelike;
        lg = logger;
        lg.info("[API/<init>] API constructed.");
    }

    public VillageNotifyResult villageNotify(ResourceLocation villageTypeId, BoundingBox locationXYZ, ResourceLocation locationDimensionId){

    }

    public boolean testForVillage(ResourceLocation id, RegistryAccess in) {
        var registry = in.registry(Registries.STRUCTURE);
        if (registry.isEmpty()){
            lg.warn("[API/testForVillage] Won't find anything; the registry is empty!");
            return false;
        }

        if (testForStructure(id, registry.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "village"))) return true;
        else return DoVillagelike&&testForStructure(id, registry.get(), ResourceLocation.fromNamespaceAndPath("lvn", "villagelike"));
    }

    public boolean testForStructure(ResourceLocation structureId, Registry<Structure> in, ResourceLocation tagId) {
        var searchspace = in.getTag(TagKey.create(in.key(), tagId));

        if (searchspace.isPresent()) {
            for (var holder : searchspace.get()) if (holder.is(structureId)) return true;
        }
        else lg.warn("[API/testForStructure] Won't find anything; the searchspace is empty!");

        return false;
    }
}