<div align="center">

# ⚔️ BackWeapons

**A Fabric mod for Minecraft 26.2 that puts your sword and axe on your back when you're not swinging them.**

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-Loader%20%E2%89%A5%200.19.3-DBB69B?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/License-CC0--1.0-blue?style=for-the-badge)

</div>

---

## ✨ Overview

Sheathe your weapons — literally. **BackWeapons** watches what's in your hand and, the moment you put your sword or axe away, straps it across your back with a smooth reach-and-sheathe arm animation. Pick it back up, and your character reaches back over their shoulder to draw it.

Built from the ground up against Minecraft 26.2's modern rendering pipeline — no copy-pasted 1.20-era tutorial code, no outdated Yarn mappings. Every class and method used here was verified against real decompiled 26.2 source.

<div align="center">

| 🗡️ Sword sheathed diagonally | 🪓 Axe sheathed horizontally | 🤚 Arm-reach draw animation |
|:---:|:---:|:---:|
| Sits across the back, clears armor | Independent slot, lower placement | Real bone rotation via mixin |

</div>

---

## 🎯 Features

- 🗡️ **Sword on back** — vanishes from your hand the moment it's equipped, and reappears sheathed across your back the instant it isn't.
- 🪓 **Axe on back** — tracked completely independently from the sword, positioned so the two never clip or flicker into each other.
- 💪 **Real arm animation** — not just the item moving; the player's actual arm bone reaches up and across during the transition, via a custom mixin.
- 🛡️ **Armor-aware** — positioned to clear worn chestplates instead of disappearing behind them.
- 🎒 **Inventory-aware** — drop it, lose it, or have it destroyed, and it correctly vanishes from your back instead of haunting you forever.
- 🎮 **Debounced** — rapid hotbar scrolling won't spam the draw animation.

---

## 📋 Requirements

| Component | Version |
|---|---|
| 🎮 Minecraft | `26.2` |
| 🧵 Fabric Loader | `≥ 0.19.3` |
| 📦 Fabric API | `0.161.0+26.2` (or compatible) |
| ☕ Java | `25` |

---

## 📥 Installation

1. Install [**Fabric Loader**](https://fabricmc.net/use/installer/) for Minecraft `26.2`.
2. Grab [**Fabric API**](https://modrinth.com/mod/fabric-api) for `26.2` and drop it in your `mods` folder.
3. Download `backweapons-1.0.0.jar` and place it in `.minecraft/mods`.
4. Launch using the Fabric `26.2` profile. Equip a sword. Put it away. Enjoy.

---

## 🛠️ Building from source

```powershell
git clone <this repo>
cd BackWeapons
.\gradlew.bat build
```

> Output lands at `build\libs\backweapons-1.0.0.jar`

For a quick local test client instead of a full build:

```powershell
.\gradlew.bat runClient
```

---

## 🗂️ Project structure

```
BackWeapons/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/main/
    ├── java/com/azeem/backweapons/
    │   ├── BackWeapons.java                 # Common mod initializer
    │   ├── BackWeaponsClient.java            # Client init — registers the render layer
    │   ├── BackWeaponFeatureRenderer.java    # Draws sword & axe on the back
    │   ├── BackWeaponAnimationTracker.java   # Shared state: layer ↔ mixin
    │   └── mixin/
    │       └── PlayerModelMixin.java         # Injects the arm-reach animation
    └── resources/
        ├── fabric.mod.json
        └── backweapons.mixins.json
```

---

## ⚙️ How it works

Minecraft 26.2 renders entities through a state-extraction pipeline rather than drawing directly:

```
Entity → extractRenderState() → RenderState → submit() → SubmitNodeCollector → GPU
```

**`BackWeaponFeatureRenderer`** is a `RenderLayer` registered onto the player's `AvatarRenderer` via Fabric API's `LivingEntityRenderLayerRegistrationCallback`. Every frame it:

1. Checks the main-hand item against `ItemTags.SWORDS` / `ItemTags.AXES`.
2. Remembers the last sword/axe held — independently — and forgets it if it's no longer actually in the player's inventory.
3. Resolves a fresh `ItemStackRenderState` via `ItemDisplayContext.FIXED`, since the hand-held render state is baked for a gripped pose and can't be reused for a static back display.
4. Draws the remembered item at a tuned position/rotation/scale whenever it isn't currently held.
5. Publishes per-player transition progress into `BackWeaponAnimationTracker`, a static map keyed by entity ID.

A `RenderLayer` alone **cannot** move the base body model — by the time it runs, the body's already been submitted for drawing. So to get a real arm-reach motion, **`PlayerModelMixin`** injects at the tail of `PlayerModel.setupAnim`, reads that player's progress from the shared tracker, and adds extra rotation to the right arm along a `sin(progress × π)` curve — a clean reach-and-return synced to the exact same transition window the back-render uses.

---

## 🔮 Possible future work

- [ ] Pickaxe support (prototyped, reverted — positioning needs more work)
- [ ] Off-hand item tracking
- [ ] Tighter item-type filtering beyond raw tag checks

---

<div align="center">

Built by **Mohammed Azeem** — mentored step-by-step by Claude ("Sanji") 🧭

</div>
