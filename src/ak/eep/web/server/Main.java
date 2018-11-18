package ak.eep.web.server;

import ak.eep.web.server.io.DirectoryWatcher;
import ak.eep.web.server.io.FileContentReader;
import ak.eep.web.server.io.JsonContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);
    private final String jsonDataFile;
    private final String logInFile;
    private final String commandOutFile;
    private final String serverSyncFile;
    private final String luaReadyFileName;
    private DirectoryWatcher directoryWatcher;

    private Main(Path dir) throws IOException {
        this.directoryWatcher = new DirectoryWatcher(dir).watchFilesInBg();

        this.commandOutFile =
                Paths.get(dir + "/eep-commands.txt").toFile().getAbsolutePath();
        this.logInFile =
                Paths.get(dir + "/eep-log.txt").toFile().getAbsolutePath();
        this.jsonDataFile =
                Paths.get(dir + "/ak_out_eep-web-server.json").toFile().getAbsolutePath();
        this.serverSyncFile =
                Paths.get(dir + "/ak_out_eep-web-server.on-sync-only").toFile().getAbsolutePath();
        // this.serverReadyFileName =
        //      Paths.get(dir + "/ak_out_eep-web-server.server-is-ready-for-data").toFile().getAbsolutePath();
        this.luaReadyFileName =
                Paths.get(dir + "/ak_out_eep-web-server.lua-is-finished-writing-data").toFile().getAbsolutePath();

        File serverSyncFile = new File(this.serverSyncFile);
        //noinspection ResultOfMethodCallIgnored
        serverSyncFile.createNewFile();
        serverSyncFile.deleteOnExit();

        File luaReadyFile = new File(luaReadyFileName);
        //noinspection ResultOfMethodCallIgnored
        luaReadyFile.delete();
        luaReadyFile.deleteOnExit();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Exited correctly ...")));
    }

    public static void main(String[] args) throws IOException {
        Path dir = parseArguments(args);

        Main main = new Main(dir);
        main.startServer();
    }

    private static Path parseArguments(String[] args) throws IOException {
        String directoryName;
        if (args.length > 0) {
            directoryName = args[0];
        } else {
            File dirFile = Paths.get("eep-lua-out-dir.txt").toFile();
            directoryName = dirFile.exists()
                    ? new FileContentReader().readFileContent("eep-lua-out-dir.txt")
                    : "out";
        }
        return checkPathsOrExit(directoryName);
    }

    private static Path checkPathsOrExit(String directoryName) {
        Path dir = Paths.get(directoryName);
        if (!dir.toFile().isDirectory()) {
            System.out.println(dir.toFile().getAbsoluteFile() + " ist kein Verzeichnis. - Bitte ein Verzeichnis angeben.");
            System.exit(1);
        }
        return dir;
    }

    private void startServer() {
        log.info(""
                + "\nCommands will be written to: " + commandOutFile
                + "\nLogs will be read from     : " + logInFile
                + "\nJSON data will be read from: " + jsonDataFile);

        final Server server = new Server();
        final JsonContentProvider jsonContentProvider = new JsonContentProvider(server);
        updateJsonData(jsonContentProvider);

        server.startServer();

        directoryWatcher.addFileConsumer(luaReadyFileName, (change) -> {
            if (change == DirectoryWatcher.Change.CREATED
                    || change == DirectoryWatcher.Change.MODIFIED) {
                updateJsonData(jsonContentProvider);
                //noinspection ResultOfMethodCallIgnored
                Paths.get(luaReadyFileName).toFile().delete();
            }
        });
    }

    private void updateJsonData(JsonContentProvider jsonContentProvider) {
        try {
            log.info("Read file: " + jsonDataFile);
            String json = new FileContentReader().readFileContent(jsonDataFile);
            jsonContentProvider.updateInput(json);
        } catch (IOException e) {
            log.info("Cannot read file: " + jsonDataFile, e);
        }
    }
}
