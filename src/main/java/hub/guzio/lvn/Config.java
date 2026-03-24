package hub.guzio.lvn;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue PADDING = BUILDER
            .comment("How many extra chunks in every direction should a village claim as its own territory")
            .defineInRange("padding", 3, 1, 5);

    static final ModConfigSpec SPEC = BUILDER.build();
}