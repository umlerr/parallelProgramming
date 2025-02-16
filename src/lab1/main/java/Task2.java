import mpi.MPI;

public class Task2 {
    public static void main(String[] args) {
        MPI.Init(args);
        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();
        System.out.println("Process " + rank + " of " + size + " - ID: " + Thread.currentThread().getId());
        MPI.Finalize();
    }
}
