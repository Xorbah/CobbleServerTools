import com.cobblemon.mod.common.pokemon.Pokemon;
import net.cobbleservertools.roaming.PokerusCompat;

public final class PokerusCompatTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        boolean present = args[0].equals("present");
        System.setProperty("test.pokerus.loaded", Boolean.toString(!args[0].equals("absent")));
        check(PokerusCompat.isLoaded() == !args[0].equals("absent"), "Incorrect toggle availability");
        Pokemon pokemon = new Pokemon();
        check(!PokerusCompat.apply(pokemon, false), "Disabled setting must do nothing");
        check(pokemon.infectionCalls == 0, "Disabled setting called sidemod");
        check(!PokerusCompat.apply(null, true), "Null Pokémon must be harmless");
        if (present) {
            check(PokerusCompat.apply(pokemon, true), "Sidmod infection was not applied");
            check(pokemon.infected && pokemon.infectionCalls == 1, "Incorrect infection call");
            Pokemon rejected = new Pokemon(); rejected.reject = true;
            check(!PokerusCompat.apply(rejected, true), "Cannot claim infection when sidemod rejects it");
            Pokemon failing = new Pokemon(); failing.fail = true;
            check(!PokerusCompat.apply(failing, true), "API failure must not crash spawning");
        } else {
            check(!PokerusCompat.apply(pokemon, true), "Absent/unavailable API must not invent an infection");
            check(!pokemon.infected && pokemon.infectionCalls == 0, "Unexpected infection mutation");
        }
        System.out.println("Pokérus compatibility test passed: " + args[0]);
    }
}
