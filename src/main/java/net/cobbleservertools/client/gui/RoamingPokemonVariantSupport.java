package net.cobbleservertools.client.gui;

import java.util.*;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.ChoiceSpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.cobblemon.mod.common.pokemon.Species;

/**
 * Cobblemon 1.7.3 form + aspect-variant support for roaming Pokemon.
 *
 * Formal forms come from Species#getForms(). Datapack/model variants also come
 * from assigned ChoiceSpeciesFeatureProvider values when the feature is an
 * aspect provider. That covers packs where a resolver selects a completely
 * different model/texture using a choice such as `fat=fat` without relying on
 * a formal FormData entry.
 */
public final class RoamingPokemonVariantSupport {
    private RoamingPokemonVariantSupport() {}

    public static final class FormChoice {
        private final String label;
        private final String formId;
        private final String requiredAspects;
        private final String variantProperty;
        private final String kind;

        public FormChoice(String label, String formId, String requiredAspects) {
            this(label, formId, requiredAspects, "", "Form");
        }

        public FormChoice(String label, String formId, String requiredAspects,
                          String variantProperty, String kind) {
            this.label = label == null || label.isBlank() ? "Default / Standard" : label;
            this.formId = formId == null ? "" : formId;
            this.requiredAspects = requiredAspects == null ? "" : requiredAspects;
            this.variantProperty = variantProperty == null ? "" : variantProperty;
            this.kind = kind == null || kind.isBlank() ? "Form" : kind;
        }

        public String label() { return label; }
        public String formId() { return formId; }
        public String requiredAspects() { return requiredAspects; }
        public String variantProperty() { return variantProperty; }
        public String kind() { return kind; }
    }

    /**
     * Returns formal Cobblemon forms plus synchronized assigned aspect-choice
     * variants for the selected species. Feature choices are merged into a
     * matching formal form when both represent the same aspect, avoiding
     * duplicate "Fat" / "Fat" entries for packs that define both layers.
     */
    public static List<FormChoice> listForms(String species) {
        List<FormChoice> out = new ArrayList<>();
        out.add(new FormChoice("Default / Standard", "", "", "", "Default"));

        RenderablePokemon renderable = baseRenderable(species, false);
        if (renderable == null || renderable.getSpecies() == null) return out;
        Species sp = renderable.getSpecies();

        FormData standard = sp.getStandardForm();
        List<FormData> forms = sp.getForms();
        if (forms != null) {
            Set<String> seen = new LinkedHashSet<>();
            seen.add("");
            for (FormData form : forms) {
                if (form == null || form == standard || form.equals(standard)) continue;
                String id = cleanToken(form.formOnlyShowdownId());
                if (id.isBlank() || !seen.add(id)) continue;
                String name = form.getName();
                String aspects = joinStrings(form.getAspects());
                out.add(new FormChoice(
                    name == null || name.isBlank() ? friendly(id) : name,
                    id, aspects, "", "Form"
                ));
            }
        }

        // SpeciesFeatures#getFeaturesFor is the live synchronized feature view,
        // including species_feature_assignments supplied by datapacks/addons.
        try {
            List<SpeciesFeatureProvider<?>> providers = SpeciesFeatures.getFeaturesFor(sp);
            if (providers != null) {
                for (SpeciesFeatureProvider<?> provider : providers) {
                    if (!(provider instanceof ChoiceSpeciesFeatureProvider choice)) continue;
                    if (!choice.isAspect()) continue;
                    List<String> keys = choice.getKeys();
                    List<String> values = choice.getChoices();
                    List<String> aspects = choice.getAllAspects();
                    if (keys == null || keys.isEmpty() || values == null || values.isEmpty()) continue;
                    String key = cleanPropertyKey(keys.get(0));
                    if (key.isBlank()) continue;
                    String defaultValue = cleanPropertyValue(choice.getDefault());

                    for (int i = 0; i < values.size(); i++) {
                        String value = cleanPropertyValue(values.get(i));
                        if (value.isBlank()) continue;
                        String aspect = i < aspects.size() ? cleanToken(aspects.get(i)) : value;
                        String property = key + "=" + value;

                        int match = findMatchingFormal(out, value, aspect);
                        if (match >= 0) {
                            FormChoice old = out.get(match);
                            out.set(match, new FormChoice(
                                old.label(), old.formId(), old.requiredAspects(),
                                mergeProperties(old.variantProperty(), property), "Form + Variant"
                            ));
                            continue;
                        }

                        // A provider's default choice normally corresponds to
                        // the ordinary/base appearance, already represented by
                        // Default / Standard. Avoid a duplicate "no" row.
                        if (!defaultValue.isBlank() && value.equals(defaultValue)) continue;

                        if (hasEquivalentVariant(out, property, aspect)) continue;
                        out.add(new FormChoice(
                            friendly(value), "", aspect, property, "Variant"
                        ));
                    }
                }
            }
        } catch (Throwable ignored) {
            // Forms remain usable even if a third-party provider is malformed.
        }

        return out;
    }

