import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;

public class MotifFinder {

    private static List<String> readSequencesFromFile(String fileName) throws FileNotFoundException {
        List<String> sequences = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                sequences.add(scanner.nextLine());
            }
        }
        return sequences;
    }

    private static String findMotif(List<String> sequences, int motifSize) {
        Set<String> allMotifs = generateAllMotifs(motifSize);

        Map<String, Integer> motifCounts = countOccurrencesParallel(sequences, allMotifs);

        String mostFrequentMotif = "";
        int maxOccurrences = 0;
        int maxConsecutiveRepeats = 0;
        for (Map.Entry<String, Integer> entry : motifCounts.entrySet()) {
            String motif = entry.getKey();
            int count = entry.getValue();
            int consecutiveRepeats = findLongestConsecutiveRepeats(motif, sequences);

            if (count > maxOccurrences || (count == maxOccurrences && consecutiveRepeats > maxConsecutiveRepeats)) {
                mostFrequentMotif = motif;
                maxOccurrences = count;
                maxConsecutiveRepeats = consecutiveRepeats;
            }
        }

        return mostFrequentMotif;
    }

    private static Set<String> generateAllMotifs(int motifSize) {
        Set<String> motifs = new HashSet<>();
        char[] bases = {'A', 'C', 'G', 'T'};
        generateMotifsRecursively(motifs, "", bases, motifSize);
        return motifs;
    }

    private static void generateMotifsRecursively(Set<String> motifs, String prefix, char[] bases, int remaining) {
        if (remaining == 0) {
            motifs.add(prefix);
            return;
        }
        for (char base : bases) {
            generateMotifsRecursively(motifs, prefix + base, bases, remaining - 1);
        }
    }

    private static Map<String, Integer> countOccurrencesParallel(List<String> sequences, Set<String> motifs) {
        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numCores);
        List<Callable<Map<String, Integer>>> tasks = new ArrayList<>();

        int chunkSize = sequences.size() / numCores;
        for (int i = 0; i < numCores; i++) {
            int start = i * chunkSize;
            int end = (i == numCores - 1) ? sequences.size() : (i + 1) * chunkSize;
            List<String> subSequences = sequences.subList(start, end);
            tasks.add(() -> countOccurrencesForChunk(subSequences, motifs));
        }

        try {
            List<Future<Map<String, Integer>>> results = executor.invokeAll(tasks);
            Map<String, Integer> motifCounts = new HashMap<>();
            for (Future<Map<String, Integer>> result : results) {
                motifCounts.putAll(result.get());
            }
            executor.shutdown();
            return motifCounts;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return null;
    }

    private static Map<String, Integer> countOccurrencesForChunk(List<String> sequences, Set<String> motifs) {
        Map<String, Integer> counts = new HashMap<>();
        for (String sequence : sequences) {
            for (String motif : motifs) {
                int count = countOccurrences(sequence, motif);
                counts.put(motif, counts.getOrDefault(motif, 0) + count);
            }
        }
        return counts;
    }

    private static int countOccurrences(String sequence, String motif) {
        int count = 0;
        for (int i = 0; i <= sequence.length() - motif.length(); i++) {
            if (sequence.substring(i, i + motif.length()).equals(motif)) {
                count++;
            }
        }
        return count;
    }

    private static int findLongestConsecutiveRepeats(String motif, List<String> sequences) {
    int maxConsecutiveRepeats = 0;

    for (String sequence : sequences) {
        int currentConsecutiveRepeats = 0;
        int prevStart = -1; // Keeps track of the starting index of the previous occurrence

        for (int i = 0; i < sequence.length(); i++) {
            // Check if current character matches the motif
            if (sequence.charAt(i) == motif.charAt(0)) {
                // Check if the entire motif matches starting from the current index
                boolean isMotifMatch = true;
                for (int j = 0; j < motif.length() && i + j < sequence.length(); j++) {
                    if (sequence.charAt(i + j) != motif.charAt(j)) {
                        isMotifMatch = false;
                        break;
                    }
                }
            if (isMotifMatch) {
                // Update consecutive repeats if it's a new occurrence or consecutive
                if (prevStart == i - 1) {
                    currentConsecutiveRepeats++;
                    } else {
                        currentConsecutiveRepeats = 1;
                    }
                    prevStart = i;
                    maxConsecutiveRepeats = Math.max(maxConsecutiveRepeats, currentConsecutiveRepeats);
                } else {
                    // Reset consecutive repeats if motif doesn't match
                    currentConsecutiveRepeats = 0;
                    prevStart = -1;
                }
            }
        }
    }

    return maxConsecutiveRepeats;
}
    public static void main(String[] args) throws FileNotFoundException {
        String fileName = "Nucleotidos.txt"; // Ajustar el nombre del archivo
        List<String> sequences = readSequencesFromFile(fileName);

        int motifSize = 4; // Tamaño del motif (ajustable)

        String mostFrequentMotif = findMotif(sequences, motifSize);

        System.out.println("El motif mas frecuente es: " + mostFrequentMotif);
    }
}