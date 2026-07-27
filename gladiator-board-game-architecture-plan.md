# Gladiator Board Game System Architecture & Technical Plan

## Overview

This game should be built as a strict separation between a **single authoritative simulation layer** and a **scene/render layer**. The simulation layer owns rules, turn flow, combat, inventory, rewards, and passive buffs. Scenes only present state and dispatch player intents.

The best fit is a **finite state machine** for match flow plus an **event-driven modifier system** for passive Buff Cards, traps, status effects, and map interactions.

---

## 1. Game State & Event Architecture

### Core Design Principles

- One global match state object owns truth.
- Scenes never decide outcomes.
- All random outcomes use a seeded RNG for replay/debugging.
- Passive buffs are registered at pre-match setup and resolve through event hooks.
- Every important gameplay action emits a structured event context.

### Turn Flow State Machine

```js
const GamePhase = {
  PRE_MATCH_DRAFT: 'PRE_MATCH_DRAFT',
  TURN_START: 'TURN_START',
  ROLL_DICE: 'ROLL_DICE',
  MOVE: 'MOVE',
  SPACE_EVENT: 'SPACE_EVENT',
  COMBAT: 'COMBAT',
  MINIGAME: 'MINIGAME',
  SHOP: 'SHOP',
  TURN_END: 'TURN_END',
  MATCH_END: 'MATCH_END',
};
```

Recommended flow:

```text
Pre-Match Draft -> Start Match -> Turn Start -> Roll Dice -> Move
-> Resolve Space -> Branch to Combat / Minigame / Shop / Event
-> End Turn -> Next Player
```

### Global Match State

```js
const gameState = {
  phase: GamePhase.PRE_MATCH_DRAFT,
  turnIndex: 0,
  activePlayerId: null,
  players: [],
  board: {
    tiles: {},
    graph: {},
    adjacency: {},
  },
  rng: {
    seed: 0,
  },
  eventLog: [],
  matchMeta: {
    questProgress: {},
    bossStatus: {},
    turnOrder: [],
  },
};
```

### Player Data Structure

```js
const player = {
  id: 'p1',
  name: 'Gladiator',
  isAI: false,
  hp: 30,
  maxHp: 30,
  atk: 5,
  def: 2,
  speed: 3,
  gold: 10,
  xp: 0,
  level: 1,
  tileId: 'T12',
  buffs: [],
  statusEffects: [],
  inventory: [],
  equipment: {
    weapon: null,
    armor: null,
    trinket: null,
  },
  derived: {
    moveBonus: 0,
    goldMultiplier: 1,
    redTileProtection: false,
  },
};
```

### Board Tile Graph

Use a graph, not a simple linear board, so the map can branch, loop, and contain tactical routes.

```js
const tile = {
  id: 'T12',
  type: 'BLUE', // BLUE, RED, SHOP, EVENT, TRAP, BOSS, QUEST, MINIGAME
  neighbors: ['T11', 'T13'],
  occupancy: [],
  payload: {
    eventId: null,
    combatId: null,
    shopId: null,
  },
};
```

### Passive Buff Catalog

Buffs are selected before the match and then behave like persistent modifiers.

```js
const passiveBuffCard = {
  id: 'buff_001',
  name: 'Arena Patronage',
  description: '2x gold on Blue tiles',
  rarity: 'common',
  stackRule: 'unique',
  triggers: [
    {
      event: 'ON_REWARD_CALCULATE',
      priority: 50,
      modify(context) {
        if (context.tile.type === 'BLUE') {
          context.rewards.gold *= 2;
        }
      },
    },
  ],
};
```

### Inventory Structure

```js
const inventoryItem = {
  id: 'item_heal_01',
  type: 'consumable',
  name: 'Medi-Pack',
  effects: [
    { stat: 'hp', op: 'add', value: 10 },
  ],
};
```

### Event Bus / Hook System

Use a priority-ordered event bus. Each event receives a context object that listeners can inspect or mutate.

```js
class EventBus {
  constructor() {
    this.listeners = new Map();
  }

  on(eventName, handler, priority = 0) {
    if (!this.listeners.has(eventName)) this.listeners.set(eventName, []);
    this.listeners.get(eventName).push({ handler, priority });
    this.listeners.get(eventName).sort((a, b) => b.priority - a.priority);
  }

  emit(eventName, context) {
    const handlers = this.listeners.get(eventName) || [];
    for (const entry of handlers) {
      entry.handler(context);
      if (context.cancelled) break;
    }
    return context;
  }
}
```

