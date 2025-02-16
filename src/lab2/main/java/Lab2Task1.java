import mpi.MPI;

public class Lab2Task1 {

    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int a = rank + 1;
        int b = (rank + 1) * 2;

        System.out.printf("Process %d (Thread ID: %d)\n", rank, Thread.currentThread().getId());
        System.out.printf("    Initial values: a%d = %d, b%d = %d\n", rank, a, rank, b);

        int[] received_a = new int[1];
        int[] received_b = new int[1];
        int[] send_a_to = {2, 0, 3, 1};
        int[] send_b_to = {1, 3, 0, 2};
        int[] recv_a_from = {1, 3, 0, 2};
        int[] recv_b_from = {2, 0, 3, 1};

        if (rank == 0 || rank == 3) {
            sendData(a, b, send_a_to[rank], send_b_to[rank]);
            receiveData(received_a, received_b, recv_a_from[rank], recv_b_from[rank]);
        } else {
            receiveData(received_a, received_b, recv_a_from[rank], recv_b_from[rank]);
            sendData(a, b, send_a_to[rank], send_b_to[rank]);
        }

        System.out.println(" \n");
        System.out.printf("Process %d received: a = %d, b = %d\n", rank, received_a[0], received_b[0]);
        System.out.printf("    Computed value c%d = a + b = %d + %d = %d", rank, received_a[0], received_b[0], (received_a[0] + received_b[0]));

        MPI.Finalize();
    }

    private static void sendData(int a, int b, int send_a_to, int send_b_to) {
        MPI.COMM_WORLD.Send(new int[]{a}, 0, 1, MPI.INT, send_a_to, 99);
        MPI.COMM_WORLD.Send(new int[]{b}, 0, 1, MPI.INT, send_b_to, 99);
    }

    private static void receiveData(int[] received_a, int[] received_b, int recv_a_from, int recv_b_from) {
        MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, recv_a_from, 99);
        MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, recv_b_from, 99);
    }
}
