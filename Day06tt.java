import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

public class Day06tt {
    static String program = """
      -- assume the operation is always the first column after the blank column
      boundaries is $BINDINGS(operations:) -> [$(.. as i; -> if <~|=' '> -> $i)..., $::end + 2];
      operations is $BINDINGS(operations:; $boundaries(..~));
      columns is $boundaries(..~ as i; -> [$, $boundaries($i + 1) - 2]);

      problems is [$columns... -> $BINDINGS(grid:; ..; $(1\\)..$(\\1))];

      compose-number templates
        @ set 0;
        $... -> ! # 
        $@ !
        when <|..> do @ set $@ * 10 + $;
      end compose-number

      multiply templates
        @ set 1;
        $... -> @ set $@ * $;
        $@ !
      end multiply

      add templates
        @ set 0;
        $... -> @ set $@ + $;
        $@ !
      end add

      -- part 1
      $problems(.. as i; -> $(..; -> compose-number) -> templates
        when <|?($operations($i) matches <|='*'>)> do $ -> multiply !
        otherwise $ -> add !
      end) -> add !

      -- part 2
      $problems(.. as i; -> templates
          problem is $;
          $(1\\; .. as j; -> $problem(..; $j) -> compose-number) !
        end
        -> templates
          when <|?($operations($i) matches <|='*'>)> do $ -> multiply !
          otherwise $ -> add !
        end) -> add !
      """;
    
  public static void main(String[] args) throws IOException {
    List<String> lines = Files.lines(Path.of("input.txt")).toList();
    String[] operations = lines.getLast().split("");
    List<Object[]> grid = lines.subList(0, lines.size() - 1).stream()
      .map(l -> Arrays.stream(l.split("")).map(c -> c.equals(" ") ? c : Long.valueOf(c)).toArray()).toList();
    try (Context truffleContext = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build()) {
      truffleContext.getPolyglotBindings().putMember("grid", grid);
      truffleContext.getPolyglotBindings().putMember("operations", operations);
      System.out.println(truffleContext.eval("tt", program));
    }
  }
}
