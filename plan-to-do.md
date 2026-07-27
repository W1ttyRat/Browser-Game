Build the core game shell.

Set up the project, scene system, and a single global GameSession.
Define the match phases: draft, turn start, roll, move, resolve, end turn.
Add a seeded RNG from day one.
Implement the board and movement.

Build the board as a graph of tiles, not a linear path.
Add dice rolling, step-by-step movement, and turn advancement.
Let a player land on a tile and trigger a basic resolution event.
Add the event system.

Create an EventBus with ordered hooks like ON_TILE_LAND, ON_REWARD_CALCULATE, and ON_COMBAT_START.
Make tile effects, traps, and rewards run through this system.
Log every resolved event for debugging.
Add passive Buff Cards.

Build the pre-match draft UI.
Make buffs data-driven and persistent for the whole match.
Test a few simple passives like gold multipliers, movement bonuses, and trap immunity.
Build combat.

Start with simple turn-based combat between two players or player vs enemy.
Add stats, damage, defense, and status effects.
Then add bosses and same-tile combat triggers.
Add board variety.

Implement event tiles, shops, traps, and quest spaces.
Add minigame entry/exit flow as a separate sub-state.
Make board sabotage happen through space interactions instead of action cards.
Add AI.

First make AI able to roll, move, and end turns.
Then add simple heuristics for buff drafting, combat choices, and shop spending.
Finally teach it to pursue quests and avoid danger.
Polish the loop.

Add UI feedback, animations, sound, and balancing.
Tune reward rates, buff strength, and combat pacing.
Add save/load and then online backend if needed.
If you want a strict next-10-tasks version, I’d do this order first:

Create project structure
Build GameSession
Build StateMachine
Build board graph
Build dice + movement
Build tile landing resolution
Add EventBus
Add passive buff draft
Add combat
Add AI