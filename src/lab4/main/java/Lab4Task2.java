import mpi.MPI;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Lab4Task2 {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int rows = 4, cols = 5;
        double[][] matrix = new double[rows][cols];
        double[] matrixFlat = new double[rows * cols];

        if (rank == 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = Math.random() * 200 - 100;
                }
            }
            System.out.println("Matrix:");
            printMatrix(matrix);
            for (int i = 0; i < rows; i++) {
                System.arraycopy(matrix[i], 0, matrixFlat, i * cols, cols);
            }
        }

        MPI.COMM_WORLD.Bcast(matrixFlat, 0, rows * cols, MPI.DOUBLE, 0);

        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrixFlat, i * cols, matrix[i], 0, cols);
        }

        int rowsPerProcess = rows / size;
        int extraRows = rows % size;
        int myRows = rank < extraRows ? rowsPerProcess + 1 : rowsPerProcess;
        int offset = rank * rowsPerProcess + Math.min(rank, extraRows);

        double[] localMatrixFlat = new double[myRows * cols];

        MPI.COMM_WORLD.Scatter(matrixFlat, 0, myRows * cols, MPI.DOUBLE,
                localMatrixFlat, 0, myRows * cols, MPI.DOUBLE, 0);

        double localPositiveSum = 0;
        double localNegativeSum = 0;

        for (int i = 0; i < myRows; i++) {
            int globalRow = offset + i;
            for (int j = 0; j < cols; j++) {
                double value = localMatrixFlat[i * cols + j];
                if (globalRow < rows / 2 && value > 0) {
                    localPositiveSum += value;
                } else if (globalRow >= rows / 2 && value < 0) {
                    localNegativeSum += value;
                }
            }
        }

        double[] totalPositiveSum = new double[1];
        double[] totalNegativeSum = new double[1];

        MPI.COMM_WORLD.Reduce(new double[]{localPositiveSum}, 0,
                totalPositiveSum, 0, 1, MPI.DOUBLE, MPI.SUM, 0);
        MPI.COMM_WORLD.Reduce(new double[]{localNegativeSum}, 0,
                totalNegativeSum, 0, 1, MPI.DOUBLE, MPI.SUM, 0);

        if (rank == 0) {
            double result = totalPositiveSum[0] * totalNegativeSum[0];
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
