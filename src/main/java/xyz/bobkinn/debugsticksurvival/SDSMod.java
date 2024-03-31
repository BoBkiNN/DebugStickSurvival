package xyz.bobkinn.debugsticksurvival;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import space.vectrix.ignite.Ignite;
import space.vectrix.ignite.mod.ModContainer;

public class SDSMod {
    public static Logger LOGGER = LogManager.getLogger("SDS");

    public ModContainer getMod(){
        return Ignite.mods().container("survival_debug_stick").orElseThrow();
    }

    public SDSMod() {
        var file = getMod().resource().path()
                .getParent() // /mods
                .getParent() // /
                .resolve("config") // /config
                .resolve("SDS.json").toFile();
        Config.load(file);
        Config.configFile = file;
        LOGGER.info("SDS initialized successfully!");
    }
}
