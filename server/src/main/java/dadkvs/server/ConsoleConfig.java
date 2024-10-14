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
        //TODO: Implement this method
    }
}
