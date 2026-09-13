# The Cursed Kingdom Game

A turn-based role-playing game (RPG) built with Java and Swing, featuring character creation, inventory management, skill systems, and battle simulation mechanics.

## Overview

The Cursed Kingdom is an object-oriented Java application that demonstrates core software engineering principles including inheritance, polymorphism, composition, serialization, and GUI development. Players can create and manage characters from three distinct classes, equip weapons, acquire skills, collect items, and engage in tactical battles against AI opponents.

## Features

### Character System
- **Three Playable Classes:**
  - **Warrior** – Melee-focused with rage mechanics for increased damage
  - **Mage** – Magic-oriented with mana resource management and spellcasting
  - **Archer** – Ranged specialist with limited arrow supply and restoration capabilities

- **Character Progression**
  - Level-based advancement with experience point (XP) tracking
  - Automatic level-up when XP thresholds are reached
  - Dynamic stat scaling based on character level

### Inventory & Item Management
- Generic inventory system using composition and generics
- Add, remove, and track items with quantity tracking
- Full serialization support for persistence

### Skill & Weapon Systems
- Aggregate skills to characters with customizable power levels
- Weapon hierarchy supporting Swords, Bows, and Staves
- Polymorphic attack calculations with weapon-specific variance
- Dynamic damage calculations incorporating character level and weapon type

### Battle Simulator
- Turn-based combat against AI-controlled enemies
- Player actions: Attack, Use Skill, or Run
- Real-time HP tracking and battle logging
- AI opponent behavior with automatic turns
- XP rewards for defeating enemies

### Data Persistence
- Save and load character rosters to/from file
- Object serialization using Java's ObjectOutputStream/ObjectInputStream
- Full state preservation including inventory, skills, and progression

## Technical Highlights

### Object-Oriented Design
- **Abstract Base Classes:** `Character`, `Weapon`, and `Skill` with polymorphic behavior
- **Interface Implementation:** `Attackable` for standardized combat mechanics
- **Inheritance Hierarchy:** Warriors, Mages, and Archers extend Character with specialized mechanics
- **Composition:** Inventory contains Items; Characters own Skills and Weapons
- **Generics:** Type-safe `ItemSet<T extends Item>` for flexible item management

### Key Patterns
- **Strategy Pattern:** Different attack calculations per character class
- **Factory-like Construction:** Overloaded constructors for flexible object creation
- **Comparable Interface:** Character comparison by level and experience
- **Cloneable Support:** Deep cloning of characters with skill preservation

### GUI Framework
- **Swing-based UI** with CardLayout for multi-panel navigation
- Themed dark interface with crimson accents
- Styled buttons, text fields, and list components
- Modal dialogs for user input and feedback

## Usage

### Running the Application
```bash
javac CursedKingdom.java
java CursedKingdom
