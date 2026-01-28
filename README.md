# Valley Day

/* ---------------------------------------------------------------------
*  Valley Day
* ----------------------------------------------------------------------
*
*  Welcome to a top-down farm & survival game built with LibGDX.
*  Here you grow crops, collect tools and resources, and reach the exit before
*  time runs out — while protecting your plants from hostile wildlife.
*
* ---------------------------------------------------------------------- */


The levels are determined using map files.properties. The game uses Box2D
technology to control movement and collisions and has a 5-minute countdown,
which affects both the gameplay results and the visual atmosphere. The on-screen
HUD display tracks your progress (collecting trash, seeds, plants, and tools),
as well as exit status, while separate menus control the scenarios for pausing,
winning, and ending the game, each accompanied by sound and music.


## Overview --------------------------------------------------------------


- **Platform:** Desktop (Windows, macOS, Linux)
- **Engine:** LibGDX
- **Language:** Java
- **Maps:** `.properties` files


## Project Structure ------------------------------------------------------


The project follows Object-Oriented Programming (OOP) principles and is
divided into clearly separated packages.
- `core/` — Game logic, screens, map, and assets
- `desktop/` — Desktop launcher
- `assets/` — Audio, textures, UI skin, and other resources
- `maps/` — Example map files


### Architecture Overview
All renderable objects implement the `Drawable` interface.

#### Entities
- `Player` — Farmer character; movement, planting, harvesting, shouting
- `Wildlife` — Enemies targeting plants or the player(can be scared with S)
- `WildlifeVisitor` — Map-placed visitor markers


#### Crops & Terrain
- `Grass` — Ground tiles
- `Seed` — Break like debris to collect; then plant on garden beds
- `Plant` — Grows in stages; harvest when mature, restore when rotten.
- `GardenBed` — Passable soil where seeds can be planted.


#### Items
- `Fertilizer` — Unlocks seed spawning
- `WateringCan` — Restores rotten plants (is hidden under debris)
- `Shovel` — Speeds up debris clearing


#### Obstacles
- `Debris` — Removable branches (hold **D** to destroy)
- `Fence` — Indestructible fence.
- `Exit` — Gate, which opens when all objectives are met.


### Main Game Classes
- `GameScreen` - Main gameplay screen: camera, rendering, overlays,
  pause/win/lose states

- `MapLoader` - Parses `.properties` files and constructs levels

- `GameMap` - Central controller: manages physics world, objects, timer
  and win/loss conditions

- `Hud` - Displays timer, tool indicators, counters and context hints

- `GameSound` / `MusicTrack`  - Sound effects and background/menu music



### Game States  ---------------------------------------------------


- **Start State** — Main menu opens and the player can load a map file and start the game.

- **Gameplay State** — Active gameplay where the player can move, grow crops, interact
  with objects, and scare wildlife while the time keeps running.

- **Paused State** — Occurs when the player presses **Esc**. The game timer and all 
  gameplay activity are paused until the game is resumed.

- **Win State** — Activated when all objectives are fulfilled and the player 
  accesses the Exit. A win screen is displayed.

- **Game Over State** — Triggered when time runs out, wildlife touches a player
  or reaches a plant. A game over screen is shown.



## Audio System -----------------------------------------------------------


- Background music used in the main menu

- Sound effects are played during key interactions, including:
    - Clearing debris
    - Collecting tools
    - Planting and harvesting crops
    - Wildlife encounters
    - Winning and losing the game
