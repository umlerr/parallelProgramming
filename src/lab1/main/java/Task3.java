import mpi.MPI;

public class Task3 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                if ((i * 10 + j) % size == rank) {
                    System.out.printf("Process %d (ID: %d): %2d x %2d = %3d%n",
                            rank, Thread.currentThread().getId(), i, j, i * j);
                }
            }
        }
        MPI.Finalize();
    }
}
