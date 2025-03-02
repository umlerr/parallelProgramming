import mpi.MPI;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Lab3Task1 {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int rows = 4, cols = 5;
        double[][] matrix = null;
        double[][] normalizedMatrix = new double[rows][cols];

        if (rank == 0) {
            matrix = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = Math.random() * 100;
                }
            }
            System.out.println("Matrix:");
            printMatrix(matrix);
        }

        int rowsPerProcess = rows / size;
        int extraRows = rows % size;
        int myRows = rank < extraRows ? rowsPerProcess + 1 : rowsPerProcess;

        double[][] localMatrix = new double[myRows][cols];

        if (rank == 0) {
            int offset = 0;
            for (int i = 0; i < size; i++) {
                int sendRows = (i < extraRows) ? rowsPerProcess + 1 : rowsPerProcess;
                for (int j = 0; j < sendRows; j++) {
                    if (i == 0) {
                        System.arraycopy(matrix[offset + j], 0, localMatrix[j], 0, cols);
                    } else {
                        MPI.COMM_WORLD.Send(matrix[offset + j], 0, cols, MPI.DOUBLE, i, 0);
                    }
                }
                offset += sendRows;
            }
        } else {
            for (int i = 0; i < myRows; i++) {
                MPI.COMM_WORLD.Recv(localMatrix[i], 0, cols, MPI.DOUBLE, 0, 0);
            }
        }

        for (int i = 0; i < myRows; i++) {
            double min = Arrays.stream(localMatrix[i]).min().orElse(0);
            double max = Arrays.stream(localMatrix[i]).max().orElse(1);
            for (int j = 0; j < cols; j++) {
                localMatrix[i][j] = (localMatrix[i][j] - min) / (max - min);
            }
        }

        if (rank == 0) {
            int offset = 0;
            for (int i = 0; i < size; i++) {
                int recvRows = (i < extraRows) ? rowsPerProcess + 1 : rowsPerProcess;
                for (int j = 0; j < recvRows; j++) {
                    if (i == 0) {
                        System.arraycopy(localMatrix[j], 0, normalizedMatrix[offset + j], 0, cols);
                    } else {
                        MPI.COMM_WORLD.Recv(normalizedMatrix[offset + j], 0, cols, MPI.DOUBLE, i, 0);
                    }
                }
                offset += recvRows;
            }
            System.out.println("Normalized Matrix:");
            printMatrix(normalizedMatrix);
        } else {
            for (int i = 0; i < myRows; i++) {
                MPI.COMM_WORLD.Send(localMatrix[i], 0, cols, MPI.DOUBLE, 0, 0);
            }
        }

        MPI.Finalize();
    }

    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println(Arrays.stream(row)
                    .mapToObj(v -> String.format("%.2f", v)) // Округляем до 2 знаков
                    .collect(Collectors.joining(", ", "[", "]"))); // Форматируем как массив
        }
    }
}
