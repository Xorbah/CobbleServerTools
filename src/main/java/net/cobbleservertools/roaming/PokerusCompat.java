package net.cobbleservertools.roaming;

import net.neoforged.fml.ModList;

/** Optional integration with timinc's Pokérus sidemod; never fabricates infection data. */
public final class PokerusCompat {
    private static final String EXTENSIONS =
        "us.timinc.mc.cobblemon.pokerus.extension.PokemonExtensionsKt";
    private static final System.Logger LOGGER = System.getLogger(PokerusCompat.class.getName());

    private PokerusCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("pokerus");
    }

    /** Returns true only when the installed sidemod confirms an active infection. */
    public static boolean apply(Object pokemon, boolean requested) {
        if (!requested || pokemon == null || !isLoaded()) return false;
        try {
            Class<?> extensions = Class.forName(EXTENSIONS);
            Class<?> pokemonType = Class.forName("com.cobblemon.mod.common.pokemon.Pokemon");
            Class<?> pairType = Class.forName("kotlin.Pair");
            // A null spread source asks the sidemod to generate its normal strain/duration.
            extensions.getMethod("infectWithPokerus", pokemonType, pairType)
                .invoke(null, pokemon, null);
            boolean infected = (Boolean) extensions.getMethod("hasPokerus", pokemonType)
                .invoke(null, pokemon);
            if (!infected) LOGGER.log(System.Logger.Level.WARNING,
                "Pokérus did not infect the roaming Pokémon; check the sidemod's marks and configuration.");
            return infected;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                "Could not apply Pokérus to a roaming Pokémon using the installed sidemod.", e);
            return false;
        }
    }
}
