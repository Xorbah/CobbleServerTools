package com.cobblemon.mod.common.api.pokemon.feature;
import java.util.*;
public class ChoiceSpeciesFeatureProvider implements SpeciesFeatureProvider<Object> {
 private List<String> keys; private String def; private List<String> choices; private boolean aspect; private String format;
 public ChoiceSpeciesFeatureProvider(List<String> keys,String def,List<String> choices,boolean aspect,String format){this.keys=keys;this.def=def;this.choices=choices;this.aspect=aspect;this.format=format;}
 public List<String> getKeys(){return keys;} public String getDefault(){return def;} public List<String> getChoices(){return choices;} public boolean isAspect(){return aspect;} public String getAspectFormat(){return format;}
 public List<String> getAllAspects(){List<String> out=new ArrayList<>();for(String c:choices)out.add(format.replace("{{choice}}",c));return out;}
}
