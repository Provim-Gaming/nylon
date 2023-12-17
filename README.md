# Nylon

It's a library for creating serverside content using custom models and animations, without requiring client mods.\
Nylon is a bridge between models created with [Animated Java](https://animated-java.dev/docs/home) and serverside
content (like entities and blocks) written using [Polymer](https://github.com/Patbox/polymer), allowing you to combine
fancy custom models, textures and animations made in [Blockbench](https://www.blockbench.net/) with completely custom
block / mob logic.

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
- Minimal impact on server performance. Nylon is highly optimized and models are updated asynchronously. Nylon also
  makes full use of vanilla's packet flush suspending to reduce network load and ping spikes.


- Support for most Animated Java features:
    - Variants: Ability to instantly switch between different models and textures.
    - Locators: Can be used to listen for pose updates on a specific part of the model. These listeners
      can be used to add extra animated objects to the model, such as particles and other entities.
    - Animation frame effects: Certain effects that can be conditionally applied on a specific frame of an animation.
      For example, running a command, playing a sound or changing the variant.
    - Many others like bone blacklists, animation loop modes, start and loop delays, etc.


- Out of the box support for many vanilla mob features:
    - Vanilla accurate hitboxes visible in F3+B, using interactions.
    - The ability to ride on top of the mob, without visually lagging behind.
    - Name tags that work just like vanilla mobs, without text display entities.
    - Working invisibility, glowing, fire animation and most particles (like potion effects, crits and death).
    - Correctly rendering leashes, death animations and smooth clientside collisions with players.
    - Dynamic hitboxes and dynamic mob scale (an example use case of this is baby mobs).
    - The model won't tick if the entity wasn't ticked, reducing server and network load.
    - Uses the display entities culling boxes to reduce client lag.

## Commands

- `/nylon model create id|filepath <model>` - Spawns a model ingame based on mob identifier or a file path (from
  server root folder) to the model json file. These models are not saved and are mostly intended for testing.


- `/nylon model <targets> animation|variant|scale <args>` - Modifies the model of any entity selected in `<targets>`
  that has a custom model. Allows you to temporarily change the scale of the model, update the variant and play /
  pause / stop animations. This is also mostly intended for testing and playing with the models.