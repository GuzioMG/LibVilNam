package hub.guzio.lvn;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class API {
    private final int Padding;
    private final Logger lg;

    public API(int padding, Logger logger){
        Padding = padding;
        lg = logger;
        lg.info("[API/<init>] API constructed.");
    }

    public boolean testForVillage(String id, RegistryAccess in) {
        try { return testForVillage(ResourceLocation.parse(id), in); }
        catch (ResourceLocationException e) {
            lg.error("[API/testForVillage] Tested for a malformatted structure ID:", e);
            return false;
        }
    }

    public boolean testForVillage(ResourceLocation id, RegistryAccess in) {
        var registry = in.registry(Registries.STRUCTURE);
        if (registry.isEmpty()){
            lg.warn("[API/testForVillage] Won't find anything; the registry is empty!");
            return false;
        }

        if (testForVillage(id, registry.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "village"))) return true;
        else return testForVillage(id, registry.get(), ResourceLocation.fromNamespaceAndPath("lvn", "villagelike"));
    }

    public boolean testForVillage(ResourceLocation id, Registry<Structure> in, ResourceLocation among) {
        var searchspace = in.getTag(TagKey.create(in.key(), among));

        if (searchspace.isPresent()) {
            for (var holder : searchspace.get()) if (holder.is(id)) return true;
        }
        else lg.warn("[API/testForVillage] Won't find anything; the searchspace is empty!");

        return false;
    }
}
