package offlcersam.shipfoundry.mixin;

import game.graphics.GraphicsLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GraphicsLoader.class, remap = false)
public class GraphicsLoaderMixin {

    @ModifyArg(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lgame/graphics/DeferedTextureLoader;<init>(ILjava/lang/String;Ljava/lang/String;IIZ)V",
                    // Just in case tell only the shipTextureLoader to be 2000
                    ordinal = 1
            ),
            index = 0
    )
    // There is no max to this in theory, but I've set it to the same ID limit.
    private int changeShipTextureAmount(int amount) {
        return 2000;
    }
}