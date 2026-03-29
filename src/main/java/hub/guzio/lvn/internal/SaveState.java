package hub.guzio.lvn.internal;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public interface SaveState extends Closeable {
    void load(Path worldDir) throws IllegalStateException, IOException;
    void save() throws IllegalStateException, IOException;
    @Override void close() throws IllegalStateException;
}