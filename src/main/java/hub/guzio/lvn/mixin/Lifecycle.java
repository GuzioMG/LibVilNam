package hub.guzio.lvn.mixin;

import hub.guzio.lvn.internal.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(MinecraftServer.class)
public class Lifecycle {
    @Inject(at = @At("HEAD"), method = "createLevels")
    protected void createLevels(ChunkProgressListener listener, CallbackInfo ci) {
        var i = Main.i();
        i.L.info("[mixin:MinecraftServer/createLevels] Got a create request for {}", getWorldPath(LevelResource.ROOT));
        try {
            i.getSaves().load(getWorldPath(new LevelResource("lvn")));
        } catch (IOException e) {
            i.L.error("[mixin:MinecraftServer/createLevels] Could not load LibVilNam data from {} - cannot operate. The game will probably crash now. More info: {}", getWorldPath(LevelResource.ROOT), e);
            throw new RuntimeException(e);
        }
    }

    @Inject(at = @At("HEAD"), method = "saveAllChunks")
    public void saveAllChunks(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        var i = Main.i();
        i.L.info("[mixin:MinecraftServer/saveAllChunks] Got a save request for {}", getWorldPath(LevelResource.ROOT));
        try {
            i.getSaves().save();
        } catch (IOException e) {
            i.L.error("[mixin:MinecraftServer/saveAllChunks] Could not save LibVilNam data for {}. You'll probably have to manually fix your file, or have some missing villages! More info: {}", getWorldPath(LevelResource.ROOT), e);
        }
    }

    @Inject(at = @At("RETURN") /*I prefer injecting at HEAD unless I need the return value, but in this case, I have to make sure that a call to close() happens AFTER the call to save(), which stopServer() calls via saveAllChunks().*/, method = "stopServer")
    public void stopServer(CallbackInfo ci) {
        var i = Main.i();
        i.L.info("[mixin:MinecraftServer/stopServer] Got a close request for {}", getWorldPath(LevelResource.ROOT));
        i.getSaves().close();
    }

    @Shadow
    public Path getWorldPath(LevelResource levelResource) { return null; }
}