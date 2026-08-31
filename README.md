# ShipUtil / ShipFoundry

A Fabric mod for Sector Space that loads ship definitions from JSON files, so a ship (and its market listing,
spawn behavior, crafting recipe, stat bonuses, and built-in devices) can be authored as data instead of Java/Mixin code.

Requires my fork of SSFML: https://github.com/OfflcerSam/SectorSpaceFabricModLoader

Latest game version support: 0.5.9.6

## Folder convention

Place JSON files under:

```
<gameDirectory>/ships/<yourShipPackName>/*.json
```

Each subfolder of `ships/` is treated as its own namespace (typically your mod's name).
Every `.json` file directly inside it is loaded as one ship. Ship IDs must be unique across **all** `ships/` subfolders.
If two files claim the same id, the second one loaded is skipped with a log message naming the folder that already claimed it.

A ship's sprite is optional. If a PNG named `ship_base_<renderIndex>.png` sits next to the ship's JSON in the same
mod folder, it's copied into `<gameDirectory>/entity/` automatically (see [Ship textures](#ship-textures) below).
Without one, the ship just uses vanilla's usual missing-texture fallback.

## Current Schema

Every section below `slots` is optional, it just won't be  listed on any market, won't spawn as an NPC/boss/police ship, and won't have a crafting recipe.
Example that is included in repo:
```json
{
  "id": 1000,
  "icon": 30,
  "color": "AZURE",
  "name": "Arrowhead",
  "description": "Maybe one day you could be a real arrow. Inbuilt: Boostyer",
  "tier": 0,
  "rarity": "UNCOMMON",
  "renderIndex": 1000,
  "engineDisplacement": 37,
  "hull": 300.0,
  "cargo": 61.875,

  "weaponLayout": [
    {
      "angle": 32.5,
      "distance": -9.2
    },
    {
      "angle": -32.5,
      "distance": -9.2
    },
    {
      "angle": 0.0,
      "distance": -6.0
    }
  ],

  "slots": {
    "energy": 2,
    "armor": 1,
    "shield": 1,
    "device": 0,
    "module": 1,
    "engine": 1
  },

  "registration": {
    "market": true,

    "npc": [
      { "tier": 0, "weight": 5 }
    ],

    "boss": [
      { "sectorTier": 0, "weight": 1 }
    ],

    "police": { "weight": 1 },

    "uniqueLoot": [
      {
        "id": 20141,
        "amount": 1,
        "chance": 100
      },
      {
        "id": 20130,
        "amount": 1,
        "chance": 25
      }
    ]
  },

  "recipe": {
    "label": "T1:Arrowhead",
    "blueprintId": 20107,
    "blueprintAmount": 1,
    "ingredients": [
      { "id": 100001, "amount": 1 },
      { "id": 10702, "amount": 6 },
      { "id": 10711, "amount": 8 }
    ]
  },

  "shipStats": [
    { "stat": "ENERGY_BANK", "percent": 140.0 },
    { "stat": "ENERGY_REGEN", "percent": 90.0 },
    { "stat": "STATION_SLOT", "flat": 5.0 },
    { "stat": "PLATFORM_SLOT", "flat": 1.0 }
  ],

  "builtInDevices": [ 960 ],

  "isStation": false,
  "isPlatform": false
}
```

### Base fields

| Field                                                                | Type            | Notes                                                                                                                                                                                                                                                                                                    |
|----------------------------------------------------------------------|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `id`                                                                 | int             | Unique ship base ID, do not change this unless you plan on starting a new save. Must be unique across every loaded ship (including vanilla's own - see [Reserved vanilla ship IDs](#Reserved-vanilla-base-ship-IDs)) and stay under 2000, since `ShipList.loadShipStatsFromItems` only scans ids 0-1999. |
| `icon`                                                               | int             | Vanilla sprite-sheet icon index.                                                                                                                                                                                                                                                                         |
| `color`                                                              | string          | Name of a `Color` constant (case-insensitive). See [Color constants](#color-constants) for the full list.                                                                                                                                                                                                |
| `name`                                                               | string          | Display name.                                                                                                                                                                                                                                                                                            |
| `description`                                                        | string          | Display description. Optional, defaults to `""`.                                                                                                                                                                                                                                                         |
| `tier`                                                               | int             | Affects usable level.                                                                                                                                                                                                                                                                                    |
| `rarity`                                                             | string          | Name of a `TypeTag` constant (case-insensitive): `NONE`, `JUNK`, `COMMON`, `UNCOMMON`, `RARE`, `EXOTIC`, `LEGENDARY`, `PLATFORM`, `STATION`.                                                                                                                                                             |
| `renderIndex`                                                        | int             | Ship sprite index, should match the ship_base_####. Current cap is 2000.                                                                                                                                                                                                                                 |
| `engineDisplacement`                                                 | int             | Engine position glow, in pixels from center. Optional, defaults to `0`.                                                                                                                                                                                                                                  |
| `hull`                                                               | float           | Hull HP, decompile ShipList for reference.                                                                                                                                                                                                                                                               |
| `cargo`                                                              | float           | Cargo capacity, decompile ShipList for reference.                                                                                                                                                                                                                                                        |
| `weaponLayout`                                                       | array or string | Either an array of `{ "angle": ##, "distance": ## }` turret slots in order (vanilla goes up to 10 slots), or a string naming one of vanilla's own built-in layouts by its `WeaponSlotLayoutList` constant name.                                                                                          |
| `slots.energy` / `armor` / `shield` / `device` / `module` / `engine` | int             | Slot counts. Any omitted default to `0`. Vanilla doesn't currently exceed 9 in any category.                                                                                                                                                                                                             |

### `weaponLayout`

Either an array of custom turret slots, or a string naming one of vanilla's own built-in layouts to reuse directly.

**Custom layout** - an array of `{ "angle": ##, "distance": ## }` objects, one per turret slot, in order (vanilla goes up to 10 slots):

```json
"weaponLayout": [
  { "angle": 32.5, "distance": -9.2 },
  { "angle": -32.5, "distance": -9.2 },
  { "angle": 0.0, "distance": -6.0 }
]
```

**Vanilla layout** - a string matching one of `WeaponSlotLayoutList`'s own layout constant names exactly (case-insensitive):

```json
"weaponLayout": "S_10_T"
```

| Constant |         |         |          |          |
|----------|---------|---------|----------|----------|
| `S_1_V`  | `S_2_V` | `S_3_V` | `S_4_V`  | `S_5_V`  |
| `S_6_V`  | `S_7_V` | `S_8_V` | `S_9_V`  | `S_10_V` |
| `S_7_T`  | `S_8_T` | `S_9_T` | `S_10_T` |          |

### `registration` (optional)

Controls whether the ship shows up anywhere beyond just existing as a usable ship. Any sub-section left out simply isn't registered.
A ship with no `"registration"` at all is loadable/usable but doesn't load to markets, NPC spawns, and crafting.

| Field        | Type    | Notes                                                                                                                                                                                                                                                                                                                                                                                                                  |
|--------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `market`     | boolean | Optional, defaults to `false`. If `true`, the ship is listed for buy/sell at any market matching station index 501 or 511, which are vanilla shipyards. (`MarketList`'s `shipyard.addStationIndices(501, 511)`)                                                                                                                                                                                                        |
| `npc`        | array   | Optional. Each entry is `{ "tier": #, "weight": # }`. Registers the ship as a candidate normal NPC spawn for that tier (0-5). `weight` is "tickets" relative to *one* vanilla-roll ticket for that tier - weight 1 makes it roughly as common as a single vanilla ship in that tier's pool, weight 2 about twice as likely, etc. A ship can appear in multiple tiers by listing multiple entries. Minimum weight is 1. |
| `boss`       | array   | Optional. Each entry is `{ "sectorTier": #, "weight": # }`. Same weighting rule as `npc`, but for boss spawns in that sector tier (0-6, where 6 covers "6 or higher").                                                                                                                                                                                                                                                 |
| `police`     | object  | Optional. `{ "weight": # }`. Registers the ship as a candidate for both single police spawns and grouped temp/escort police spawns. Not tiered as police spawns are not tier gated. Minimum weight is 1.                                                                                                                                                                                                               |
| `uniqueLoot` | array   | Optional. Each entry is `{ "id": #, "amount": #, "chance": # }`. Extra item(s) this ship can drop when destroyed as an NPC/boss, **on top of** whatever generic loot it would already drop. `amount` defaults to `1`, `chance` is a plain 0-100 percentage defaulting to `100` (always drops).                                                                                                                         |

`uniqueLoot` requires the item's ID like shown in recipe.

### `recipe` (optional)

Adds the ship to the standard crafting table (`CraftingTableNormal`). If omitted, the ship simply isn't craftable.

| Field             | Type   | Notes                                                                                                            |
|-------------------|--------|------------------------------------------------------------------------------------------------------------------|
| `label`           | string | Recipe label shown in the crafting UI. No specific naming convention required, vanilla usually does T1:ShipName. |
| `blueprintId`     | int    | Item ID of the blueprint consumed.                                                                               |
| `blueprintAmount` | int    | Optional, defaults to `1` which is the normal amount.                                                            |
| `ingredients`     | array  | Must have **exactly 3** entries, each `{ "id": #, "amount": # }`. This is a vanilla restriction for the moment.  |

The ship's own database/market ID in the recipe is derived as `100000 + id`, if you want to add it as a craftable.

Blueprint/material variable names below are exactly as named in the decompiled `CraftingTableNormal.init()`, use the **IDs** not the name.

**Processed Materials:**

| Tier Range | Node           | Matrix         |
|------------|----------------|----------------|
| 0-1        | `CuNode` 10701 | `FeMatx` 10711 |
| 2          | `AgNode` 10702 | `AlMatx` 10712 |
| 3-4        | `AuNode` 10703 | `TiMatx` 10713 |
| 4-5        | `PtNode` 10704 | `WgMatx` 10714 |
| 5-6        | `OsNode` 10705 | `VaMatx` 10715 |

**Energy / Fluids / Gems / Tablets:**

| Material  | ID    | Notes                                                   |
|-----------|-------|---------------------------------------------------------|
| `enrGEM`  | 10079 | Energized essence, seen in tier 0-3 recipes.            |
| `drkGEM`  | 10067 | Dark essence, seen in tier 0-3 recipes.                 |
| `enrFLD`  | 10815 | Energized essence, seen in tier 4-6 recipes.            |
| `drkFLD`  | 10816 | Dark essence, seen in tier 4-6 recipes.                 |
| `orgGEL`  | 10069 | Organic gel, used across all tiers 0-6.                 |
| `tabletA` | 10817 | Rare tablet, only seen in tier 4-6 unique ship recipes. |
| `tabletB` | 10818 | Rare tablet, only seen in tier 4-6 unique ship recipes. |

**Kinetic / EM / Field / Bulk / Container blueprints:**

| Tier Range | Kinetic            | EM               | Field             | Bulk               | Container          |
|------------|--------------------|------------------|-------------------|--------------------|--------------------|
| 0-1        | `bpKineT0_1` 20101 | `bpEMT0_1` 20102 | `bpFldT0_1` 20103 | `bpBulkT0_1` 20104 | `bpContT0_1` 20105 |
| 2          | `bpKineT2` 20111   | `bpEMT2` 20112   | `bpFldT2` 20113   | `bpBulkT2` 20114   | `bpContT2` 20115   |
| 3-4        | `bpKineT3_4` 20121 | `bpEMT3_4` 20122 | `bpFldT3_4` 20123 | `bpBulkT3_4` 20124 | `bpContT3_4` 20125 |
| 5-6        | `bpKineT5_6` 20131 | `bpEMT5_6` 20132 | `bpFldT5_6` 20133 | `bpBulkT5_6` 20134 | `bpContT5_6` 20135 |

**Ammo / Ship / Launcher blueprints:**

| Tier Range | Ammo               | Ship               | Launcher           |
|------------|--------------------|--------------------|--------------------|
| 0-1        | `bpAmmoT0_1` 20106 | `bpShipT0_1` 20107 | `bpLnchT0_1` 20108 |
| 2          | `bpAmmoT2` 20116   | `bpShipT2` 20117   | `bpLnchT2` 20118   |
| 3          | `bpAmmoT3` 20126   | `bpShipT3` 20127   | `bpLnchT3` 20128   |
| 4-5        | `bpAmmoT4_5` 20136 | `bpShipT4_5` 20137 | `bpLnchT4_5` 20138 |

**Unique faction/ship plans:**

| Plan             | ID    |
|------------------|-------|
| `bpCovertPlans`  | 20129 |
| `bpCarrierPlans` | 20130 |
| `bpSpecterPlans` | 20139 |
| `bpArchPlans`    | 20140 |
| `bpValkPlans`    | 20141 |

### `shipStats` (optional)

Gives the ship permanent stat bonuses, reapplied every time its stats recompile.
This is the JSON equivalent of  ShipTest's `ShipListMixin`, which hardcoded a per-ship-id `if` block mirroring the giant
vanilla case switch in `items.lists.ShipList#compile(boolean, int)` - here it's driven generically off whatever's in the ship's JSON instead.

Each entry is `{ "stat": "NAME", "flat": #, "percent": # }` - `stat` is the name of an `items.Stat` constant
(case-sensitive, matched exactly as written below), and at least one of `flat`/`percent` must be present. A stat
that doesn't support the one you gave it (see table) just logs a warning in-game and does nothing for that value.

| Stat              | Flat | Percent | Notes                                                                                                                                              |
|-------------------|------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `WEAPON_DAMAGE`   | Yes  | Yes     | Weapon Damage.                                                                                                                                     |
| `WEAPON_RANGE`    | Yes  | No      | Weapon Range.                                                                                                                                      |
| `WEAPON_ROF`      | No   | Yes     | Weapon Rate of Fire.                                                                                                                               |
| `WEAPON_ACCURACY` | No   | Yes     | Weapon Accuracy                                                                                                                                    |
| `CRITICAL_CHANCE` | No   | Yes     | Critical Chance.                                                                                                                                   |
| `CRITICAL_DAMAGE` | Yes  | Yes     | Critical Damage.                                                                                                                                   |
| `CARGO_SPACE`     | Yes  | Yes     | Cargo Space.                                                                                                                                       |
| `CLOAK_POWER`     | No   | Yes     | Only applies if the ship has a cloak installed.                                                                                                    |
| `CLOAK_EVASION`   | No   | Yes     | Only applies if the ship has a cloak installed.                                                                                                    |
| `JUMP_RANGE`      | Yes  | Yes     | Only applies if the ship has a jump drive.                                                                                                         |
| `JUMP_CHARGE`     | Yes  | Yes     | Only applies if the ship has a jump drive.                                                                                                         |
| `ENERGY_BANK`     | Yes  | Yes     | Max energy capacity.                                                                                                                               |
| `ENERGY_REGEN`    | Yes  | Yes     | Energy Regen.                                                                                                                                      |
| `SOLAR_REGEN`     | Yes  | Yes     | Only applies if the ship has solar regen already.                                                                                                  |
| `SHIELD_BANK`     | Yes  | Yes     | Max shield strength.                                                                                                                               |
| `SHIELD_REGEN`    | Yes  | Yes     | Shield Regen.                                                                                                                                      |
| `SHIELD_RESIST`   | No   | Yes     | Shield Resist.                                                                                                                                     |
| `SHIELD_SOAK`     | Yes  | No      | Shield Soak.                                                                                                                                       |
| `ARMOR_INTEG`     | Yes  | Yes     | Max armor integrity.                                                                                                                               |
| `ARMOR_REPAIR`    | Yes  | Yes     | Armor Regen.                                                                                                                                       |
| `ARMOR_RESIST`    | No   | Yes     | Armor Resist.                                                                                                                                      |
| `ARMOR_SOAK`      | Yes  | No      | Armor Soak.                                                                                                                                        |
| `HULL_INTEG`      | Yes  | Yes     | Max hull integrity.                                                                                                                                |
| `HULL_REPAIR`     | Yes  | Yes     | Hull Regen.                                                                                                                                        |
| `HULL_RESIST`     | No   | Yes     | **Currently does nothing.** - `ShipList.compile()`'s case for this stat is an empty switch case in this game version, so setting it has no effect. |
| `HULL_SOAK`       | Yes  | No      | **Currently does nothing.**, same reason as `HULL_RESIST`.                                                                                         |
| `FIGHTER_BONUS`   | Yes  | No      | Fighter slot count.                                                                                                                                |
| `MINING_POWER`    | Yes  | No      | Mining Power.                                                                                                                                      |
| `MINING_DAMAGE`   | Yes  | No      | Mining Damage.                                                                                                                                     |
| `PLATFORM_SLOT`   | Yes  | No      | Bonus deployable platform slots.                                                                                                                   |
| `STATION_SLOT`    | Yes  | No      | Bonus deployable station slots.                                                                                                                    |
| `MISSILE_BONUS`   | Yes  | No      | Missile salvo size.                                                                                                                                |

Every stat bonus you give a ship automatically shows up in its in-game description, exactly like vanilla ships do.
So you don't need to write this text yourself or do anything extra, it's automatic.

### `builtInDevices` (optional)

A flat array of device item ids, Only 16 of these actually do anything.
`items.lists.DeviceList#compile` checks an *installed* device's item id against to flip one of
`game.shiputils.UniqueDeviceUpdater`'s static flags on.

If you want your ship's description to say that it has a named Inbuilt (e.g. "Inbuilt: Booster, "), you need to type that
into your own `"description"` field yourself, same as vanilla does.

| IDs                     | Flag(s)                                                                    | Effect                           |
|-------------------------|----------------------------------------------------------------------------|----------------------------------|
| `920` `921` `922` `923` | `hasDroneBoost1` - `hasDroneBoost4`                                        | Drone Boost tiers 1-4.           |
| `960` `961` `962` `963` | `hasBooster` / `hasBetterBooster` / `hasBestBooster` / `hasBestestBooster` | Booster tiers 1-4.               |
| `970` `971` `972` `973` | `hasOffLevel1` - `hasOffLevel4`                                            | Offensive Coprocessor tiers 1-4. |
| `980` `981` `982` `983` | `hasDefLevel1` - `hasDefLevel4`                                            | Defensive Coprocessor tiers 1-4. |

### `isStation` / `isPlatform` (optional)

Both default to `false` (a normal ship) and can't both be `true` on the same ship. Vanilla itself decides whether a ship is a normal ship, station, or platform
purely by which numeric bracket its id falls into (`game.shiputils.ShipStats#determineHullObjectType`, where below 500 is a ship, 500-599 is a station, 600+ is a platform).
`NPCPilot` checks that before running any movement logic, and what `SpaceShipDrawer` uses to pick which draw path to use (including weapon slot positioning).

`ShipStatsMixin` overrides that check for every ship this mod registers, using these two flags directly instead of the ship's id.
So the id you pick has no effect on whether it moves and renders like a ship, station, or platform.
Leave both `false` for a normal ship regardless of what id you gave it.

## Color constants

Any of these (case-insensitive) work for the `color` field, taken from `illuminatus.core.graphics.Color`:

```
WHITE, LT_GRAY, LT_GREY, GRAY, GREY, M_GRAY, M_GREY, DK_GRAY, DK_GREY, LT_BLACK, BLACK,
PINK, CORAL_RED, RED, LT_RED, DK_RED, M_RED, MAROON, LT_MAROON, DK_MAROON, BROWN,
ORANGE, RED_ORANGE, DK_ORANGE, M_ORANGE, LT_ORANGE, TANGERINE, LT_TANGERINE,
YELLOW, DK_YELLOW, M_YELLOW, LT_YELLOW, OLIVE, DK_OLIVE,
EMERALD, LT_LIME, LIME, DK_LIME, M_LIME, GREEN, DK_GREEN,
LT_AZURE, BR_AZURE, AZURE, M_AZURE, DK_AZURE, SKY_BLUE, LT_BLUE, M_BLUE, BLUE,
GREY_BLUE, DK_BLUE, NAVY, GREY_NAVY, DK_NAVY, LT_NAVY,
TURQUOISE, AQUA, LT_AQUA, TEAL, AQUA_AZURE,
LT_VIOLET, VIOLET, DK_VIOLET, LAVENDER, LT_LAVENDER, PURPLE, LT_PURPLE, DK_PURPLE
```

## Reserved vanilla base ship IDs

Vanilla IDs will most likely win, do not try to overwrite them. This is purely about avoiding id collisions with  real vanilla ships.

Available IDs 1,699.

```
write(): 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 35, 36, 37, 38, 39, 45,
46, 47, 48, 49, 50, 51, 52, 90, 91, 92, 93, 94, 101, 102, 103, 104, 105, 106, 107, 108, 110, 111, 112, 113, 114, 115,
116, 117, 118, 120, 121, 122, 123, 124, 125, 126, 127, 128, 130, 131, 132, 133, 134, 135, 136, 137, 138, 140, 141, 142,
143, 144, 150, 151, 152, 153, 154, 160, 161, 162, 163, 164, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181,
182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 200, 201, 202, 203, 204, 205, 206,
207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 225, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251,
252, 253, 254, 255, 256, 257, 258, 371, 372, 373, 374, 375, 376, 398.

writeStation(): 501, 502, 503, 504, 505, 506, 507, 508, 511, 512, 513, 514, 515, 516, 517, 520, 521, 522, 523, 524, 525,
526, 530, 531, 532, 533, 534, 535, 536, 541, 542, 543, 544, 545, 546, 547, 548, 549, 551, 552, 553, 554, 556, 557, 561,
562, 563, 564, 565, 566, 571, 572, 573, 574, 576, 577.

writeDrone(): 600, 601, 602, 603, 604, 606, 607, 608, 609, 611, 612, 613, 614, 616, 617, 618, 619, 621, 622, 623, 624,
625, 626, 627, 628, 629, 630, 631, 632, 633, 650, 651, 660, 661, 699, 700, 701, 702, 703, 704, 706, 707, 708, 709, 710,
711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 771, 772, 773, 774, 775, 776, 798, 800,
801, 802, 803, 810, 811, 812, 813, 900, 910.
```

## Ship textures

`game.graphics.DeferedTextureLoader` resolves a ship's sprite as `entity/ship_base_<renderIndex>.png`, checked first as an external file relative to the game directory, and only falling back to a classpath-bundled resource (baked into  a mod jar at build time) if that's missing.
Since a JSON-only ship has no build-time resource, `ShipGraphicsLoader` copies `ship_base_<renderIndex>.png` from next to the ship's JSON into `<gameDirectory>/entity/`
automatically during loading, so drop-in-a-folder works for sprites as well instead of needing the user to make a compiled jar.

## Known limitations

- Recipe ingredient/blueprint IDs are raw database item ids (e.g. `100001`, `10702`) as I have not put in name-based lookup yet.
  To get the recipe IDs, I recommend looking through CraftingTableNormal using a decompiler like JADX.

## Setup

`ships/ShipSample/arrowhead.json` recreates ShipTest's original Arrowhead as JSON, using id `1000` and exercising every optional section above:
market listing, tier-0 NPC spawn, a sector-tier-0 boss spawn, a police spawn, a couple of unique loot drops, its original crafting recipe, a couple of `shipStats` bonuses, and an inbuilt Booster device. `ship_base_1000.png` sits alongside it as the matching sprite.

Copy the `ShipSample` folder to `<gameDirectory>/ships/` to try it. If ships folder doesn't exist create it.

Use `STEST` on character save name to get all ships on load. Otherwise, use the folder name for that folder's ships on load.