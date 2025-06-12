package mod.linguardium.open2lan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static mod.linguardium.open2lan.Constants.MOD_NAME;

public class Logging {
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void log(Level level, String message) {
        LOGGER.atLevel(level).log(message);
    }
}
