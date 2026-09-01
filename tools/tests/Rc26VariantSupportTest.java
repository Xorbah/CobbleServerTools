import java.util.List;
import net.crulim.kantonpcs.client.gui.RoamingPokemonVariantSupport;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;

public final class Rc26VariantSupportTest {
    public static void main(String[] args) {
        List<RoamingPokemonVariantSupport.FormChoice> forms = RoamingPokemonVariantSupport.listForms("vulpix");
        boolean found = forms.stream().anyMatch(f ->
            f.formId().equals("alola") && f.requiredAspects().contains("alolan"));
        if (!found) throw new AssertionError("Alolan Vulpix form was not discovered: " + forms.size());

        RenderablePokemon normal = RoamingPokemonVariantSupport.buildRenderable("vulpix", "", "", false);
        if (normal == null || normal.getAspects().contains("alolan"))
            throw new AssertionError("Default Vulpix preview incorrectly contains alolan aspect");

        RenderablePokemon alola = RoamingPokemonVariantSupport.buildRenderable("vulpix", "alola", "", false);
        if (alola == null || !alola.getAspects().contains("alolan"))
            throw new AssertionError("Alolan Vulpix preview did not receive alolan aspect");

        RenderablePokemon shinyAlola = RoamingPokemonVariantSupport.buildRenderable("vulpix", "alola", "", true);
        if (!shinyAlola.getAspects().contains("alolan") || !shinyAlola.getAspects().contains("shiny"))
            throw new AssertionError("Shiny Alolan Vulpix preview lost shiny/form aspects");

        String custom = RoamingPokemonVariantSupport.customOnly("alolan,radiant-a", "alolan");
        if (!custom.equals("radiant-a"))
            throw new AssertionError("RC25 combined aspect migration failed: " + custom);

        System.out.println("RC26 variant support tests passed");
    }
}
