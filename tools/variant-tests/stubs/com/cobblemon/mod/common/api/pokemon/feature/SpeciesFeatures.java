package com.cobblemon.mod.common.api.pokemon.feature;
import java.util.*;
import com.cobblemon.mod.common.pokemon.Species;
public final class SpeciesFeatures {
 public static List<SpeciesFeatureProvider<?>> getFeaturesFor(Species species){ return species.getFeatureProviders(); }
}
