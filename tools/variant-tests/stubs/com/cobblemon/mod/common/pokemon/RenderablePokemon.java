package com.cobblemon.mod.common.pokemon;
import java.util.LinkedHashSet;
import java.util.Set;
public class RenderablePokemon {
 private Species species;
 private Set<String> aspects = new LinkedHashSet<>();
 public RenderablePokemon(){ this(new Species(), java.util.Set.of()); }
 public RenderablePokemon(Species species, Set<String> aspects){ this.species=species; this.aspects=new LinkedHashSet<>(aspects); }
 public Species getSpecies(){ return species; }
 public Set<String> getAspects(){ return aspects; }
 public void setAspects(Set<String> aspects){ this.aspects = new LinkedHashSet<>(aspects); }
}
