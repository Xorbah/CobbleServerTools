package com.cobblemon.mod.common.pokemon;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class FormData {
 private String name;
 private List<String> aspects;
 public FormData(){ this("Normal", java.util.List.of()); }
 public FormData(String name,List<String> aspects){ this.name=name; this.aspects=new ArrayList<>(aspects); }
 public String getName(){ return name; }
 public List<String> getAspects(){ return aspects; }
 public String formOnlyShowdownId(){ return name == null ? "" : name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""); }
}
