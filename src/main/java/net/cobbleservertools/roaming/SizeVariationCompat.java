package net.cobbleservertools.roaming;
public final class SizeVariationCompat { private SizeVariationCompat(){} public static boolean isLoaded(){try{Class<?> m=Class.forName("net.neoforged.fml.ModList");Object inst=m.getMethod("get").invoke(null);return (Boolean)m.getMethod("isLoaded",String.class).invoke(inst,"cobblemonsizevariation");}catch(Throwable t){return false;}} }
