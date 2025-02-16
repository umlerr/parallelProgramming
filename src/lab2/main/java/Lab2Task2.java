import mpi.MPI;

import java.util.Random;

public class Lab2Task2 {

    public static void main(String[] args) {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        Random random = new Random();
        int numElements = 10;
        int[] numbers = new int[numElements];
        for (int i = 0; i < numElements; i++) {
            numbers[i] = random.nextInt(201) - 100;
        }

        int localEvenCount = countPositive(numbers);

        System.out.printf("Process %d (Thread ID: %d) numbers: %s\n", rank,
                Thread.currentThread().getId(), arrayToString(numbers));
        System.out.printf("Process %d found %d even positive numbers.\n", rank,
                localEvenCount);

        int totalEvenCount;

        if (rank == 0) {
            totalEvenCount = localEvenCount;
            for (int i = 1; i < size; i++) {
                int[] received = new int[1];
                MPI.COMM_WORLD.Recv(received, 0, 1, MPI.INT, i, 99);
                totalEvenCount += received[0];
            }
            System.out.printf("Total even positive numbers: %d\n", totalEvenCount);
        } else {
            MPI.COMM_WORLD.Send(new int[]{localEvenCount}, 0, 1, MPI.INT, 0, 99);
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
