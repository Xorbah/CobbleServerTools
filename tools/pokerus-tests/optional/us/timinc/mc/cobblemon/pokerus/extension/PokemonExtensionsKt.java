package us.timinc.mc.cobblemon.pokerus.extension;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Pair;
public final class PokemonExtensionsKt {
    public static void infectWithPokerus(Pokemon pokemon, Pair<Integer, Integer> spreadFrom) {
        if (spreadFrom != null) throw new AssertionError("Expected sidemod-generated strain/duration");
        pokemon.infectionCalls++;
        if (pokemon.fail) throw new IllegalStateException("Simulated incompatible API");
        pokemon.infected = !pokemon.reject;
    }
    public static boolean hasPokerus(Pokemon pokemon) { return pokemon.infected; }
}
