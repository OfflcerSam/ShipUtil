package offlcersam.shipfoundry.mixin;

import crafting.CraftingTable;
import crafting.CraftingTableNormal;
import mods.ModLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
TODO: Edit this to be more JSON data magic
 */
@Mixin(value = CraftingTableNormal.class, remap = false)
public abstract class CraftingTableMixin extends CraftingTable {

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void shipfoundry$registerShipRecipe(CallbackInfo ci) {

        // Readability, ShipIDs are essentially BaseID + 10000
        int ARROWHEAD_ID = 100350;
        int FOUNDRY_ITEM_ID = 100040;
        int FOUNDRY_PLUS_ITEM_ID = 100041;

        // Uses market IDs of ships (100000 + ID), these are here for readability.
        int yachtItemId = 100001;
        int fabricatorItemId = 100029;
        int fabricatorplusItemId = 100039;

        // List of materials from CraftingTableNormal
        int bpKineT0_1 = 20101, bpEMT0_1 = 20102, bpFldT0_1 = 20103, bpBulkT0_1 = 20104, bpContT0_1 = 20105, bpAmmoT0_1 = 20106, bpShipT0_1 = 20107, enrFLD = 10815, drkFLD = 10816;
        int bpKineT2 = 20111, bpEMT2 = 20112, bpFldT2 = 20113, bpBulkT2 = 20114, bpContT2 = 20115, bpAmmoT2 = 20116, bpShipT2 = 20117, enrGEM = 10079, drkGEM = 10067;
        int bpKineT3_4 = 20121, bpEMT3_4 = 20122, bpFldT3_4 = 20123, bpBulkT3_4 = 20124, bpContT3_4 = 20125, bpAmmoT3 = 20126, bpShipT3 = 20127, orgGEL = 10069;
        int bpKineT5_6 = 20131, bpEMT5_6 = 20132, bpFldT5_6 = 20133, bpBulkT5_6 = 20134, bpContT5_6 = 20135, bpAmmoT4_5 = 20136, bpShipT4_5 = 20137;
        int CuNode = 10701, AgNode = 10702, AuNode = 10703, PtNode = 10704, OsNode = 10705;
        int FeMatx = 10711, AlMatx = 10712, TiMatx = 10713, WgMatx = 10714, VaMatx = 10715;

        this.addRecipe(
                "T1:Arrowhead", // String: Label, no specific naming convention, copied what it looked like in CraftingTableNormal.
                ARROWHEAD_ID, // Int: Market ID of Ship
                bpShipT0_1, 1, // Int: Blueprint ID, Int: Amount to use
                yachtItemId, 1, // Int: Item A, Int: Amount to use
                AgNode, 6, // Int: Item B, Int: Amount to use
                FeMatx, 8 // Int: Item C, Int: Amount to use
        );

        this.addRecipe(
                "T4:Foundry",
                FOUNDRY_ITEM_ID,
                bpShipT4_5, 1,
                fabricatorItemId, 2,
                AuNode, 20,
                TiMatx, 40
        );

        this.addRecipe(
                "T4:Foundry+Alt1",
                FOUNDRY_ITEM_ID,
                bpShipT4_5, 1,
                fabricatorplusItemId, 1,
                AuNode, 10,
                TiMatx, 25
        );

        this.addRecipe(
                "T5:FoundryPlus",
                FOUNDRY_PLUS_ITEM_ID,
                bpShipT4_5, 1,
                FOUNDRY_ITEM_ID, 1,
                PtNode, 10,
                WgMatx, 30
        );

        this.addRecipe(
                "T5:FoundryPlus+Alt1",
                FOUNDRY_PLUS_ITEM_ID,
                bpShipT4_5, 1,
                fabricatorplusItemId, 2,
                PtNode, 15,
                WgMatx, 50
        );

        ModLogger.log("[ShipTest] Ship recipes added.");
    }
}