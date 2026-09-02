package net.cobbleservertools.client.state;

public final class ClientMoneyState {
   private static int money;

   private ClientMoneyState() {
   }

   public static int get() {
      return money;
   }

   public static void set(int var0) {
      money = Math.max(0, var0);
   }

   public static void clear() {
      money = 0;
   }
}
