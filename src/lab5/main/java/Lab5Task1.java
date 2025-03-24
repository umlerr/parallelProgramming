import java.util.*;

public class Lab5Task1 {

    static class Operation {
        int id;
        int duration;
        List<Integer> dependencies;

        Operation(int id, int duration) {
            this.id = id;
            this.duration = duration;
            this.dependencies = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        List<Operation> operations = initializeOperations();
        int[] earliestStartTimes = new int[operations.size()];

        long totalDuration = 0;
        int numRuns = 10;

        for (int run = 0; run < numRuns; run++) {
            long startTime = System.nanoTime();
            Arrays.fill(earliestStartTimes, 0);

            for (int i = 0; i < operations.size(); i++) {
                Operation currentOp = operations.get(i);
                int maxDependencyFinishTime = 0;

                for (int dependency : currentOp.dependencies) {
                    maxDependencyFinishTime = Math.max(maxDependencyFinishTime,
                            earliestStartTimes[dependency] + operations.get(dependency).duration);
                }
                earliestStartTimes[i] = maxDependencyFinishTime;
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalDuration += duration;
        }

        double averageDuration = totalDuration / (double) numRuns;
        printResults(operations, earliestStartTimes, averageDuration);
    }

    private static List<Operation> initializeOperations() {
        List<Operation> ops = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int duration = (i % 5) + 1;
            ops.add(new Operation(i, duration));
        }

        for (int i = 1; i < 10; i++) {
            ops.get(i).dependencies.add(i - 1);
        }
        return ops;
    }

    private static void printResults(List<Operation> ops, int[] earliestStartTimes, double averageTime) {
        System.out.println("Earliest start times:");
        for (int i = 0; i < ops.size(); i++) {
            System.out.printf("Operation %d: %d\n", i, earliestStartTimes[i]);
        }

        System.out.printf("\nAverage execution time over 10 runs: %.6f seconds\n", averageTime / 1_000_000_000.0);
    }
}
