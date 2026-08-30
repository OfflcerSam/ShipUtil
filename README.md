# ShipUtil / Ship Foundry

A Fabric mod for Sector Space that loads ship definitions from JSON files, so a ship can be written as JSON data instead of Java/Mixin code.

## Folder convention

Place JSON files under:

```
<gameDirectory>/ships/<yourModName>/*.json
```

Each subfolder of `ships/` is treated as its own namespace (typically your mod's name). 
Every `.json` file directly inside it is loaded as one ship. Ship IDs must be unique across **all** `ships/` subfolders.
If two files claim the same id, the second one loaded is skipped with a log message naming the folder that already claimed it.

## Current Schema

```json
{
  "id": 350,
  "icon": 30,
  "color": "AZURE",
  "name": "Arrowhead",
  "description": "Maybe one day you could be a real arrow.",
  "tier": 0,
  "rarity": "UNCOMMON",
  "renderIndex": 350,
  "engineDisplacement": 37,
  "hull": 300.0,
  "cargo": 61.875,
  "weaponLayout": [
    { "angle": 32.5, "distance": -9.2 },
    { "angle": -32.5, "distance": -9.2 },
    { "angle": 0.0, "distance": -6.0 }
  ],
  "slots": {
    "energy": 2,
    "armor": 1,
    "shield": 1,
    "device": 0,
    "module": 1,
    "engine": 1
  }
}
```

| Field | Type | Notes |
|---|---|---|
| `id` | int | Unique Ship Base ID. Max 1999 - as 2000+ equips as null. |
| `icon` | int | Vanilla sprite-sheet icon index. |
| `color` | string | Name of a `Color` constant: `AZURE`, `PURPLE`, `WHITE`, (will add the others later)) |
| `name` | string | Display name. |
| `description` | string | Display description. Optional, defaults to `""`. |
| `tier` | int | Affects spawning and usable level. |
| `rarity` | string | Name of a `TypeTag` constant: `NONE`, `JUNK`, `COMMON`, `UNCOMMON`, `RARE`, `EXOTIC`, `LEGENDARY`, `PLATFORM`, `STATION`. |
| `renderIndex` | int | Ship sprite index. Vanilla currently caps at 376 but built-in extension to 2000 (add later). |
| `engineDisplacement` | int | Engine position glow, in pixels. Optional, defaults to `0`. |
| `hull` | float | Hull HP. |
| `cargo` | float | Cargo capacity. |
| `weaponLayout` | array | One or more `{ "angle":##, "distance":## }` turret slots, in that order, will add built-in layouts later. |
| `slots.energy/armor/shield/device/module/engine` | int | Slot counts. Any omitted default to `0`. Vanilla doesn't currently exceed 9 in any category. |

## TODO

Extend weaponLayout to also allow for built-in layout slot usage.
Import the NPC Spawning from ShipTest.
Clean up some stuff as needed.

## Setup

`example_ships_folder/ShipForgeExample/arrowhead.json` recreates ShipTest's Arrowhead as JSON for validation, using id `900` (not `350` original)
Copy that folder to `<gameDirectory>/ships/` to try it.
