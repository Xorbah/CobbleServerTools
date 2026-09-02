package net.cobbleservertools.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.level.ServerPlayer;

public final class NpcDialogText {
   private static final Pattern PLAYER = Pattern.compile("\\[player]", 2);

   private NpcDialogText() {
   }

   public static List<String> apply(List<String> var0, ServerPlayer var1) {
      ArrayList var2 = new ArrayList(var0.size());
      String var3 = var1.getGameProfile().getName();

      for (String var5 : var0) {
         var2.add(PLAYER.matcher(var5).replaceAll(Matcher.quoteReplacement(var3)));
      }

      return List.copyOf(var2);
   }
}
