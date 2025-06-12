//? if forge {
/*package mod.linguardium.open2lan.forge;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Open2LanForge  {
    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "open2lan";
    public static final String MOD_NAME = "Open To Lan";

    public void onInitialize() {
        log(Level.INFO, "Initializing");
        //TODO: Initializer;
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

}

*///?}