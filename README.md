# Krillion Defense

CSEN 160 project. A tower defense game in Java Swing where trivia questions pop up in the middle of
each wave, and the more obscure your answer is, the better the power-up you get.
See `DESIGN.md` for the original design.

## How to run

Needs Java 17 or newer.

**Option 1 - script (no Maven needed):**
```
cd krillion-defense
./run.sh
```

**Option 2 - IntelliJ:** right click `krillion-defense/pom.xml` -> "Add as Maven Project",
then run `krilliondefense.ui.Main`. Run the tests by right clicking `src/test/java` -> Run 'All Tests'.

**Option 3 - Maven:** `mvn compile exec:java` to play, `mvn test` for the tests.

## How to play

1. Pick a difficulty on the menu and click **Start**.
   - **Easy:** 1 path, 1 base, 100 gold, 20 lives
   - **Medium:** 2 paths into 1 base, 150 gold, 20 lives, more enemies
   - **Hard:** 3 paths and 2 bases, 250 gold, 15 lives, the most enemies and the fastest spawns
2. **Build your defense.** Click a tower in the shop at the bottom (or press 1-4), then click a grass tile.
   Green = you can build there, red = you can't. The circle shows its range.
3. Click any tower on the map to see its stats and **Upgrade** or **Sell** it (sell asks you to click twice).
4. Press **Start Wave** (or Space). Enemies walk the path and each one that reaches a base costs a life.
5. **Questions pop up during the wave** (1 per wave at first, 2 from wave 4). The game pauses, you get
   30 seconds, and the rarer your answer the better the reward:

   | Level | Reward |
   | --- | --- |
   | Common | +25 gold |
   | Uncommon | +60 gold |
   | Rare | Towers do +50% damage for the rest of the wave |
   | Epic | Freeze every enemy for 6 seconds |
   | Legendary | 200 damage to every enemy + unlocks the Scholar Tower |

6. Beat all 8 waves (the last one has a Boss) to win.

Other controls: S = 2x speed, Esc or right click = cancel.

## What's in the game
- 3 difficulties with 3 different maps
- 4 towers: Arrow, Frost (slows), Cannon (splash), Scholar (gets stronger with knowledge points)
- 4 enemies: Basic, Fast, Tank, Boss
- 42 questions with about 4,000 answers in `src/main/resources/answers.csv`. Every answer has a level from
  1 (most common) to 5 (rarest). Typos and other names (like "Swaziland" for Eswatini) still count.

## Changes from DESIGN.md
- Answers have a fixed level 1-5 instead of a count that changes, and there's a 5th tier (Epic).
- Each tier always gives the same reward, so you know what you're playing for.
- Questions come during waves instead of at the start of the build phase, with a 30 second timer.
- 8 waves instead of 10, and difficulties/maps were added.
- UML diagrams (class, sequence and activity) are in `docs/uml` as .puml source + .png.