### Buff Resolution Example

A tile landing sequence might fire events in the following order:

```js
const context = {
  player,
  tile,
  rewards: { gold: 0, xp: 0, items: [] },
  penalties: { goldLost: 0, hpLost: 0 },
  cancelled: false,
};

eventBus.emit('ON_TILE_LAND_PRE', context);
eventBus.emit('ON_TILE_LAND', context);
eventBus.emit('ON_REWARD_CALCULATE', context);
eventBus.emit('ON_TILE_LAND_POST', context);
```

This lets you support:

- Gold/XP multipliers.
- Trap resistance.
- Shop price modifiers.
- Same-tile combat triggers.
- Boss arena overrides.
- Quest progression bonuses.

---

## 2. Architecture & Scene Breakdown

### Core Rule

Scenes are presentation and input surfaces only. The simulation layer owns truth.

### Recommended Scenes

- `MainMenuScene` - profile select, load/save, settings, tutorial.
- `CharacterSelectScene` - gladiator selection, team count, AI/human assignment.
- `BuffDraftScene` - pre-match passive buff selection.
- `BoardScene` - dice, movement, board camera, tile highlights.
- `BattleScene` - turn-based combat.
- `MinigameScene` - isolated sub-game loop.
- `ShopOverlay` - buy/sell interface and price modifiers.
- `SpaceResolveOverlay` - event/trap/quest text and outcomes.
- `ResultsScene` - final ranking and rewards.

### Global Session Model

Keep one persistent `GameSession` and change only active scenes.

```js
const gameSession = {
  state: gameState,
  eventBus,
  sceneManager,
  saveService,
};
```

### Transition Strategy

- Use hard scene swaps for board/combat/minigame transitions.
- Use overlays for shop or event prompts.
- Preserve one shared global match state across all scenes.
- Keep scene-local state only for animation and UI controls.

```js
function onTileResolved(tileResult) {
  if (tileResult.type === 'COMBAT') {
    sceneManager.push('BattleScene');
  } else if (tileResult.type === 'SHOP') {
    sceneManager.push('ShopOverlay');
  } else if (tileResult.type === 'MINIGAME') {
    sceneManager.push('MinigameScene');
  } else {
    advanceTurn();
  }
}
```

### What Must Persist Globally

- Player HP, gold, XP, buffs, statuses, inventory.
- Board positions and occupied tiles.
- Quest progress and boss state.
- Turn order and phase.
- Seeded RNG state.

---

## 3. Step-by-Step Development Phases

### Phase 1: Core Board Mechanics

Goal: create a playable turn loop.

Tasks:

- Build the board as a graph of connected tiles.
- Implement seeded dice rolling.
- Implement path-based movement across board edges.
- Add the match state machine.
- Add landing resolution for basic tile types.
- Add end-turn and next-player logic.

Deliverable:

A player can roll, move, land, and end turns on a functional board.

### Phase 2: Pre-Match Draft & Buff System

Goal: make passive setup meaningful.

Tasks:

- Build the passive buff catalog as data.
- Add a pre-match drafting UI.
- Register selected buffs into the event bus.
- Implement trigger categories like `ON_TILE_LAND`, `ON_REWARD_CALCULATE`, `ON_COMBAT_START`, and `ON_TURN_START`.
- Add stacking, uniqueness, and exclusivity rules.
- Make tile-based modifiers affect rewards and penalties.

Deliverable:

Players choose passives before the match, and those passives continuously alter gameplay.

### Phase 3: Combat System

Goal: make same-tile conflicts and bosses strategic.

Tasks:

- Build turn-based combat flow.
- Add stats, actions, and combat resolution.
- Add status effects and turn-duration modifiers.
- Add mini-boss and boss templates.
- Add combat event hooks for passive buffs.
- Support combat from tile-sharing, boss arenas, and scripted encounters.

Recommended flow:

```text
Combat Start -> Initiative -> Player Action -> Enemy Action -> Status Tick -> Victory/Defeat -> Rewards
```

Deliverable:

