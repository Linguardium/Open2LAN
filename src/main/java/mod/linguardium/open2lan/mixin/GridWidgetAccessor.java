package mod.linguardium.open2lan.mixin;

import net.minecraft.client.gui.layouts.GridLayout;
import java.util.List;

import net.minecraft.client.gui.layouts.LayoutElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GridLayout.class)
public interface GridWidgetAccessor {
    @Accessor("children")
    List<LayoutElement> getChildren();
}
