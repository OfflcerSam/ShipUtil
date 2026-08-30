package offlcersam.shipfoundry;

import mods.ModLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class ShipGraphicsLoader {
    private static final String TEXTURE_FOLDER_NAME = "entity";
    private static final String TEXTURE_FILE_PREFIX = "ship_base_";
    private static final String TEXTURE_FILE_EXTENSION = ".png";

    private ShipGraphicsLoader() {
    }

    /**
     * Looks for "ship_base_<renderIndex>.png" next to the ship's JSON file and, if present,
     * copies it into <gameDirectory>/entity/ so the vanilla ship texture loader can find it as an external file.
     * Should be safe to call even when no matching PNG exists it just would then use the vanilla fallback.
     */
    public static void loadShipTexture(Path modFolder, ShipDefinition def) {
        String textureFileName = TEXTURE_FILE_PREFIX + def.renderIndex() + TEXTURE_FILE_EXTENSION;
        Path sourceTexture = modFolder.resolve(textureFileName);

        if (!Files.isRegularFile(sourceTexture)) {
            ModLogger.log(
                    "[ShipFoundry] No \"" + textureFileName
                            + "\" found next to ship id " + def.id()
                            + "'s JSON in \"" + modFolder.getFileName()
                            + "\" - it will use vanilla's fallback texture for render index " + def.renderIndex()
            );
            return;
        }

        Path entityFolder = Paths.get("").toAbsolutePath().resolve(TEXTURE_FOLDER_NAME);
        Path destinationTexture = entityFolder.resolve(textureFileName);

        try {
            Files.createDirectories(entityFolder);
            Files.copy(sourceTexture, destinationTexture, StandardCopyOption.REPLACE_EXISTING);
            ModLogger.log(
                    "[ShipFoundry] Copied ship texture " + sourceTexture
                            + " -> " + destinationTexture
            );
        } catch (IOException e) {
            ModLogger.log(
                    "[ShipFoundry] Failed to copy ship texture " + sourceTexture
                            + " to " + destinationTexture + ": " + e
            );
        }
    }
}