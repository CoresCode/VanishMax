package dev.corescode.vanishmax;

/**
 * Represents one configured permission level from config.yml.
 *
 * Example config entry:
 *   moderator:
 *     rank: 1
 *
 * Permission node: invis.moderator
 */
public class VanishLevel {

    private final String name;
    private final int rank;
    private final String permission;

    public VanishLevel(String name, int rank) {
        this.name       = name;
        this.rank       = rank;
        this.permission = "invis." + name;
    }

    public String getName()       { return name; }
    public int getRank()          { return rank; }
    public String getPermission() { return permission; }

    @Override
    public String toString() {
        return "VanishLevel{name=" + name + ", rank=" + rank + ", perm=" + permission + "}";
    }
}