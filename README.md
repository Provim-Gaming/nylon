# Nylon

It's a library for creating serverside entities with custom models and animations, without requiring client mods.\
Nylon is a bridge between models created with [Animated Java](https://animated-java.dev/docs/home) and serverside
entities written using [Polymer](https://github.com/Patbox/polymer), allowing you to combine fancy custom models,
textures and animations made in [Blockbench](https://www.blockbench.net/) with completely custom mob logic and AI.

## Setup

```groovy
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    modImplementation "maven.modrinth:nylon:[TAG]"
}
```

## Features

- Smooth and accurate animations, using mounted display entities with configurable rotation interpolation instead of
  armor stands to ensure smooth rotations and that all the model pieces positions are in sync.


- Support for most Animated Java features:
    - Variants: Ability to switch between different models and textures for an entity.
    - Locators: Can be used to listen for pose updates on a specific part of the model. These listeners
      can be used to add extra animated objects to the model, such as particles and other entities.
    - Animation frame effects: Certain effects that can be conditionally applied on a specific frame of an animation.
      For example, running a command, playing a sound or changing the variant.
    - Many others like bone blacklists, animation loop modes, start and loop delays, etc.


- Support for many vanilla mob features:
    - Vanilla accurate hitboxes visible in F3+B, using interactions.
    - The ability to ride on top of the mob, without visually lagging behind.
    - Working invisibility, glowing, fire animation and most particles (like potion effects, crits and death).
    - Correctly rendering leashes, death animations and smooth clientside collisions with players.
    - Dynamic hitboxes and dynamic mob scale (an example use case of this is baby mobs).
    - The model won't tick if the entity wasn't ticked, to reduce server and network load.
    - Uses the display entities culling boxes to reduce client lag.