package dadkvs.server;


public class ConsoleConfig {

    private final ServerState server_state;

    public ConsoleConfig(ServerState state) {
        this.server_state = state;
    }

    /**
     * Set the leader status of the server, if the server becomes the
     * leader, and it has pending requests, it will send the requests
     * to other servers.
     * @param isLeader The leader status of the server.
     */
    public void setLeader(boolean isLeader) {
        this.server_state.i_am_leader.set(isLeader);
        if (isLeader) {
            Thread thread = new Thread(
                    () -> server_state.request_handler.startOrderRequests()
            );
            thread.start();
        } else {
            this.server_state.request_handler.stopOrderRequests();
        }
    }

    public void setDebug(int mode) {
        switch(mode){
            case 1: //Debug Mode 1 Crash
                Thread crashThread = new Thread(() -> {
                    try {
                        // Simulate doing some work.
                        System.out.println("Thread is running... about to crash.");
                        Thread.sleep(2000); // Simulate some process is going on

                        // Throwing an exception
                        throw new RuntimeException("Simulated thread crash for debugging purposes.");
                    } catch (InterruptedException e) {
                        // Handle interrupted thread.
                        System.err.println("Thread interrupted.");
                    }
                });
                crashThread.start();
                break;

        }
    }
}
