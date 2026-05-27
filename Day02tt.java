import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

public class Day02tt {
    static String program = """
            start requires <|"1">
            end requires <|"1">
            intervals is [$BINDINGS::ranges... -> {start: ($(0))"1", end: ($(1))"1"}];
            -- still haven't implemented tests
            --intervals is [[
            --  [11,22],
            --  [95,115],
            --  [998,1012],
            --  [1188511880,1188511890],
            --  [222220,222224],
            --  [1698522,1698528],
            --  [446443,446449],
            --  [38593856,38593862],
            --  [565653,565659],
            --  [824824821,824824827],
            --  [2121212118,2121212124]
            --]... -> {start: ($(1))"1", end: ($(2))"1"}];

            -- currently missing processor support for day02.tt
            -- and missing composer support for day02alt.tt
            -- so we have to do a little more work

            check-twice templates
              value is $;
              10"1" -> # !

              when <|$value ~/ $ ~.. > do
                $ -> if <|?($value ~/ $ * 10"1" matches <|$..>)> -> if <|?($value ~/ $ matches <|=$value mod $>)> -> ($value)"twice" !
              otherwise
                $ * 10"1" -> # !
            end check-twice

            silly-value templates
              @ set 0"1";
              value is $;
              10"1" -> # !

              when <|?($value mod $ matches <|$value ~/ $ ~..>)> do VOID
              when <|?($value mod $ matches <|$@~..>)> do
                @ set $value mod $;
                multiplier is $;
                $@ -> auxiliary templates
                  when <|..~$value> do $ * $multiplier + $@silly-value -> #!
                  when <|=$value> do ($value)"silly" !
                    @silly-value set $value; --avoid finding another repeat pattern
                end !
                $multiplier * 10"1" -> #!
              otherwise $ * 10"1" -> #!
            end silly-value

            count-silly templates
              @ set { twice: 0"twice", silly: 0"silly"};
              $... -> templates
                interval is $;
                $interval(start:) -> !#

                when <|..$interval(end:)> do
                    $ -> check-twice -> @count-silly(twice:) set $@count-silly(twice:) + $;
                    $ -> silly-value -> @count-silly(silly:) set $@count-silly(silly:) + $;
                    $ + 1"1" -> !#
              end -> !VOID
              $@!
            end count-silly

            $intervals -> count-silly!
            """;
    
  public static void main(String[] args) throws IOException {
    List<long[]> ranges = Files.lines(Path.of("input.txt"))
        .flatMap(s -> Arrays.stream(s.split(",")))
        .map(s -> Arrays.stream(s.split("-")).mapToLong(Long::valueOf).toArray())
        .toList();
    try (Context truffleContext = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build()) {
      truffleContext.getPolyglotBindings().putMember("ranges", ranges);
      System.out.println(truffleContext.eval("tt", program));
    }
  }
}
