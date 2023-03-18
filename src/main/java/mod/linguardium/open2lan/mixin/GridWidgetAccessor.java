package mod.linguardium.open2lan.mixin;

import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GridWidget.class)
public interface GridWidgetAccessor {
    @Accessor("children")
    List<Widget> getChildren();
}