Combat resolves player-vs-player and player-vs-boss encounters, with buffs influencing outcomes.

### Phase 4: Event Spaces & Minigames

Goal: increase map variety and tactical sabotage.

Tasks:

- Add story/event tiles with branching outcomes.
- Add trap tiles with penalties and disruptions.
- Add shop tiles with item economy.
- Add minigame launch and return flow.
- Return rewards back into the main match economy.
- Use board space interactions, not hand cards, for sabotage.

Deliverable:

Landing on spaces creates strategic consequences and board pressure.

### Phase 5: Basic Rule-Based AI

Goal: support full matches with AI opponents.

Tasks:

- Build movement and target-selection heuristics.
- Add AI pre-match buff drafting.
- Add AI shop, combat, and item-use behavior.
- Add AI aggression around shared tiles.
- Add quest and boss prioritization logic.

AI decision inputs:

- Current HP and danger threshold.
- Distance to quest objective or boss.
- Nearby enemy threat level.
- Synergy between buffs and tile types.
- Available gold versus shop value.

Deliverable:

AI players can draft, move, fight, shop, and pursue the win condition competently.

---

## 4. Recommended Project File Structure

```text
src/
  app/
    GameApp.js
    GameSession.js
    SceneManager.js
    Config.js

  scenes/
    MainMenuScene.js
    CharacterSelectScene.js
    BuffDraftScene.js
    BoardScene.js
    BattleScene.js
    MinigameScene.js
    ShopOverlay.js
    ResultsScene.js

  state/
    store.js
    reducers/
    actions/
    selectors/

  systems/
    eventBus.js
    stateMachine.js
    rng.js
    combatSystem.js
    movementSystem.js
    rewardSystem.js
    aiSystem.js
    saveSystem.js

  data/
    boards/
    tiles/
    buffs/
    items/
    enemies/
    bosses/
    minigames/
    quests/
    dialogue/

  ui/
    components/
    hud/
    overlays/
    menus/
    cards/

  rendering/
    boardRenderer.js
    battleRenderer.js
    animations/
    camera.js
    effects/

  assets/
    sprites/
    audio/
    fonts/
    ui/

  utils/
    math.js
    validation.js
    clone.js
```

### Separation Rules

- `data/` contains content.
- `systems/` contains game rules.
- `scenes/` contains presentation and input.
- `state/` contains authoritative runtime state.
- `rendering/` contains visuals only.

---

## 5. Implementation Recommendations

- Use seeded RNG so behavior is reproducible.
- Make tile resolution idempotent so rewards are not double-applied.
- Keep buff logic data-driven instead of hardcoding each passive in scenes.
- Use explicit phase transitions instead of implicit scene callbacks.
- Log every resolved event into an `eventLog` for balancing and debugging.
- Consider Phaser 3 if you want faster scene management and sprite tooling.
- Consider a custom canvas/WebGL shell if you want maximum control and minimal framework overhead.

---

## 6. Practical Buff Engine Pattern

A good long-term model is to treat buffs as modifier registries rather than one-off effects.

```js
const buffCatalog = {
  buff_gold_blue: {
    id: 'buff_gold_blue',
    name: 'Arena Patronage',
    triggers: [
      {
        event: 'ON_REWARD_CALCULATE',
        priority: 50,
        handler(context) {
          if (context.tile.type === 'BLUE') {
            context.rewards.gold *= 2;
          }
        },
      },
    ],
  },
};
```

This is the safest pattern because it keeps the game deterministic, composable, and easy to tune.

---

## 7. Recommended Next Build Order

1. Implement the state machine and board graph.
2. Add seeded dice and movement.
3. Add tile resolution and logging.
4. Add passive buff selection and event hooks.
5. Build combat and shared-tile encounters.
6. Add shops, traps, and minigames.
7. Add AI and balancing tools.

---

## Summary

The correct architecture for this game is:

- One authoritative simulation state.
- One event-driven buff and modifier layer.
- Multiple scenes that only render and request actions.
- A graph-based board with modular tile effects.
- Pre-match passive Buff Cards instead of a mid-match hand system.
- Shared-tile combat and board sabotage to drive player interaction.

This gives you a structure that is scalable, debuggable, and well suited for a browser-based turn-based board game with RPG systems.
