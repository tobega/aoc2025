import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

public class Day03tt {
    static String program = """
      digits-to-number templates
        @ set 0;
        $... -> @ set $@ * 10 + $;
        $@!
      end digits-to-number

      total-joltage templates
      requires <|=2|=12>

      series is $;

      joltage templates
        bank is $;
        @ set [$bank::start];
        $@::length -> !#
        $bank($@) -> digits-to-number !

        when <|..$series> do
          last is $series - $@::length + 1;
          $@(\\1)~..$bank(\\$last as i; -> $i)
          -> auxiliary templates
            when <|?($bank($) matches <|$bank($@joltage(\\1))~..>)> do @joltage(\\1) set $;
          end -> !VOID
          $@::length -> auxiliary templates
            when <|..~$series> do
              ..\\@joltage set $@joltage(\\1) + 1;
              $@joltage::length !
          end -> !#
      end joltage

      [$BINDINGS(banks:)... -> joltage] -> templates
        @ set 0;
        $... -> @ set $@ + $;
        $@ !
      end !

      end total-joltage
      2 -> total-joltage !
      12 -> total-joltage !
      """;
    
  public static void main(String[] args) throws IOException {
    List<long[]> banks = Files.lines(Path.of("input.txt"))
        .map(s -> s.split(""))
        .map(s -> Arrays.stream(s).mapToLong(Long::valueOf).toArray())
        .toList();
    try (Context truffleContext = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build()) {
      truffleContext.getPolyglotBindings().putMember("banks", banks);
      System.out.println(truffleContext.eval("tt", program));
    }
  }
}
