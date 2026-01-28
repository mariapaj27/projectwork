/* ----------------------------------------------------------------------
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


## How to Run the Game ---------------------------------------------------


1. Clone or open the project.
2. Ensure **Java 17** is installed
3. From the project root, run:
    - **Windows / Linux:** `./gradlew desktop:run`
    - **macOS:** `./gradlew desktop:run`
4. From the main menu, load one of the map (`.properties` files) to start a game.


## Game Rules --------------------------------------------------------


1. Pick the **Shovel** and collect at least **6 Debris**
2. Collect **Fertilizer** and **6 Seeds** (seeds appear after the debris and fertilizer collected)
3. **Plant** seeds on garden beds
4. **Protect plants** from Wildlife; find the **Watering Can** under
   random Debris to restore rotten plants
5. **Collect all 6 plants** and do not get hit by Wildlife
6. When all requirements are met, go to the **Exit** and win


## Controls --------------------------------------------------------------


- **Arrow Keys** — Move player(up,left,right,down)
- **D (hold)** — Clear debris or break seeds (faster with Shovel)
- **E** — Pick up Shovel, Fertilizer, or Watering Can when facing them
- **A** — Plant seed / harvest plant / restore rotten plant
- **S** — Shout to scare nearby Wildlife
- **Esc** — Pause, Resume or Back to menu


## Core Gameplay Systems --------------------------------------------------------


### Player Tools
- **Shovel** - On the map (value 7). Speeds up  destruction of debris

- **Fertilizer** - Appears at (9,1) after 6 debris collected. Unlocks seed
  spawning

- **Watering Can** - Hidden under one random Debris at game start; appears when
  that Debris is cleared. Restores rotten plants


### Win Conditions
- **Exit** - On the map or under one random Debris (if no exit in the map).
  Becomes passable when you have Shovel, Fertilizer, Watering Can, 6 debris, and
  6 plants
-

### Failure Conditions
- **Time** - A 5‑minute countdown; at zero you lose

- **Rotting** - Mature plants rotten if not harvested in time; use the
  Watering Can to restore them

- **Wildlife** - Move toward plants (or the player if no plants). If they
  touch the player or reach a plant, game is over



## Bonus Features --------------------------------------------------------


- **Daylight darkening** - As the timer runs down, a dark overlay gradually
  increases (e.g. from 5:00 toward 1:30), then stays at a fixed level until time
  runs out. Pause freezes the timer and the darkening.

- **Diagonal movement** - The farmer can move in eight directions; diagonal
  input is normalized so speed stays consistent (when two arrows pressed simultaneously)

- **Dynamic wildlife spawning** — Wildlife appear randomly after seeds are
  planted rather than spawning in fixed positions.

- **Wildlife unstuck logic** - When Wildlife get stuck (e.g. between debris),
  they use simple obstacle-avoidance: they try several escape directions
  (perpendicular, diagonal, axis-only) and alternate strategies over time so they
  can work their way around blockages instead of staying stuck

- **Context hints** - Context hints appear when you are close and facing
  something interactable: e.g. “Press E to take the shovel/fertilizer/watering
  can”, or “Press A to plant / harvest / restore”. The hint area uses the same
  brown panel style as the HUD



## Authors ---------------------------------------------------------------


- **Maria Pajkusz**

- **Adel Daulet** 






