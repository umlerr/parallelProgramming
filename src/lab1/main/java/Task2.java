import mpi.MPI;

public class Task2 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        if (rank < 3) {
            System.out.println("Process " + rank + " - ID: " + Thread.currentThread().getId());
        }
        MPI.Finalize();
    }
}