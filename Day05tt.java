import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

public class Day05tt {
    static String program = """
      compact-ranges templates
        @ set [];
        $... -> auxiliary templates
          @ set 'pending';
          range is $;
          @compact-ranges set [$@compact-ranges... -> #, $range -> if <|?($@ matches <|='pending'>)>];

          when <|?($@ matches <|='inserted'>)> do $!
          when <|{from: <|$range(to:)~..>}> do
            $range! $! @ set 'inserted';
          when <|{from: <|$range(from:)..>}> do
            { from: $range(from:), to: $(to:) -> templates
              when <|$range(to:)..> do $ !
              otherwise $range(to:) ! 
            end} !
            @ set 'inserted';
          when <|{to: <|$range(from:)..>}> do
            { from: $(from:), to: $(to:) -> templates
              when <|$range(to:)..> do $ !
              otherwise $range(to:) ! 
            end} !
            @ set 'inserted';
          when <|{to: <|..~$range(from:)>}> do $ !
        end -> !VOID
        $@ !
      end compact-ranges

      ranges is [$BINDINGS(ranges:)... -> { from: ingredient´($(from:)), to: ingredient´($(to:))}] -> compact-ranges;

      count-fresh templates
        @ set 0;
        $... -> auxiliary templates
          ingredient is $;
          @ set { start: ($ranges::start)"1", end: ($ranges::end)"1"};
          ($@(start:) + $@(end:)) ~/ 2"1" -> !#

          when <|?($@(start:) matches <|$@(end:)~..>)> do VOID
          when <|?($ingredient matches <|$ranges($::raw; from:)..$ranges($::raw; to:)>)> do
            @count-fresh set $@count-fresh + 1;
          when <|?($ingredient matches <|..~$ranges($::raw; from:)>)> do
            @(end:) set $ - 1"1";
            ($@(start:) + $@(end:)) ~/ 2"1" -> !#
          when <|?($ingredient matches <|$ranges($::raw; to:)~..>)> do
            @(start:) set $ + 1"1";
            ($@(start:) + $@(end:)) ~/ 2"1" -> !#
        end -> !VOID
        $@ !         
      end count-fresh

      -- part 1
      [$BINDINGS(ingredients:)... -> ingredient´($)] -> count-fresh !
      """;
    
  public static void main(String[] args) throws IOException {
    String[] inputParts = Files.readString(Path.of("input.txt")).split("\n\n");
    List<Map<String, Long>> ranges = Arrays.stream(inputParts[0].split("\n")).map(s -> {
      String[] parts = s.split("-");
      return Map.of("from", Long.valueOf(parts[0]), "to", Long.valueOf(parts[1]));
    }).toList();
    long[] ingredients = Arrays.stream(inputParts[1].split("\n")).mapToLong(Long::valueOf).toArray();
    try (Context truffleContext = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build()) {
      truffleContext.getPolyglotBindings().putMember("ranges", ranges);
      truffleContext.getPolyglotBindings().putMember("ingredients", ingredients);
      System.out.println(truffleContext.eval("tt", program));
    }
  }
}
