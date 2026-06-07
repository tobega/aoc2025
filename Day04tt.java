import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

public class Day04tt {
    static String program = """
      grid is [$BINDINGS::rolls ... -> [$ ... -> '$;']];

      count-adjacent templates
        when <|?($grid($(1\\); $(\\1)) matches <|='@'>)> do
          fromi is $(1\\)-1;
          toi is $(1\\)+1;
          fromj is $(\\1)-1;
          toj is $(\\1)+1;
          [$grid($fromi..$toi; $fromj..$toj; -> if <|='@'>) ... ...] -> $::length - 1 !
        otherwise '.' !
      end count-adjacent

      adjacency-grid is $grid(.. as i; .. as j; -> [$i, $j] -> count-adjacent);

      movable-rolls is [$adjacency-grid(.. as i; .. as j; -> if ´|..|='.'´<|..~4> -> [$i, $j]) ... ...];
      $movable-rolls::length !

      $movable-rolls -> templates
        @ set { plan: $adjacency-grid, removed: 0"1" };
        $... -> !#
        $@(removed:) !

        when <|?($@(plan:; $(1\\); $(2\\)) matches ´|..|='.'´<|..~4>)> do
          @(removed:) set $@(removed:) + 1"1";
          @(plan:; $(1\\); $(2\\)) set '.';
          fromi is $(1\\)-1;
          toi is $(1\\)+1;
          fromj is $(2\\)-1;
          toj is $(2\\)+1;
          neighbours is [$@(plan:; $fromi..$toi as i; $fromj..$toj as j; -> if <|..> -> [$i, $j]) ... ...];
          $neighbours... -> @(plan:; $(1\\); $(2\\)) set $@(plan:; $(1\\); $(2\\)) - 1;
          $neighbours... -> !#
      end !
      """;
    
  public static void main(String[] args) throws IOException {
    List<List<String>> rolls = Files.lines(Path.of("input.txt"))
        .map(s -> Arrays.asList(s.split("")))
        .toList();
    try (Context truffleContext = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build()) {
      truffleContext.getPolyglotBindings().putMember("rolls", rolls);
      System.out.println(truffleContext.eval("tt", program));
    }
  }
}
