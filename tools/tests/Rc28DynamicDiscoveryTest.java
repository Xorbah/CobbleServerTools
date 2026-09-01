import java.util.*;
import net.crulim.kantonpcs.client.gui.RoamingPokemonVariantSupport;

public final class Rc28DynamicDiscoveryTest {
    public static void main(String[] args) {
        var pika = RoamingPokemonVariantSupport.listForms("pikachu");
        if (pika.stream().noneMatch(x -> x.formId().equals("fat") && x.variantProperty().equals("fat=fat")))
            throw new AssertionError("live datapack form/variant discovery regression");
        String a = RoamingPokemonVariantSupport.catalogFingerprint(pika);
        String b = RoamingPokemonVariantSupport.catalogFingerprint(new ArrayList<>(pika));
        if (!a.equals(b) || a.isBlank()) throw new AssertionError("stable catalog fingerprint failed");
        var changed = new ArrayList<>(pika);
        changed.add(new RoamingPokemonVariantSupport.FormChoice("Injected Test", "injected", "injected-aspect", "", "Form"));
        if (a.equals(RoamingPokemonVariantSupport.catalogFingerprint(changed)))
            throw new AssertionError("catalog changes were not detectable");
        System.out.println("RC28 dynamic discovery tests passed");
    }
}
