package hub.guzio.lvn.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import hub.guzio.lvn.API;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

record Commands(CommandDispatcher<CommandSourceStack> dispatcher, Logger lg, API api) {
    public void getVillage() {
        lg.info("[Commands/getVillage] Adding /lvn-getvillage...");
        dispatcher.register(net.minecraft.commands.Commands.literal("lvn-getvillage").then(net.minecraft.commands.Commands.argument("radius", IntegerArgumentType.integer()).executes(context -> {
            var sender = context.getSource();
            var radius = (double) context.getArgument("radius", Integer.class);
            var dimensionId = sender.getLevel().dimension().location();
            var coords = new BlockPos((int) sender.getPosition().x, (int) sender.getPosition().y, (int) sender.getPosition().z);
            var getVillageAtAllCost = true;

            sender.sendSystemMessage(Component.literal("--- VILLAGE AT "+coords+" ---"));
            sender.sendSystemMessage(Component.literal(" *  You are currently inside of: "+api.getVillageByPresence(coords, dimensionId, getVillageAtAllCost).orElseThrow()+". This is the village used for the /lvn-rename command."));
            sender.sendSystemMessage(Component.literal(" *  You are closest to (in a "+radius+" block radius): "+api.getVillageByProximity(coords, dimensionId, radius, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal(" *  ...So, combined, you are at (going by presence-first): "+api.getVillageByPresenceOrProximity(coords, dimensionId, radius, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal(" *  ...Or, combined, you are at (going by proximity-first): "+api.getVillageByProximityOrPresence(coords, dimensionId, radius, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal("/ If any villages above report type lvn:fake, then no village was actually found, and a placeholder name was returned instead /"));

            return 0;
        })));
    }
}