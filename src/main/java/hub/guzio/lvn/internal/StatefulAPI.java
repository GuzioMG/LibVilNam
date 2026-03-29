package hub.guzio.lvn.internal;

import hub.guzio.lvn.API;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class StatefulAPI extends API implements SaveState {
    private Optional<Path> innerPath = Optional.empty();
    private final Logger L;

    StatefulAPI(int padding, boolean doVillagelike, @NotNull String dataset, @NotNull String langCode, @NotNull Logger logger) {
        super(padding, doVillagelike, dataset, langCode, logger);
        L = logger;
        L.info("[StatefulAPI/<init>] Upstream constructed; nothing to do downstream.");
    }

    @Override
    public void load(Path worldDir) throws IllegalStateException, IOException {
        if (innerPath.isPresent()) throw new IllegalStateException("Tried to load LibVilNam data after it was already loaded!");

        var path = Path.of(worldDir.toString(), "dimensional");
        innerPath = Optional.of(path);

        L.info("[StatefulAPI/load] Loading...");
        super.villages = new ConcurrentHashMap<>();
        var dir = path.toFile(); //TODO: do something with it
        L.info("[StatefulAPI/load] Loaded!");
    }

    @Override
    public void save() throws IllegalStateException, IOException {
        if (innerPath.isEmpty()) throw new IllegalStateException("Tried to save LibVilNam data before it was already loaded!");
        L.info("[StatefulAPI/load] Saving...");
        //TODO: save
    }

    @Override
    public void close() throws IllegalStateException {
        if (innerPath.isEmpty()) throw new IllegalStateException("Tried to close LibVilNam API twice!");
        L.info("[StatefulAPI/load] Closing...");
        innerPath = Optional.empty();
        super.villages = new ConcurrentHashMap<>();
        L.info("[StatefulAPI/load] Closed.");
    }
}