    /** Stable fingerprint of the current live form/variant catalog. */
    public static String catalogFingerprint(List<FormChoice> choices) {
        if (choices == null || choices.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (FormChoice c : choices) {
            if (c == null) continue;
            out.append(c.label()).append('\u001f')
               .append(c.formId()).append('\u001f')
               .append(c.requiredAspects()).append('\u001f')
               .append(c.variantProperty()).append('\u001f')
               .append(c.kind()).append('\u001e');
        }
        return out.toString();
    }

    private static int findMatchingFormal(List<FormChoice> choices, String value, String aspect) {
        for (int i = 1; i < choices.size(); i++) {
            FormChoice c = choices.get(i);
            if (!c.formId().isBlank() && cleanToken(c.formId()).equals(cleanToken(value))) return i;
            if (!aspect.isBlank() && splitAspects(c.requiredAspects()).contains(aspect)) return i;
        }
        return -1;
    }

    private static boolean hasEquivalentVariant(List<FormChoice> choices, String property, String aspect) {
        for (FormChoice c : choices) {
            if (c.variantProperty().equalsIgnoreCase(property)) return true;
            if (!aspect.isBlank() && c.formId().isBlank() && splitAspects(c.requiredAspects()).contains(aspect)) return true;
        }
        return false;
    }

    /** Builds the exact animated preview aspect set ModelWidget consumes. */
    public static RenderablePokemon buildRenderable(String species, String form,
                                                     String chosenAspects, boolean shiny) {
        RenderablePokemon renderable = baseRenderable(species, shiny);
        if (renderable == null) return null;
        LinkedHashSet<String> aspects = new LinkedHashSet<>();
        if (renderable.getAspects() != null) aspects.addAll(renderable.getAspects());
        aspects.addAll(splitAspects(requiredAspectsFor(renderable.getSpecies(), form)));
        aspects.addAll(splitAspects(chosenAspects));
        renderable.setAspects(aspects);
        return renderable;
    }

    public static String requiredAspectsFor(String species, String form) {
        RenderablePokemon renderable = baseRenderable(species, false);
        return renderable == null ? "" : requiredAspectsFor(renderable.getSpecies(), form);
    }

    private static String requiredAspectsFor(Species species, String form) {
        if (species == null || form == null || form.isBlank()) return "";
        String wanted = cleanToken(form);
        List<FormData> forms = species.getForms();
        if (forms == null) return "";
        for (FormData data : forms) {
            if (data == null) continue;
            if (cleanToken(data.formOnlyShowdownId()).equals(wanted)) return joinStrings(data.getAspects());
        }
        return "";
    }

    public static String customOnly(String combined, String required) {
        LinkedHashSet<String> custom = new LinkedHashSet<>(splitAspects(combined));
        custom.removeAll(splitAspects(required));
        return String.join(",", custom);
    }

    public static String displayName(String form, String customAspects, String variantProperties) {
        if (form != null && !form.isBlank()) return friendly(form);
        if (variantProperties != null && !variantProperties.isBlank()) {
            String first = variantProperties.trim().split("\\s+")[0];
            int eq = first.indexOf('=');
            if (eq >= 0 && eq + 1 < first.length()) return friendly(first.substring(eq + 1));
            return friendly(first);
        }
        return customAspects == null || customAspects.isBlank() ? "Default" : "Custom";
    }

    // Retain RC26 binary/source convenience for any overlay code still calling it.
    public static String displayName(String form, String customAspects) {
        return displayName(form, customAspects, "");
    }

    public static String mergeAspects(String required, String custom) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.addAll(splitAspects(required));
        set.addAll(splitAspects(custom));
        return String.join(",", set);
    }

    public static String mergeProperties(String existing, String property) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String p : splitProperties(existing)) if (!p.isBlank()) set.add(p);
        for (String p : splitProperties(property)) if (!p.isBlank()) set.add(p);
        return String.join(" ", set);
    }

    public static List<String> splitProperties(String value) {
        if (value == null || value.isBlank()) return List.of();
        List<String> out = new ArrayList<>();
        for (String raw : value.trim().split("\\s+")) {
            int eq = raw.indexOf('=');
            if (eq <= 0 || eq == raw.length() - 1) continue;
            String key = cleanPropertyKey(raw.substring(0, eq));
            String val = cleanPropertyValue(raw.substring(eq + 1));
            if (!key.isBlank() && !val.isBlank()) out.add(key + "=" + val);
        }
        return out;
    }

    private static RenderablePokemon baseRenderable(String species, boolean shiny) {
        try {
            PokemonProperties props = new PokemonProperties();
            props.setSpecies(normalizeSpecies(species));
            props.setShiny(Boolean.valueOf(shiny));
            props.updateAspects();
            return props.asRenderablePokemon();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String joinStrings(Iterable<String> value) {
        if (value == null) return "";
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String x : value) {
            String cleaned = cleanToken(x);
            if (!cleaned.isBlank()) out.add(cleaned);
        }
        return String.join(",", out);
    }

    static List<String> splitAspects(String value) {
        if (value == null || value.isBlank()) return List.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String s : value.split("[,;\\s]+")) {
            s = cleanToken(s);
            if (!s.isBlank()) out.add(s);
        }
        return new ArrayList<>(out);
    }

    private static String normalizeSpecies(String species) {
        String s = species == null || species.isBlank() ? "suicune" : species.trim();
        return s.replace(' ', '_');
    }

    private static String cleanToken(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_:\\-.]", "");
    }

    private static String cleanPropertyKey(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_:\\-.]", "");
    }

    private static String cleanPropertyValue(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_:\\-.]", "");
    }

    private static String friendly(String id) {
        if (id == null || id.isBlank()) return "Default";
        String s = id.replace('_', ' ').replace('-', ' ');
        StringBuilder b = new StringBuilder();
        boolean cap = true;
        for (char c : s.toCharArray()) {
            if (cap && Character.isLetter(c)) { b.append(Character.toUpperCase(c)); cap = false; }
            else b.append(c);
            if (c == ' ') cap = true;
        }
        return b.toString();
    }
}
