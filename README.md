# VanishMax

A hierarchical staff vanish plugin for Paper and Folia.

VanishMax lets you define multiple vanish "levels" (e.g. Moderator, Admin, Head Admin, Developer), each with its own rank. A vanished player stays hidden from anyone ranked lower than them, but stays visible — tagged `[Vanish]` — to anyone at an equal or higher rank. This means your staff team can always see who's actually online, while regular players and lower staff never know.

## Features

- Hierarchical, fully configurable vanish levels
- Damage immunity while vanished (all causes except `/kill`)
- Mobs ignore vanished players entirely
- Auto Fire Resistance + Night Vision while vanished, restored correctly on unvanish
- Vanish state persists across restarts/relogs
- PlaceholderAPI support (`%vanishmax_vanished%`)
- Built on the Folia scheduler API — works on Paper and Folia with no separate builds

## Installation

1. Download `VanishMax-1.0.0.jar` from [Modrinth](#) / [CurseForge](#).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. Edit `plugins/VanishMax/config.yml` to set up your vanish levels.
5. Grant the generated permissions (`vanishmax.<level-name>`) via LuckPerms or your permissions plugin.
6. Run `/vanishmax reload` after any config changes — no restart needed.

## Commands

| Command | Aliases | Description |
|---|---|---|
| `/invis` | `/vanish`, `/v` | Toggle vanish mode |
| `/vanishmax reload` | — | Reload the config |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `vanishmax.reload` | op | Allows reloading the config |
| `vanishmax.<level-name>` | false | Grants a specific vanish level, e.g. `vanishmax.moderator` |

Levels and their ranks are defined in `config.yml` — add or remove as many as you want. Permissions are generated automatically from the level name.

## Building from source

```bash
mvn clean package
```

The compiled jar will be at `target/plugin/VanishMax-1.0.0.jar`.

**Requirements:** Java 21, Maven.

## Dependencies

- **Required:** Paper or Folia 1.21+
- **Optional:** PlaceholderAPI (enables `%vanishmax_vanished%`)
- **Recommended:** LuckPerms, for managing vanish-level permissions

## License

[MIT]

## Author

CoresCode — [GitHub](https://github.com/CoresCode)