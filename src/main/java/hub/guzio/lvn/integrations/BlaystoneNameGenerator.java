package hub.guzio.lvn.integrations;

import hub.guzio.lvn.internal.Main;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.worldgen.namegen.NameGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlaystoneNameGenerator implements NameGenerator {
    private final NameGenerator fallback;

    public BlaystoneNameGenerator(NameGenerator fallback) {
        this.fallback = fallback;
    }

    @Override
    public @NotNull Optional<Component> generateName(LevelAccessor levelAccessor, @NotNull Waystone waystone, RandomSource randomSource) {
        var village = Main.i().getAPI().getVillageByPresenceOrProximity(waystone.getPos(), waystone.getDimension().location(), 300, false);
        if (village.isPresent()) return Optional.of(Component.literal(village.get().name()));
        else return fallback.generateName(levelAccessor, waystone, randomSource);
    }
}