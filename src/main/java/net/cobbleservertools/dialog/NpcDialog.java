package net.cobbleservertools.dialog;

import java.util.List;

public record NpcDialog(List<String> main, List<String> yes, List<String> no, List<String> cancel) {
   public static final NpcDialog EMPTY = new NpcDialog(List.of(), List.of(), List.of(), List.of());

   public NpcDialog(List<String> main, List<String> yes, List<String> no, List<String> cancel) {
      main = List.copyOf(main == null ? List.of() : main);
      yes = List.copyOf(yes == null ? List.of() : yes);
      no = List.copyOf(no == null ? List.of() : no);
      cancel = List.copyOf(cancel == null ? List.of() : cancel);
      this.main = main;
      this.yes = yes;
      this.no = no;
      this.cancel = cancel;
   }

   public boolean hasChoices() {
      return !this.yes.isEmpty() || !this.no.isEmpty();
   }

   public boolean isEmpty() {
      return this.main.isEmpty() && this.yes.isEmpty() && this.no.isEmpty() && this.cancel.isEmpty();
   }
}
