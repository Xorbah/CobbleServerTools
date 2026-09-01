import java.util.List;
import net.crulim.kantonpcs.client.gui.RoamingPokemonVariantSupport;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;

public final class Rc27VariantSupportTest {
    public static void main(String[] args) {
        List<RoamingPokemonVariantSupport.FormChoice> vulpix = RoamingPokemonVariantSupport.listForms("vulpix");
        if (vulpix.stream().noneMatch(f -> f.formId().equals("alola") && f.requiredAspects().contains("alolan")))
            throw new AssertionError("Alolan Vulpix formal form regression");

        List<RoamingPokemonVariantSupport.FormChoice> pika = RoamingPokemonVariantSupport.listForms("pikachu");
        RoamingPokemonVariantSupport.FormChoice fat = pika.stream().filter(f -> f.label().equalsIgnoreCase("Fat")).findFirst().orElseThrow();
        if (!fat.formId().equals("fat") || !fat.variantProperty().equals("fat=fat") || !fat.requiredAspects().contains("fat"))
            throw new AssertionError("Fat Pikachu form/feature merge failed: " + fat.formId() + " / " + fat.variantProperty());
        if (pika.stream().noneMatch(f -> f.variantProperty().equals("fat=bug") && f.requiredAspects().contains("bug")))
            throw new AssertionError("Assigned Pikachu feature choices were not discovered");
        if (pika.stream().anyMatch(f -> f.variantProperty().equals("fat=no")))
            throw new AssertionError("Feature default should collapse into Default / Standard");

        List<RoamingPokemonVariantSupport.FormChoice> resolver = RoamingPokemonVariantSupport.listForms("resolvermon");
        RoamingPokemonVariantSupport.FormChoice chunky = resolver.stream().filter(f -> f.variantProperty().equals("coat=chunky")).findFirst().orElseThrow();
        if (!chunky.formId().isBlank() || !chunky.requiredAspects().equals("chunky-coat") || !chunky.kind().equals("Variant"))
            throw new AssertionError("Aspect-only resolver variant was not represented independently of FormData");

        RenderablePokemon preview = RoamingPokemonVariantSupport.buildRenderable("resolvermon", "", chunky.requiredAspects(), true);
        if (preview == null || !preview.getAspects().contains("chunky-coat") || !preview.getAspects().contains("shiny"))
            throw new AssertionError("Variant live preview did not receive resolver aspect + shiny");

        if (!RoamingPokemonVariantSupport.splitProperties("coat=chunky bad fat=gojo").equals(List.of("coat=chunky","fat=gojo")))
            throw new AssertionError("Variant property sanitization failed");

        System.out.println("RC27 form + species-feature variant tests passed");
    }
}
