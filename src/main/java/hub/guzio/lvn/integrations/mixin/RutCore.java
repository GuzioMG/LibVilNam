package hub.guzio.lvn.integrations.mixin;
/*
import com.vodmordia.railwaysuntold.worldgen.namegen.BiomeWordPools;
import com.vodmordia.railwaysuntold.worldgen.namegen.StationNameGenerator;
import hub.guzio.lvn.internal.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StationNameGenerator.class)
abstract class RutCore extends SavedData {
    @Inject(at = @At("HEAD"), method="generateName", cancellable = true)
    public synchronized void generateName(ServerLevel level, BlockPos pos, RandomSource rand, @NotNull CallbackInfoReturnable<String> cir) {
        var village = Main.i().getAPI().getVillageByPresenceOrProximity(pos, level.dimension().location(), 500, false);
        var baseName = "";

        if (village.isPresent()) baseName = village.get().name();
        else {
            Holder<Biome> biomeHolder = level.getBiome(pos);
            String biomeName = biomeHolder.unwrapKey().map((key) -> key.location().getPath()).orElse("plains");
            String[] biomeWords = BiomeWordPools.getWordsForBiome(biomeName);
            String adjective = RutAccessor.getADJECTIVES()[rand.nextInt(RutAccessor.getADJECTIVES().length)];
            String biomeWord = biomeWords[rand.nextInt(biomeWords.length)];

            baseName = adjective + " " + biomeWord + " [FALLBACK]";
        }

        String finalName = resolveDuplicate(baseName);
        ((RutAccessor) this).getUsedNames().add(finalName);
        super.setDirty();
        cir.setReturnValue(finalName);
    }

    @Inject(at = @At("HEAD"), method="resolveDuplicate", cancellable = true)
    private void resolveDuplicateMixin(String name, CallbackInfoReturnable<String> cir) {
        var nameStripped = name;
        var testedName = nameStripped+" Central Station";

        if (nameStripped.endsWith(" [FALLBACK]")){
            nameStripped = name.substring(0, name.length()-11);
            testedName = nameStripped+" Station I";
        }

        if (!((RutAccessor) this).getUsedNames().contains(testedName)) {
            cir.setReturnValue(testedName);
        } else {
            int i = 2;

            String tryName;
            do {
                tryName = nameStripped + " Station " + toRoman(i);
                ++i;
            } while(((RutAccessor) this).getUsedNames().contains(tryName));

            cir.setReturnValue(tryName);
        }
    }

    @Shadow
    private String resolveDuplicate(String name) { return null; }

    @Shadow
    private static String toRoman(int number) { return null; }
}*/