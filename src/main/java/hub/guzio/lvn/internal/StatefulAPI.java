package hub.guzio.lvn.internal;

import hub.guzio.lvn.API;
import hub.guzio.lvn.Village;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class StatefulAPI extends API implements SaveState {
    private Optional<Path> innerPath = Optional.empty();
    private final Logger L;

    StatefulAPI(int padding, boolean doVillagelike, @NotNull String dataset, @NotNull String langCode, @NotNull Logger logger) {
        super(padding, doVillagelike, dataset, langCode, logger);
        L = logger;
        L.info("[StatefulAPI/_] Upstream constructed; nothing to do downstream.");
    }

    @Override
    public void load(@NotNull Path worldDir) throws IllegalStateException, IOException {
        L.info("[StatefulAPI/load] Loading...");
        if (innerPath.isPresent()) throw new IllegalStateException("Tried to load LibVilNam data after it was already loaded!");

        innerPath = Optional.of(worldDir);
        super.villages = new ConcurrentHashMap<>();
        var dir = worldDir.toFile();

        if (!dir.exists()){
            L.info("[StatefulAPI/load] This seems to be your first time using LibVilNam in this world - creating a data folder at {}...", dir.getAbsolutePath());
            if (dir.mkdir()){
                L.info("[StatefulAPI/load] Done! You're good to go; nothing else to do for now.");
                return;
            }
            else L.error("[StatefulAPI/load]  ...or not, as folder {} failed to create! There wasn't even an error thrown, it just somehow refused. Will TRY to continue running the mod (saving also tries to create a directory), but unless something fixes in the meantime (maybe with system permissions, or you free your hard drive space, or something), saving will most certainly fail.", dir.getAbsolutePath());
        }
        else if (!dir.isDirectory()){
            L.error("[StatefulAPI/load] LibVilNam data folder seems to already exist at {}, but it's not actually a folder!", dir.getAbsolutePath());
            throw new NotDirectoryException("Path "+dir.getAbsolutePath()+" was supposed to be a directory, but wasn't!");
        }

        var dirs = dir.listFiles();
        if (Objects.isNull(dirs)){
            try { Objects.requireNonNull(dirs); } //This will be null - that's the point. We want an „organic” NPL.
            catch (NullPointerException e){ throw new FileNotFoundException(e.toString()); }
        }

        for (var subdir : dirs){
            if (!subdir.isDirectory()){
                L.warn("[StatefulAPI/load] {} was supposed to be a folder, but isn't. Skipping it...", subdir.getAbsolutePath());
                continue;
            }

            var files = subdir.listFiles();
            if (Objects.isNull(files)){
                L.warn("[StatefulAPI/load] Tried to list {}, but got a null instead. Skipping it...", subdir.getAbsolutePath());
                continue;
            }

            var subdirName = subdir.getName();
            for (var file : files){
                if (!file.isFile()){
                    L.warn("[StatefulAPI/load] {} was supposed to be a file, but isn't. Skipping it...", subdir.getAbsolutePath());
                    continue;
                }

                var fileName = file.getName();
                if (!fileName.endsWith(".csv")){
                    L.warn("[StatefulAPI/load] {} was supposed to be a CSV file, but isn't (based on extension). Skipping it...", subdir.getAbsolutePath());
                    continue;
                }
                else {
                    fileName = fileName.substring(0, fileName.length()-4);
                }

                var dim = ResourceLocation.fromNamespaceAndPath(subdirName, fileName);
                super.villages.put(dim, readVillageFile(file, dim));
            }
        }

        L.info("[StatefulAPI/load] Loaded!");
    }

    @Override
    public void save() throws IllegalStateException, IOException {
        L.info("[StatefulAPI/save] Saving...");
        if (innerPath.isEmpty()) throw new IllegalStateException("Tried to save LibVilNam data before it was even loaded!");

        var maindir = innerPath.get().toFile();
        if (!maindir.exists()){
            L.info("[StatefulAPI/save] This seems to be your first time using LibVilNam in this world - creating a data folder at {}...", maindir.getAbsolutePath());
            if (maindir.mkdir()) L.info("[StatefulAPI/save] Creation done!");
            else{
                var e = new IOException("Couldn't create a LibVilNam data directory for some reason! Sorry, that's all we know.");
                L.error("[StatefulAPI/save] ...or not, as folder {} failed to create! There wasn't even an error thrown, it just somehow refused - so a generic IO Exception ({}) will be thrown instead.", maindir.getAbsolutePath(), e);
                throw e;
            }
        }
        else if (!maindir.isDirectory()){
            L.error("[StatefulAPI/save] LibVilNam data folder seems to already exist at {}, but it's not actually a folder!", maindir.getAbsolutePath());
            throw new NotDirectoryException("Path "+maindir.getAbsolutePath()+" was supposed to be a directory, but wasn't!");
        }

        for(var pair : super.villages.entrySet()){
            L.info("[StatefulAPI/save] Saving {} villages (+1 doc-line) for {}...", pair.getValue().size(), pair.getKey());
            var file = getVillageFile(pair.getKey());
            StringBuilder result = new StringBuilder("STRUCTURE TYPE, FROM X, TO X, FROM Z, TO Z, VILLAGE NAME");
            for (var village : pair.getValue()) result.append("\n").append(village.toCsvLine());

            var parent = file.getParentFile();
            if (!parent.exists()){
                if (parent.mkdir()) L.info("[StatefulAPI/save] Created a directory {} for dimension {}!", parent.getAbsolutePath(), pair.getKey());
                else{
                    L.error("[StatefulAPI/save] LibVilNam's dimension-data folder {} failed to create! There wasn't even an error thrown, it just somehow refused. Sorry, poor village names for {} unfortunately could not be saved ['] :( May whatever you believe in have mercy upon thyne soul.", parent.getAbsolutePath(), pair.getKey());
                    continue;
                }
            }
            else if (!parent.isDirectory()){
                L.error("[StatefulAPI/save] LibVilNam's dimension-data folder {} seems to already exist, but not be a folder! There wasn't even an error thrown, it just somehow refused. Sorry, poor village names for {} unfortunately could not be saved ['] :( May whatever you believe in have mercy upon thyne soul.", parent.getAbsolutePath(), pair.getKey());
                continue;
            }

            var write = new FileWriter(file);
            write.write(result.toString());
            write.close();
            L.info("[StatefulAPI/save] Written {} lines to {}.", pair.getValue().size()+1, file.getAbsolutePath());
        }

        L.info("[StatefulAPI/save] Saved!");
    }

    @Override
    public void close() throws IllegalStateException {
        if (innerPath.isEmpty()) throw new IllegalStateException("Tried to close LibVilNam API twice!");
        L.info("[StatefulAPI/load] Closing...");
        innerPath = Optional.empty();
        super.villages = new ConcurrentHashMap<>();
        L.info("[StatefulAPI/load] Closed.");
    }

    public @NotNull File getVillageFile(@NotNull ResourceLocation dimId){
        return Path.of(innerPath.orElseThrow().toString(), dimId.getNamespace(), dimId.getPath()+".csv").toFile();
    }

    public @NotNull List<@NotNull Village> readVillageFile(@NotNull File in, @NotNull ResourceLocation dimId) throws FileNotFoundException {
        L.info("[StatefulAPI/readVillageFile] Loading villages for {}... (From file: {}).", dimId, in.getAbsolutePath());
        var result = Collections.synchronizedList(new ArrayList<Village>());
        var scan = new Scanner(in);

        if (scan.hasNextLine()) scan.nextLine(); //NO-OP to get past doc-line
        while (scan.hasNextLine()) result.add(Village.fromCsvLine(scan.nextLine(), dimId));
        scan.close();

        L.info("[StatefulAPI/readVillageFile] Loaded {} villages for {}.", result.size(), dimId);
        return result;
    }
}