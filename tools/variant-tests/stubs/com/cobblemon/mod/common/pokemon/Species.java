package com.cobblemon.mod.common.pokemon;
import java.util.*;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;
public class Species {
 private final List<FormData> forms = new ArrayList<>();
 private final List<SpeciesFeatureProvider<?>> featureProviders = new ArrayList<>();
 private FormData standardForm = new FormData("Normal", java.util.List.of());
 public List<FormData> getForms(){ return forms; }
 public FormData getStandardForm(){ return standardForm; }
 public void setStandardForm(FormData f){ standardForm=f; }
 public List<SpeciesFeatureProvider<?>> getFeatureProviders(){return featureProviders;}
}
