package hub.guzio.lvn.integrations.mixin;

import com.vodmordia.railwaysuntold.worldgen.namegen.StationNameGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(StationNameGenerator.class)
interface RutAccessor {
    @Accessor("usedNames")
    Set<String> getUsedNames();

    @Contract(pure = true)
    @Accessor("ADJECTIVES")
    static @NotNull String @NotNull [] getADJECTIVES() { return null; /*It's guarantted-not-null in the source class, so the annotations still make sense.*/ }
}