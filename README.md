# TFC+ Quick Pockets

The TFC+ Addon that tries to simplify and streamline inventory management - one unnecessary click at a time. So that you can focus on playing this incredible game instead of being bogged down by inventory screens.

If you find inventory management tedious, this is for you!

## Features

### Hotbar Cycling

TFC adds so many great new tools to minecraft, and this is absolutely awesome! However it also means that your hotbar is constantly filled with tools that you have to swap between very often, which brings your momentary experience to a halt.

This mod tackles this issue by allowing you to swap items in your hotbar without ever having to go to your inventory screen. Simply press and hold <kbd>ALT</kbd> and a preview window will appear showing you whats in the inventory slots above, and below your hotbar.

<details>
  <summary>Click to expand.</summary>
  <p align="center">
    <img src="screenshots/hotbar.png">
  </p>
</details>  

While holding <kbd>ALT</kbd> you can then <kbd>SCROLL UP</kbd> or <kbd>SCROLL DOWN</kbd> to swap the items in your hotbar, giving you quick and convenient access to your whole inventory.

### Auto-Refill

You're chopping a tree, and your axe suddenly breaks. Aw snap - well thankfully you've come prepared! You have another axe in your inventory just for situations like this. But now you have to open up the inventory menu and swap it into your hand - completely breaking your tree-cutting flow. Not anymore.

With the auto-feature enabled, whenever you break a tool, a new one will be placed into your hand - provided you have a backup in your inventory to begin with. This also works for weapons, food, drinks, building blocks, as well as other items like stone flakes, flowers, etc.

### Quick Bag Access

Moving items to and from vessels and leather bags can be very tedious, as you first have to open up your inventory, drop the first bag in your hotbar, close the menu, right click on the bag from your hotbar, move the items from the bag to your inventory, then open up your inventory again, move the second bag to the hotbar, right click on the second bag...

With this mod, you can simply <kbd>RIGHT CLICK</kbd> on any bag from any inventory screen and the bag will be placed in your hotbar and open right there on the spot. This allows you to swap items in your bag without ever leaving your inventory screen once.

### Quick Tool Swapping

You want the right tool for the job, but it's not in your hotbar! It's god knows where in your inventory, oh brother. This mod adds hotkeys that allow you to instantly cycle through the tools you want without you ever having to look through your inventory. There's hotkeys for axes, saws, pickaxes, chisels, ...

These hotkeys are not bound to anything by default, but you can bind them yourself in the `Controls` section of the Options menu, under the `Inventory` category. 

### Walk in Inventory

Have you ever found it kind of strange how your character cannot move at all while your inventory screen is open. Well if this ever bothered you before, this mod allows you to run and jump inside of your inventory screen when you hold the sprint key. Be free.

Note that it wouldn't make much sense to allow the player to move around while inspecting a chest's inventory, so this feature only works for the player inventory, as well as for container inventories for containers that you can carry around with you, like the small vessel and the leather bag.

### Waterskin Fix

<p align="left">
  <img align="left" width="56" height="56" src="screenshots/waterskin.png">
  
  These little guys are a blessing and a curse. On one hand they hold a lot more water than a clay jug and so you don't have to carry as many of them. On another hand, they are hyperactive little monsters! Every time you fill one up, or drink from it, they change inventory slots, ruining your perfectly organized inventory.
</p>

This mod adds a very hacky fix for this problem. It detects when a waterskin "hops" inventory slots and places it in the correct slot on the next game tick, saving you the trouble of doing it manually.

### Inventory Screen Fixes

Apart from the core features, this mod also fixes a few small visual inconsistencies in inventory screens. Things that you probably won't notice for a while but that you can't stop noticing if you've seen them.

The player model in the inventory screen is supposed to be looking directly at your cursor - but they don't! They look slightly off to the side. This was fixed.

<details>
  <summary>Click to expand.</summary>
  <p align="center">
    <img width="796" height="274" src="screenshots/player-stare.png">
  </p>
</details>

The basket and large vessel inventory screens hava a small inconsistency on their left-most inventory slots. This has also been fixed.

<details>
  <summary>Click to expand.</summary>
  <p align="center">
    <img width="796" height="172" src="screenshots/gui-fixes.png">
  </p>
</details>

### No Useless Drops

<p align="left">
  <img align="left" width="60" height="60" src="screenshots/spider-eye.png">
  <img align="right" width="60" height="60" src="screenshots/rotten-flesh.png">
  
  Spider eyes and rotten flesh have absolutely no use in TFC+. Their only purpose for existing is to bog down your inventory - well not anymore. This mod stops rotten flesh and spider eyes from spawning.
</p>

### Config Options

All of the above features are fully customizable. You can completely choose whatever combination of features works best for you. There configuration categories for each of the above features in the mod's `Config` menu. For features that require pressing keys, these keys can be bound through the key bindings menu, under the `Inventory` category.

<details>
  <summary>Click to expand.</summary>
  <p align="center">
    <img src="screenshots/config.png">
  </p>
</details>

## Requirements

This mod is build as an addon for TerraFirmaCraft+, a minecraft mod which you can download [here](https://www.curseforge.com/minecraft/mc-mods/terrafirmacraftplus/files) if you haven't already. It was tested and working with TFC+ versions 0.84.1, 0.85, 0.85.1, however it should also work with newer versions as long as they don't change the GUI too much.

The mod was build using Minecraft Forge 10.13.4.1558 for minecraft 1.7.10. Forge is required for running this mod, and you can download it [here](http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.7.10.html) if you haven't already.

## Build from Source

Download the source and open a terminal in the folder containing the `gradlew` file. Run:

```bash
$ gradlew setupDecompWorkspace
```

This will take a while to complete. After it's done run:

```bash
$ gradlew build
```

After it has finished, the mod you built should be in the `./build/libs/` directory.

## Download and Install

Download the latest version [here](https://github.com/blat-blatnik/TFC-Quick-Pockets/releases). 

Put the downloaded `jar` file in your [`mods`](https://gaming.stackexchange.com/questions/151317/where-is-the-mod-folder) folder, right alongside TFC+. Tested and working with TFC+ 0.84.1, 0.85, 0.85.1. 

**Note** the mod is currently in beta. It was not tested extensively yet and so some problems could have slipped through the cracks. Only use this if you understand the risks. _**Back up** your beloved save games before using this mod_.
