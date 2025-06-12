//? if neoforge {
package mod.linguardium.open2lan.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.event.Level;

import static mod.linguardium.open2lan.Constants.MOD_ID;
import static mod.linguardium.open2lan.Logging.log;

@Mod(value=MOD_ID, dist = Dist.CLIENT)
public class Open2LanNeoforge {
    public Open2LanNeoforge() {
        log(Level.INFO, "Initializing");
    }
}
//?}