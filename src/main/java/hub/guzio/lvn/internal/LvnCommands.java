package hub.guzio.lvn.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import hub.guzio.lvn.API;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

record LvnCommands(CommandDispatcher<CommandSourceStack> dispatcher, Logger lg, API api) {
    public void getVillage() {
        var NAME = "lvn-getvillage";
        lg.info("[LvnCommands/getVillage] Adding /{}...", NAME);
        dispatcher.register(Commands.literal(NAME).then(Commands.argument("radius", IntegerArgumentType.integer()).executes(context -> {
            var sender = context.getSource();
            var radius = (double) context.getArgument("radius", Integer.class);
            var dimensionId = sender.getLevel().dimension().location();
            var rawCoords = sender.getPosition();
            var coords = new BlockPos((int) rawCoords.x, (int) rawCoords.y, (int) rawCoords.z);
            var getVillageAtAllCost = true;

            sender.sendSystemMessage(Component.literal("--- VILLAGE AT "+coords+" ---"));
            sender.sendSystemMessage(Component.literal(" *  You are currently inside of (this is the village used for the /lvn-rename command): "+api.getVillageByPresence(coords, dimensionId, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal(" *  You are closest to (in a "+radius+" block radius): "+api.getVillageByProximity(coords, dimensionId, radius, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal(" *  ...So, combined, you are at (going by presence-first): "+api.getVillageByPresenceOrProximity(coords, dimensionId, radius, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal(" *  ...Or, combined, you are at (going by proximity-first): "+api.getVillageByProximityOrPresence(coords, dimensionId, radius, getVillageAtAllCost).orElseThrow()));
            sender.sendSystemMessage(Component.literal("/ If any villages above report type lvn:fake, then no village was actually found, and a placeholder name was returned instead /"));

            lg.info("[LvnCommands/getVillage] {} executed {} successfully!", sender.getTextName(), NAME);
            return 0;
        })));
    }
    
    public void renameVillage() {
        var NAME = "lvn-rename";
        lg.info("[LvnCommands/renameVillage] Adding /{}...", NAME);
        dispatcher.register(Commands.literal(NAME).then(Commands.argument("name", StringArgumentType.greedyString()).executes(context -> {
            var sender = context.getSource();
            var radius = context.getArgument("name", String.class);
            var dimensionId = sender.getLevel().dimension().location();
            var coords = new BlockPos((int) sender.getPosition().x, (int) sender.getPosition().y, (int) sender.getPosition().z);
            var target = api.getVillageByPresence(coords, dimensionId, false);
            
            if (target.isEmpty()) sender.sendSystemMessage(Component.literal("You need to be INSIDE a village to rename it! If you run /lvn-getvillage, that'd be the 1st village shown."));
            else sender.sendSystemMessage(Component.literal("Renamed "+target.get()+" to "+api.updateVillageName(target.get(), radius)));

            lg.info("[LvnCommands/renameVillage] {} executed {} successfully!", sender.getTextName(), NAME);
            return 0;
        })));
    }
}