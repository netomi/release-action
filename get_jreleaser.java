//JAVA 11+

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

class get_jreleaser {
    public static void main(String... args) throws Exception {
        // default version
        var version = "latest";

        if (args.length > 1) {
            System.err.println("Usage: java get_jreleaser.java [VERSION]");
            System.exit(1);
        }

        // grab the version if specified
        if (args.length == 1) {
            version = args[0];
        }

        // check
        if (null == version || version.isEmpty()) {
            System.out.printf("❌ Version '%s' is invalid%n", version);
            System.exit(1);
        }

        var versionFile = Path.of("JRELEASER_VERSION");

        var cachedVersion = "";
        if (Files.exists(versionFile)) {
            try {
                cachedVersion = Files.readString(versionFile).trim();
            } catch (IOException e) {
                System.out.printf("☠️  JReleaser %s could not be resolved%n", version);
                e.printStackTrace();
                System.exit(1);
            }
        }

        // resolve latest to a tagged release
        if ("latest".equalsIgnoreCase(version)) {
            var url = "https://jreleaser.org/releases/latest/download/VERSION";

            try (var stream = new URL(url).openStream()) {
                System.out.printf("✅ Located version marker%n");
                Files.copy(stream, versionFile, REPLACE_EXISTING);
                version = new String(Files.readAllBytes(versionFile)).trim();
                System.out.printf("✅ JReleaser latest resolves to %s%n", version);
            } catch (FileNotFoundException e) {
                System.out.printf("❌ JReleaser %s not found%n", version);
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                System.out.printf("☠️  JReleaser %s could not be downloaded/copied%n", version);
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            try {
                Files.writeString(versionFile, version, CREATE, TRUNCATE_EXISTING, WRITE);
            } catch (IOException e) {
                System.out.printf("☠️  JReleaser %s could not be resolved%n", version);
                e.printStackTrace();
                System.exit(1);
            }
        }

        var jreleaserJar = Path.of("jreleaser-cli.jar");
        if (version.equals(cachedVersion) && Files.exists(jreleaserJar)) {
            // assume jreleaserJar has the expected version for now
            System.out.printf("✅ JReleaser %s already downloaded%n", version);
            System.exit(0);
        }

        // setup the actual download
        var tag = !"early-access".equals(version) ? "v" + version : version;
        var url = "https://github.com/jreleaser/jreleaser/releases/download/" + tag + "/jreleaser-tool-provider-" + version + ".jar";

        try (var stream = new URL(url).openStream()) {
            System.out.printf("✅ Located JReleaser %s%n", version);
            System.out.printf("⬇️  Downloading %s%n", url);
            var size = Files.copy(stream, jreleaserJar, REPLACE_EXISTING);
            System.out.printf("%s << copied %d bytes%n", jreleaserJar, size);
            System.out.printf("✅ JReleaser installed successfully%n");
        } catch (FileNotFoundException e) {
            System.out.printf("❌ JReleaser %s not found%n", version);
            System.exit(1);
        } catch (IOException e) {
            System.out.printf("☠️  JReleaser %s could not be downloaded/copied%n", version);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
