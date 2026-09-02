package offlcersam.shipfoundry.mixin;

import com.sector.bridge.SSFMLLogger;
import crafting.CraftingTable;
import crafting.CraftingTableNormal;
import offlcersam.shipfoundry.ShipDefinition;
import offlcersam.shipfoundry.ShipRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingTableNormal.class, remap = false)
public abstract class CraftingTableMixin extends CraftingTable {

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void shipfoundry$registerShipRecipes(CallbackInfo ci) {
        int added = 0;

        for (ShipDefinition def : ShipRegistrar.getLoadedShips()) {
            ShipDefinition.Recipe recipe = def.recipe();

            if (recipe == null) {
                continue;
            }

            // Product ID uses the same base-ID -> database/item ID conversion as everything else in this mod
            // (cargo grants, market listings, etc), so a recipe's ingredients can also reference other custom
            // ShipFoundry ships by their base id if needed.
            int productId = ShipRegistrar.toDatabaseID(def.id());

            ShipDefinition.Ingredient ingredientA = recipe.ingredients().get(0);
            ShipDefinition.Ingredient ingredientB = recipe.ingredients().get(1);
            ShipDefinition.Ingredient ingredientC = recipe.ingredients().get(2);

            this.addRecipe(
                    recipe.label(),
                    productId,
                    recipe.blueprintId(), recipe.blueprintAmount(),
                    ingredientA.id(), ingredientA.amount(),
                    ingredientB.id(), ingredientB.amount(),
                    ingredientC.id(), ingredientC.amount()
            );

            added++;
        }

        SSFMLLogger.log("[ShipFoundry] Added " + added + " ship recipe(s) from JSON.");
    }
}