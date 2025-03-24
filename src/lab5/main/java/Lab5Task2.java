import mpi.MPI;
import mpi.MPIException;
import java.util.ArrayList;
import java.util.List;

public class Lab5Task2 {
    static class Operation implements java.io.Serializable {
        int id, duration;
        List<Integer> dependencies;

        Operation(int id, int duration) {
            this.id = id;
            this.duration = duration;
            this.dependencies = new ArrayList<>();
        }
    }

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        List<Operation> operations = null;
        int[] earliestStart = null;
        int operationCount = 0;

        int numRuns = 10; // Количество запусков
        long totalDuration = 0;

        for (int run = 0; run < numRuns; run++) {
            if (rank == 0) {
                operations = initializeOperations();
                operationCount = operations.size();
                earliestStart = new int[operationCount];
            }

            int[] operationCountArray = new int[]{operationCount};
            MPI.COMM_WORLD.Bcast(operationCountArray, 0, 1, MPI.INT, 0);
            operationCount = operationCountArray[0];

            if (rank != 0) {
                earliestStart = new int[operationCount];
            }

            Operation[] operationsArray = new Operation[operationCount];
            if (rank == 0) {
                operationsArray = operations.toArray(new Operation[0]);
            }
            MPI.COMM_WORLD.Bcast(operationsArray, 0, operationCount, MPI.OBJECT, 0);

            MPI.COMM_WORLD.Bcast(earliestStart, 0, operationCount, MPI.INT, 0);

            long startTime = System.nanoTime();

            int chunkSize = operationCount / size;
            int startIndex = rank * chunkSize;
            int endIndex = (rank == size - 1) ? operationCount : startIndex + chunkSize;

            int[] localEarliest = new int[operationCount];

            for (int i = startIndex; i < endIndex; i++) {
                Operation op = operationsArray[i];
                int maxDepTime = 0;
                for (int dep : op.dependencies) {
                    maxDepTime = Math.max(maxDepTime, earliestStart[dep] + operationsArray[dep].duration);
                }
                localEarliest[i] = maxDepTime;
            }

            MPI.COMM_WORLD.Reduce(localEarliest, 0, earliestStart, 0, operationCount, MPI.INT, MPI.MAX, 0);

            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        double averageDuration = totalDuration / (double) numRuns;

        if (rank == 0) {
            printResults(operations.toArray(new Operation[0]), earliestStart, averageDuration);
        }

        MPI.Finalize();
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

    private static void printResults(Operation[] ops, int[] earliestStart, double averageTime) {
        System.out.println("Earliest start times:");
        for (int i = 0; i < ops.length; i++) {
            System.out.printf("Operation %d: %d\n", i, earliestStart[i]);
        }

        System.out.printf("\nAverage execution time over 10 runs: %.6f seconds\n", averageTime / 1_000_000_000.0);
    }
}
