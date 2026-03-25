package hub.guzio.lvn;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue PADDING = BUILDER
            .comment("How many extra chunks in every direction should a village claim as its own territory?")
            .defineInRange("padding", 3, 1, 5);

    public static final ModConfigSpec.BooleanValue DO_VILLAGELIKE = BUILDER
            .comment("Should „village-like” structures (ie. those that generally have some humanoid-ish inhabitants and don't seem like some ancient ruins, such as Woodland Mansions or Bastions) also be given names, or do we only assign names to actual villages? Will look for structures in both #minecraft:village and #lvn:villagelike tags if TRUE, only fot #minecraft:village if false.")
            .define("do_villagelike", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}