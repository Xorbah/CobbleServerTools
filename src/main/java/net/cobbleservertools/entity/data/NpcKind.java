package net.cobbleservertools.entity.data;

import java.util.Locale;

public enum NpcKind {
   BATTLE,
   RIVAL,
   DIALOG,
   TRADER,
   MART,
   MOVE_TUTOR;

   public String serializedName() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
