package com.cobblemon.mod.common.api.pokemon;
import java.util.*;
import com.cobblemon.mod.common.api.pokemon.feature.ChoiceSpeciesFeatureProvider;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.cobblemon.mod.common.pokemon.Species;
public class PokemonProperties {
 private String speciesName="suicune"; private Boolean shiny=false; private java.util.Set<String> aspects = new LinkedHashSet<>();
 public PokemonProperties(){}
 public static PokemonProperties parse(String s){ PokemonProperties p=new PokemonProperties(); p.setSpecies(s); p.updateAspects(); return p; }
 public void setSpecies(String s){ speciesName=s; }
 public void setShiny(Boolean b){ shiny=b; }
 public void updateAspects(){ aspects.clear(); if(Boolean.TRUE.equals(shiny)) aspects.add("shiny"); }
 public RenderablePokemon asRenderablePokemon(){
   Species sp=new Species();
   String n=speciesName==null?"":speciesName.toLowerCase(Locale.ROOT);
   if(n.equals("vulpix") || n.endsWith(":vulpix")) {
     sp.getForms().add(sp.getStandardForm()); sp.getForms().add(new FormData("Alola", List.of("alolan")));
   }
   if(n.equals("pikachu") || n.endsWith(":pikachu")) {
     sp.getForms().add(sp.getStandardForm());
     sp.getForms().add(new FormData("Fat", List.of("fat")));
     sp.getForms().add(new FormData("Balloon", List.of("balloon")));
     sp.getFeatureProviders().add(new ChoiceSpeciesFeatureProvider(
       List.of("fat"), "no", List.of("no","fat","balloon","bug","dragonite","surfing","gojo"), true, "{{choice}}"));
   }
   if(n.equals("resolvermon") || n.endsWith(":resolvermon")) {
     sp.getFeatureProviders().add(new ChoiceSpeciesFeatureProvider(
       List.of("coat"), "normal", List.of("normal","chunky","striped"), true, "{{choice}}-coat"));
   }
   return new RenderablePokemon(sp,aspects);
 }
}
