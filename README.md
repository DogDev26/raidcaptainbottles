# Raid Captain Bottles

A server-side Fabric mod for Minecraft **26.1.2** that makes raid captains
always drop an Ominous Bottle when a player kills them - including mid-raid -
no matter which illager type (pillager, vindicator, evoker, or illusioner)
happens to be carrying the ominous banner that turn.

## Why this is needed

Since the "ominous bottle" rework, a raid captain already drops an Ominous
Bottle when killed by a player **outside** an active raid. The one gap is
a captain killed **during** an active raid wave - vanilla doesn't drop a
bottle there. This mod only fills in that missing case; it doesn't touch
or duplicate the existing outside-raid behavior.

The bottle's amplifier (Bad Omen/Raid Omen level I-V) is derived from the
raid's current omen level, clamped to the bottle's valid range, so it scales
the same way vanilla's own bottles do.

## Requirements

| Component      | Version                    |
|-----------------|----------------------------|
| Minecraft       | 26.1.2                     |
| Fabric Loader   | 0.19.3 (or newer)          |
| Fabric API      | 0.155.0+26.1.2 (or newer)  |
| Java            | 25 (mandatory for 26.1+)   |

Double-check the exact recommended Fabric Loader / Fabric API / Loom
versions at https://fabricmc.net/develop/ before building - these move
fast and the ones pinned in `gradle.properties` may have been superseded
by the time you build this.

## Building

```
./gradlew build
```

You'll need a Gradle wrapper; if this project doesn't already have one,
run `gradle wrapper --gradle-version latest` once (with any local Gradle
install) to generate it. The built jar lands in `build/libs/`.

Requires JDK 25 on the machine you build with.

## Installing

Drop the built jar and a matching Fabric API jar into your server's (or
client's, though this mod is server-side-only logic) `mods/` folder.

## A note on how this was built

Minecraft 26.1's raid/captain internals are quite new (this version ships
unobfuscated, official-Mojang-mapped code), so rather than reaching into
private engine internals with mixins - which would be brittle and could
silently break on the next patch - this mod uses only Fabric API's public
`ServerLivingEntityEvents.AFTER_DEATH` event plus stable, long-standing
public methods (`Raider#isPatrolLeader()`, `ServerLevel#getRaidAt(...)`,
`Raid#getRaidOmenLevel()`). That keeps it robust and easy to read, at the
cost of not being a one-line data pack tweak. If a future snapshot renames
one of these methods, the compile error will point straight at the line
to fix.
