import mpi.MPI;
import java.util.Random;

public class Lab2Task2 {

    public static void main(String[] args) {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        int numElements = 10;
        int elementsPerProcess = numElements / size;
        int[] numbers = new int[numElements];
        int[] localNumbers = new int[elementsPerProcess];

        if (rank == 0) {
            Random random = new Random();
            for (int i = 0; i < numElements; i++) {
                numbers[i] = random.nextInt(201) - 100;
            }

            System.out.printf("Process %d (Thread ID: %d) full numbers: %s\n", rank,
                    Thread.currentThread().getId(), arrayToString(numbers));

            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Send(numbers, i * elementsPerProcess, elementsPerProcess, MPI.INT, i, 99);
            }

            System.arraycopy(numbers, 0, localNumbers, 0, elementsPerProcess);
        } else {
            MPI.COMM_WORLD.Recv(localNumbers, 0, elementsPerProcess, MPI.INT, 0, 99);
        }

        System.out.printf("Process %d (Thread ID: %d) local numbers: %s\n", rank,
                Thread.currentThread().getId(), arrayToString(localNumbers));

        int localEvenCount = countPositive(localNumbers);
        System.out.printf("Process %d found %d even positive numbers.\n", rank, localEvenCount);

        if (rank == 0) {
            int totalEvenCount = localEvenCount;
            int[] receivedCount = new int[1];

            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(receivedCount, 0, 1, MPI.INT, i, 100);
                totalEvenCount += receivedCount[0];
            }

            System.out.printf("Total even positive numbers: %d\n", totalEvenCount);
        } else {
            MPI.COMM_WORLD.Send(new int[]{localEvenCount}, 0, 1, MPI.INT, 0, 100);
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
