package xyz.bobkinn.debugsticksurvival;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import space.vectrix.ignite.api.Platform;

public class SDSMod {
    public static Logger LOGGER = LogManager.getLogger("SDS");

    @Inject
    @SuppressWarnings("unused")
    public SDSMod(final Logger logger,
                  final @NotNull Platform platform) {
        var file = platform.getConfigs().resolve("SDS.json").toFile();
        Config.load(file);
        LOGGER.info("SDS initialized successfully!");
    }
}
