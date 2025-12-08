import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Day08 {
    static class Heap<T> {
        final List<T> values;
        final int k;
        final Comparator<T> comparator;

        Heap(int k, Comparator<T> comparator) {
            this.k = k;
            this.comparator = comparator;
            values = new ArrayList<>();
            values.add(null); // Don't need zero:th element
        }

        List<T> getUnsorted() {
            return Collections.unmodifiableList(values.subList(1, values.size()));
        }

        private void bubbleUp(int child) {
            int parent = child / 2;

            if (child > 1 && comparator.compare(values.get(child), values.get(parent)) == 1) {
                Collections.swap(values, child, parent);
                bubbleUp(parent);
            }
        }

        private void trickleDown(int parent) {
            int left = parent * 2;
            int right = left + 1;

            if (right < values.size()
                    && comparator.compare(values.get(right), values.get(parent)) == 1
                    && comparator.compare(values.get(right), values.get(left)) == 1) {
                Collections.swap(values, right, parent);
                trickleDown(right);
            } else if (left < values.size()
                    && comparator.compare(values.get(left), values.get(parent)) == 1) {
                Collections.swap(values, left, parent);
                trickleDown(left);
            }
        }

        Optional<T> insert(T value) {
            if (k == 0 || values.size() <= k) {
                values.add(value);
                bubbleUp(values.size() - 1);
                return Optional.empty();
            } else if (comparator.compare(value, values.get(1)) == -1) {
                T discarded = values.get(1);
                values.set(1, value);
                trickleDown(1);
                return Optional.of(discarded);
            } else {
                return Optional.of(value);
            }
        }

        T pop() {
            T result = values.get(1);
            values.set(1, values.removeLast());
            trickleDown(1);
            return result;
        }
    }

    static record DisjointSet(int set, List<Integer> elements) {
    }

    static class DisjointSets {
        final int[] pointers;

        DisjointSets(int nElements) {
            pointers = new int[nElements];
            for (int i = 0; i < pointers.length; i++)
                pointers[i] = i;
        }

        int find(int element) {
            if (pointers[element] == element) {
                return element;
            } else {
                // store it back for path-contraction
                int set = find(pointers[element]);
                pointers[element] = set;
                return set;
            }
        }

        void union(int a, int b) {
            int aSet = find(a);
            int bSet = find(b);
            pointers[aSet] = bSet;
        }

        List<DisjointSet> getSets() {
            record Element(int set, int element) {
            }
            return IntStream.range(0, pointers.length).mapToObj(i -> new Element(find(i), i))
                    .collect(Collectors.groupingBy(e -> e.set))
                    .entrySet().stream()
                    .map((e) -> new DisjointSet(e.getKey(), e.getValue().stream().map(Element::element).toList()))
                    .toList();
        }
    }

    static record Box(long x, long y, long z) {
        static Box of(String line) {
            String[] parts = line.split(",");
            return new Box(Long.valueOf(parts[0]), Long.valueOf(parts[1]), Long.valueOf(parts[2]));
        }

        long distanceFrom(Box other) {
            long x = this.x - other.x;
            long y = this.y - other.y;
            long z = this.z - other.z;

            return x * x + y * y + z * z; // don't need the correct value, just comparative
        }
    }

    static record Lights(int from, int to, long distance) {
    }

    final List<Box> boxes;
    final DisjointSets sets;
    final Heap<Lights> remainder;

    Day08(List<Box> boxes) {
        this.boxes = boxes;
        this.sets = new DisjointSets(boxes.size());
        this.remainder = new Heap<>(0, Comparator.comparing((Lights d) -> d.distance).reversed());
    }

    long part1() {
        Heap<Lights> min1000 = new Heap<>(1000, Comparator.comparing((d) -> d.distance));

        for (int i = 0; i < boxes.size(); i++) {
            for (int j = i + 1; j < boxes.size(); j++) {
                Lights lights = new Lights(i, j, boxes.get(i).distanceFrom(boxes.get(j)));
                min1000.insert(lights).ifPresent(remainder::insert);
            }
        }

        min1000.getUnsorted().forEach(d -> sets.union(d.from, d.to));

        record SizedSet(int set, int size) {
        }

        Heap<SizedSet> maxheap = new Heap<>(0, Comparator.comparing(s -> s.size));
        sets.getSets().stream().map(d -> new SizedSet(d.set, d.elements.size())).forEach(maxheap::insert);
        return maxheap.pop().size * maxheap.pop().size * maxheap.pop().size;
    }

    long part2() {
        Lights next = null;
        while (sets.getSets().size() > 1) {
            next = remainder.pop();
            sets.union(next.from, next.to);
        }
        return next == null ? 0 : boxes.get(next.from).x * boxes.get(next.to).x;
    }

    public static void main(String[] args) {
        List<Box> boxes = new BufferedReader(new InputStreamReader(System.in)).lines().map(Box::of).toList();
        Day08 puzzle = new Day08(boxes);
        System.out.println(puzzle.part1());
        System.out.println(puzzle.part2());
    }
}