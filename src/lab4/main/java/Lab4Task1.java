import mpi.MPI;
import java.util.Random;

public class Lab4Task1 {

    public static void main(String[] args) {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        int numElements = 40;
        int elementsPerProc = numElements / size;

        int[] numbers = new int[numElements];
        int[] localNumbers = new int[elementsPerProc];

        if (rank == 0) {
            Random random = new Random();
            for (int i = 0; i < numElements; i++) {
                numbers[i] = random.nextInt(201) - 100;
            }
        }

        MPI.COMM_WORLD.Scatter(numbers, 0, elementsPerProc, MPI.INT,
                localNumbers, 0, elementsPerProc, MPI.INT, 0);

        int localEvenCount = countPositive(localNumbers);

        System.out.printf("Process %d (Thread ID: %d) numbers: %s\n", rank,
                Thread.currentThread().getId(), arrayToString(localNumbers));
        System.out.printf("Process %d found %d even positive numbers.\n", rank,
                localEvenCount);

        int[] totalEvenCount = new int[1];

        MPI.COMM_WORLD.Reduce(new int[]{localEvenCount}, 0, totalEvenCount, 0, 1, MPI.INT, MPI.SUM, 0);

        if (rank == 0) {
            System.out.printf("Total even positive numbers: %d\n", totalEvenCount[0]);
        }

        MPI.Finalize();
    }

    private static int countPositive(int[] numbers) {
        int count = 0;
        for (int num : numbers) {
            if (num > 0 && num % 2 == 0) {
                count++;
            }
        }
        return count;
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
