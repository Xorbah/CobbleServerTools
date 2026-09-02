package net.neoforged.fml;
public final class ModList {
    private static final ModList INSTANCE = new ModList();
    public static ModList get() { return INSTANCE; }
    public boolean isLoaded(String id) {
        if (!id.equals("pokerus")) throw new AssertionError("Unexpected mod ID: " + id);
        return Boolean.getBoolean("test.pokerus.loaded");
    }
}
