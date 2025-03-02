import mpi.MPI;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Lab3Task2 {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int rows = 4, cols = 5;
        double[][] matrix = null;
        double[] positiveSumUpperHalf = new double[1];
        double[] negativeSumLowerHalf = new double[1];

        if (rank == 0) {
            matrix = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = Math.random() * 200 - 100;
                }
            }
            System.out.println("Matrix:");
            printMatrix(matrix);
        }

        int rowsPerProcess = rows / size;
        int extraRows = rows % size;
        int myRows = rank < extraRows ? rowsPerProcess + 1 : rowsPerProcess;
        int offset = rank * rowsPerProcess + Math.min(rank, extraRows);

        double[][] localMatrix = new double[myRows][cols];

        if (rank == 0) {
            int rowOffset = 0;
            for (int i = 0; i < size; i++) {
                int sendRows = (i < extraRows) ? rowsPerProcess + 1 : rowsPerProcess;
                for (int j = 0; j < sendRows; j++) {
                    if (i == 0) {
                        System.arraycopy(matrix[rowOffset + j], 0, localMatrix[j], 0, cols);
                    } else {
                        MPI.COMM_WORLD.Send(matrix[rowOffset + j], 0, cols, MPI.DOUBLE, i, 0);
                    }
                }
                rowOffset += sendRows;
            }
        } else {
            for (int i = 0; i < myRows; i++) {
                MPI.COMM_WORLD.Recv(localMatrix[i], 0, cols, MPI.DOUBLE, 0, 0);
            }
        }

        double localPositiveSum = 0;
        for (int i = 0; i < myRows; i++) {
            int globalRow = offset + i;
            if (globalRow < rows / 2) {
                for (int j = 0; j < cols; j++) {
                    if (localMatrix[i][j] > 0) {
                        localPositiveSum += localMatrix[i][j];
                    }
                }
            }
        }

        double localNegativeSum = 0;
        for (int i = 0; i < myRows; i++) {
            int globalRow = offset + i;
            if (globalRow >= rows / 2) {
                for (int j = 0; j < cols; j++) {
                    if (localMatrix[i][j] < 0) {
                        localNegativeSum += localMatrix[i][j];
                    }
                }
            }
        }

        System.out.println("Rank " + rank + " positive sum: " + localPositiveSum);
        System.out.println("Rank " + rank + " negative sum: " + localNegativeSum);

        MPI.COMM_WORLD.Reduce(
                new double[]{localPositiveSum}, 0,
                positiveSumUpperHalf, 0,
                1,
                MPI.DOUBLE,
                MPI.SUM,
                0
        );

        MPI.COMM_WORLD.Reduce(
                new double[]{localNegativeSum}, 0,
                negativeSumLowerHalf, 0,
                1,
                MPI.DOUBLE,
                MPI.SUM,
                0
        );

        if (rank == 0) {
            double result = positiveSumUpperHalf[0] * negativeSumLowerHalf[0];
            System.out.println("Result (positive sum upper half * negative sum lower half): " + result);
        }

        MPI.Finalize();
    }

    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println(Arrays.stream(row)
                    .mapToObj(v -> String.format("%.2f", v))
                    .collect(Collectors.joining(", ", "[", "]")));
        }
    }
}
