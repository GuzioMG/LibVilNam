package hub.guzio.lvn;

import com.ibm.icu.util.ULocale;
import de.tfelix.namegen.MarkovChain;
import de.tfelix.namegen.model.ModelBuilder;
import hub.guzio.lvn.internal.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class API {
    public static final int CHUNK = 16;
    public static final int WORLDHEIGHT = 2032;

    public final boolean doVillagelike;
    public final Locale lang;
    public final int expansion;
    public final MarkovChain<Random> markov;

    private final Logger lg;
    @NotNull protected Map<ResourceLocation, List<Village>> villages;

    public API(int padding, boolean doVillagelike, @NotNull String dataset, @NotNull String langCode, @NotNull Logger logger) {
        logger.info("[API/<init>] Setting simple params...");
        this.doVillagelike = doVillagelike;
        lg = logger;
        expansion = CHUNK*padding;

        logger.info("[API/<init>] Computing language data...");
        var lang = ULocale.forLanguageTag(langCode);
        this.lang = lang.toLocale();

        logger.info("[API/<init>] Building a new Markov Chain...");
        markov = new MarkovChain<>(new Random(), new ModelBuilder<>(3, 0, 0, lang, logger).from(dataset).build());

        logger.info("[API/<init>] Loading up an initial state...");
        villages = new ConcurrentHashMap<>(); //Has to be concurrent to account for mods like Async, DimThread, or C2ME, which may try to generate multiple villages at once on different threads.

        lg.info("[API/<init>] API constructed!");
    }


    public @NotNull Village placeVillage(@NotNull ResourceLocation villageTypeId, @NotNull BoundingBox locationXYZ, @NotNull ResourceLocation locationDimensionId) {
        return placeVillage(new Village(markov.toString(), villageTypeId, locationXYZ, locationDimensionId));
    }

    public @NotNull Village placeVillage(@NotNull Village village) {
        //Loading data
        var dim = village.locationDimensionId();
        var villages = getLiveVillagesInDimension(dim);
        var selfBounds = normalize(village.locationXYZ());

        //Computing bounds
        var otherBounds = new BoundingBox[villages.size()];
        var index = 0;
        for (var knownVillage : villages) {
            otherBounds[index] = knownVillage.locationXYZ();
            index++;
        }
        var combinedBounds = add(selfBounds, otherBounds);
        var villagesToConquer = findIntersectingVillages(combinedBounds, villages);

        if(villagesToConquer.isEmpty()){
            //The BLISSFUL, WONDERFULLY simple case: Our village is completely brand-new; we can just get it normalized (either from scratch (name) or from pre-computed values (bounds)), register it and return it without worrying.
            var newVillage = new Village(normalize(village.name()), village.villageTypeId(), selfBounds, dim);
            villages.add(newVillage);
            return newVillage;
        }
        else if(villagesToConquer.size() == 1){
            //Moderately annoying: we fold our new village into an existing village that it intersects.
            var abortedVillage = new Village(normalize(village.name()), village.villageTypeId(), selfBounds, dim);
            var oldVillage = villagesToConquer.getFirst();
            var newVillage = updateVillageSize(oldVillage, combinedBounds);
            if (!Objects.equals(abortedVillage.villageTypeId(), oldVillage.villageTypeId())) newVillage = updateVillageType(newVillage, ResourceLocation.fromNamespaceAndPath(Main.ID, "mixed"));
            lg.info("[API/placeVillage] Placing a new {} on top on an already existing {} -> Instead, the old village will eat it and from a new {}", abortedVillage, oldVillage, newVillage);
            return newVillage;
        }
        else{
            //Time to cry: got a Clusterfuck Area [TM]
            throw new UnsupportedOperationException("...Yea, I can't be bothered with this today; sorry."); //TODO: implement
        }
    }

    public @NotNull List<@NotNull Village> findIntersectingVillages(@NotNull BoundingBox with, @NotNull ResourceLocation dimensionId) {
        return findIntersectingVillages(with, getLiveVillagesInDimension(dimensionId));
    }

    private @NotNull List<@NotNull Village> getLiveVillagesInDimension(@NotNull ResourceLocation dimensionId) {
        if (villages.containsKey(dimensionId)) return villages.get(dimensionId);

        lg.info("[API/getLiveVillagesInDimension] New dimension discovered: {}", dimensionId);
        var list = Collections.synchronizedList(new ArrayList<@NotNull Village>()); //Has to be synchronized to account for mods like Async or C2ME, which may try to generate multiple villages at once on different threads.
        villages.put(dimensionId, list);
        return list;
    }

    public @NotNull Village @NotNull [] getCurrentVillagesInDimension(@NotNull ResourceLocation dimensionId) {
        var list = getLiveVillagesInDimension(dimensionId);
        return list.toArray(new Village[list.size()]);
    }

    public @NotNull Optional<Village> getVillageByPresence(BlockPos coords, ResourceLocation dimensionId, boolean getNameAtAllCost){
        var bounds = BoundingBox.encapsulatingPositions(List.of(coords)).orElseThrow();
        var found = findIntersectingVillages(bounds, dimensionId);

        if (found.isEmpty()){
            if (getNameAtAllCost) return Optional.of(getFakeVillageWithRandomName(Optional.of(bounds), Optional.of(dimensionId)));
            return Optional.empty();
        }
        else if(found.size() > 1) throw new IllegalStateException("Somehow, multiple villages were present at a given coordinate. Overlapping should not be possible; your state must be cooked.");

        return Optional.of(found.getFirst());
    }

    public @NotNull Optional<Village> getVillageByProximity(BlockPos coords, ResourceLocation dimensionId, double radius, boolean getNameAtAllCost){
        Optional<Village> result = Optional.empty();
        var dist = Double.MAX_VALUE;
        for (var village : getCurrentVillagesInDimension(dimensionId)){
            if (result.isEmpty()){
                result = Optional.of(village);
                continue;
            }

            var newDist = result.get().locationXYZ().getCenter().distSqr(coords);
            if(newDist < dist && (radius == 0 || newDist >= Math.abs(radius))){
                dist = newDist;
                result = Optional.of(village);
            }
        }

        if(!getNameAtAllCost || result.isPresent()) return result;
        else return Optional.of(getFakeVillageWithRandomName(Optional.empty(), Optional.of(dimensionId)));
    }

    public @NotNull Optional<Village> getVillageByPresenceOrProximity(BlockPos coords, ResourceLocation dimensionId, double radius, boolean getNameAtAllCost){
        var byPresence = getVillageByPresence(coords, dimensionId, false);
        if (byPresence.isPresent()) return byPresence;
        else return getVillageByProximity(coords, dimensionId, radius, getNameAtAllCost);
    }

    public @NotNull Optional<Village> getVillageByProximityOrPresence(BlockPos coords, ResourceLocation dimensionId, double radius, boolean getNameAtAllCost){
        var byProximity = getVillageByProximity(coords, dimensionId, radius, false);
        if (byProximity.isPresent()) return byProximity;
        else return getVillageByPresence(coords, dimensionId, getNameAtAllCost);
    }

    public @NotNull Village updateVillageSize(@NotNull Village old, @NotNull BoundingBox New){
        var dim = old.locationDimensionId();
        var newNormalized = normalize(New);
        var villages = getLiveVillagesInDimension(dim);
        var intersects = findIntersectingVillages(newNormalized, villages);
        var selfIndex = villages.indexOf(old);
        var target = new Village(old.name(), old.villageTypeId(), newNormalized, dim);

        if (intersects.isEmpty() && selfIndex != -1) lg.warn("[API/updateVillageSize] {} is getting moved to a completely new location ({}). This may be intentional, but more likely indicates that some mod royally f'd up its math.", old, newNormalized); //The actual move is taken care of below.
        else if (intersects.isEmpty()) {
            lg.warn("[API/updateVillageSize] There was an attempt to move {}, but it doesn't seem to exist at all - placing it down from scratch as {} instead...", old, target);
            return placeVillage(target);
        }
        else if (intersects.size() == 1 && (selfIndex == -1 || !Objects.equals(intersects.getFirst(), old))) {
            lg.error("[API/updateVillageSize] Tried to re-size/re-position {} to {} (or tried to create it at that location), but that's on top on an already existing {}! Nothing will happen; returning back the original unmodified (and potentially un-created) village. This MAY leave some mod in a broken state, if they're stupid enough to not check return values, but that's their problem, not LibLilNam's.", old, newNormalized, intersects.getFirst());
            return old;
        }
      //else if intersects 1, then it must be intersecting itself, so it's a normal resize; taken care of below.
        else if (intersects.size() > 1){
            lg.error("[API/updateVillageSize] Tried to re-size/re-position {} to {} (or tried to create it at that location), but that's on top on {} already existing villages! Nothing will happen; returning back the original unmodified (and potentially un-created) village. This MAY leave some mod in a broken state, if they're stupid enough to not check return values, but that's their problem, not LibLilNam's.", old, newNormalized, intersects.size());
            return old;
        }

        updateVillageUNSAFELY(old, Optional.of(target), villages, selfIndex, List.of());
        return target;
    }

    public @NotNull Village updateVillageName(@NotNull Village old, @NotNull String New){
        var dim = old.locationDimensionId();
        var villages = getLiveVillagesInDimension(dim);
        var selfIndex = villages.indexOf(old);
        var target = new Village(New /*No name normalization here - someone may have called this func exactly because they want a fancier name than what the default casing permits.*/, old.villageTypeId(), old.locationXYZ(), dim);

        if (selfIndex == -1) {
            lg.warn("[API/updateVillageName] There was an attempt to rename {}, but it doesn't seem to exist at all - placing it down from scratch as {} instead (although the name WILL get normalized)...", old, target);
            return placeVillage(target);
        }

        updateVillageUNSAFELY(old, Optional.of(target), villages, selfIndex, List.of());
        return target;
    }

    public @NotNull Village updateVillageType(@NotNull Village old, @NotNull ResourceLocation New){
        var dim = old.locationDimensionId();
        var villages = getLiveVillagesInDimension(dim);
        var selfIndex = villages.indexOf(old);
        var target = new Village(old.name(), New, old.locationXYZ(), dim);

        if (selfIndex == -1) {
            lg.warn("[API/updateVillageType] There was an attempt to change the type of {}, but it doesn't seem to exist at all - placing it down from scratch as {} instead...", old, target);
            return placeVillage(target);
        }

        updateVillageUNSAFELY(old, Optional.of(target), villages, selfIndex, List.of());
        return target;
    }

    /**
     * One should NEVER, EVER call this method without extensive safety checks first. Few pitfalls to avoid:
     * <p> * It CANNOT be used to move villages between dimensions; such functionality is completely unsupported by LibVilNam and this method can't help with that. Any attempts to do so will leave this LibVilNam's session in AN EXTREMELY BROKEN STATE, where the village is placed in one dimension, but it thinks that it's in a different one. This will utterly break any subsequent operations that may in any way involve that village (such as moves, merges (so new village creation, btw), disbands, etc.) and may lead to villages being unjustly „eaten” or the opposite (duplicated). Do NOT move between dimensions.
     * <p> * The village must already be placed, the inputted list MUST be the one on which it's placed, and the index MUST be valid (not -1 - otherwise Java's ArrayList crashes, taking down the game with it) and represent the village's actual position in said list.
     * <p> * If relocating/resizing, the village MUST NOT intersect any other village, otherwise LibVilNam's state will be broken for this world >PERMANENTLY< (with similar results to cross-dimensional relocation)!!! (or unless manually-fixed by the player in their world-files)
     * <p> * The merge param does NOT actually merge anything; it's only used for controlling TODO: event notification
     *
     * @param old Old village state
     * @param New New village state if present; if empty, then the village gets removed
     * @param at ...
     * @param in ...
     * @param merge If this update happens as part of a merge, this list should contain all villages that are being merged together. Should be empty otherwise.
     */
    public void updateVillageUNSAFELY(@NotNull Village old, @NotNull Optional<Village> New, @NotNull List<Village> in, int at, @NotNull List<@NotNull Village> merge){
        if(New.isPresent()) in.set(at, New.get());
        else in.remove(old);
    }


    public boolean testForVillage(@NotNull ResourceLocation id, @NotNull RegistryAccess in) {
        var registry = in.registry(Registries.STRUCTURE);
        if (registry.isEmpty()){
            lg.warn("[API/testForVillage] Won't find anything; the registry is empty!");
            return false;
        }

        if (testForStructure(id, registry.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "village"))) return true;
        else return doVillagelike &&testForStructure(id, registry.get(), ResourceLocation.fromNamespaceAndPath(Main.ID, "villagelike"));
    }

    public boolean testForStructure(@NotNull ResourceLocation structureId, @NotNull Registry<Structure> in, @NotNull ResourceLocation tagId) {
        var searchspace = in.getTag(TagKey.create(in.key(), tagId));

        if (searchspace.isPresent()) {
            for (var holder : searchspace.get()) if (holder.is(structureId)) return true;
        }
        else lg.warn("[API/testForStructure] Won't find anything; the searchspace is empty!");

        return false;
    }

    public @NotNull Village getFakeVillageWithRandomName(@NotNull Optional<BoundingBox> at, @NotNull Optional<ResourceLocation> inDimensionId){
        var dim = ResourceLocation.fromNamespaceAndPath(Main.ID, "unknown");
        var bounds = BoundingBox.infinite();

        if (inDimensionId.isPresent()) dim = inDimensionId.get();
        if (at.isPresent()) bounds = at.get();


        return new Village(markov.toString(), ResourceLocation.fromNamespaceAndPath(Main.ID, "fake"), bounds, dim);
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

    public static @NotNull List<@NotNull Village> findIntersectingVillages(@NotNull BoundingBox with, @NotNull List<@NotNull Village> among) {
        var found = new ArrayList<Village>(among.size());
        for (var village : among) if (village.locationXYZ().intersects(with)) found.add(village);
        return found;
    }

    public static @NotNull BoundingBox add(@NotNull BoundingBox boundingBox, @NotNull BoundingBox @NotNull [] toSystem){
        var overallBox = boundingBox;
        for (var processedBox : toSystem) if(processedBox.intersects(overallBox)) overallBox = BoundingBox.encapsulatingBoxes(List.of(overallBox, processedBox)).orElseThrow();

        if(Objects.equals(overallBox, boundingBox)) return boundingBox;
        else return add(overallBox, toSystem); //Our new box may actually overlap with some boxes that it wasn't overlapping with by the time they were being checked (eg. our box was the upper-right corner, and it slightly overlapped with the lower-left corner, and the new resulting box overlaps with the even-lower-right corner, but that corner was checked before the lower-left one, so it was thought to be non-overlapping at the time) - re-doing the checks in case the box grew lets us catch that.
    }
}