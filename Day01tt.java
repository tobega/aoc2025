import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

public class Day01tt {

  static String program = """
      part1 templates
        update-zeroes auxiliary sink
          when <|=0"1"> do
            @part1(zeroes:) set $@part1(zeroes:) + 1"1";
        end update-zeroes
        @ set { zeroes: 0"1", position: 50"1"};
        $... -> !#
        $@(zeroes:) !
      
        when <|..~0> do
          @(position:) set ($@(position:) + $)"1" mod 100"1";
          $@(position:) -> !update-zeroes
        when <|0~..> do
          @(position:) set ($@(position:) + $)"1" mod 100"1";
          $@(position:) -> !update-zeroes
        otherwise
          $ -> !REJECT
      end part1
      
      part2 templates
        update-zeroes auxiliary sink
          when <|..0"1"> do
            @part2(zeroes:) set $@part2(zeroes:) - $ ~/ 100"1" + 1"1";
          when <|100"1"..> do
            @part2(zeroes:) set $@part2(zeroes:) + $ ~/ 100"1";
        end update-zeroes
        @ set { zeroes: 0"1", position: 50"1"};
        $... -> !#
        $@(zeroes:) !
      
        when <|..~0?($@(position:) matches <|=0"1">)> do
          new is ($@(position:) + $)"1";
          -- Don't double-count the zero
          @ set  {position: $new mod 100"1", zeroes: $@(zeroes:) - 1"1"};
          $new -> !update-zeroes
        when <|..~0> do
          new is ($@(position:) + $)"1";
          @(position:) set  $new mod 100"1";
          $new -> !update-zeroes
        when <|0~..> do
          new is ($@(position:) + $)"1";
          @(position:) set $new mod 100"1";
          $new -> !update-zeroes
        otherwise
          $ -> !REJECT
      end part2
      
      $BINDINGS(instructions:) -> part1 !
      
      $BINDINGS(instructions:) -> part2 !
      """;
    
  public static void main(String[] args) throws IOException {
    List<Long> instructions = Files.readAllLines(Path.of("input.txt"))
        .stream()
        .map(s -> s.replace("L", "-"))
        .map(s -> s.replace("R", ""))
        .map(Long::valueOf)
        .toList();
    try (Context truffleContext = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build()) {
      truffleContext.getPolyglotBindings().putMember("instructions", instructions);
      System.out.println(truffleContext.eval("tt", program));
    }
  }
}