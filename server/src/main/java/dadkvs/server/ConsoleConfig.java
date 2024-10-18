package dadkvs.server;


public class ConsoleConfig {

    private final ServerState server_state;
    private final Object freezeObject = new Object();
    Thread frozenThread;

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
                crashThread();
                break;
            case 2:
                freezeThread();
                break;
            case 3:
                unfreezeThread();

        }
    }

    private void crashThread(){
        Thread crashThread = new Thread(() -> {
            try {
                // Simulate doing some work.
                System.out.println("Thread is going to crash oh nooo");
                Thread.sleep(2000); // Simulate some process is going on

                // Throwing an exception
                throw new RuntimeException("Runtime exception for debug mode - crash");
            } catch (InterruptedException e) {
                // Handle interrupted thread.
                System.err.println("Thread interrupted.");
            }
        });
        crashThread.start();
    }

    private void freezeThread(){
        frozenThread = new Thread(() -> {
            synchronized (this.freezeObject) {
                try {
                    System.out.println("Thread is about to freeze oh nooo");
                    // Wait indefinitely
                    this.freezeObject.wait();
                } catch (InterruptedException e) {
                    System.err.println("Thread was interrupted debug mode - freeze");
                }
            }
        });
        frozenThread.start();
    }

    private void unfreezeThread(){
        synchronized (this.freezeObject) {
            if(frozenThread != null){
                frozenThread.notify();
                System.out.println("Thread is unfrozen");
            }else{
                System.out.println("No Thread to be unfrozen");
            }
        }
    }
}
