import mpi.MPI;

public class Task1 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        if (rank < 2) {
            System.out.println("Process " + rank + " is running.");
        }
        MPI.Finalize();
    }
}