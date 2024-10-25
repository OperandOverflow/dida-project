package dadkvs.server;


import java.util.Random;

public class ConsoleConfig {

    private final ServerState server_state;

    public ConsoleConfig(ServerState state) {
        this.server_state = state;
    }

//    /**
//     * Set the leader status of the server, if the server becomes the
//     * leader, and it has pending requests, it will send the requests
//     * to other servers.
//     *
//     * @param isLeader The leader status of the server.
//     */
//    public void setLeader(boolean isLeader) {
//        this.server_state.i_am_leader.set(isLeader);
//        if (isLeader) {
//            Thread thread = new Thread(
//                    () -> server_state.request_handler.startOrderRequests()
//            );
//            thread.start();
//        } else {
//            this.server_state.request_handler.stopOrderRequests();
//        }
//    }

    public void setDebug(int mode) {
        if (mode == 1)
            crashThread();
        else if (mode == 3) {
            this.server_state.debug_mode.set(0);
            unfreezeThread();
        } else if (mode == 5) {
            this.server_state.debug_mode.set(0);
            unSlow();
        } else
            this.server_state.debug_mode.set(mode);
    }

    public void goDebug() {
        switch (this.server_state.debug_mode.get()) {
            case 1, 6:
                crashThread();
                break;
            case 2:
                freezeThread();
                break;
            case 4:
                randomSlow();
                break;
            default:
                break;
        }
    }

    private void crashThread() {
        try {
            // Simulate doing some work.
            System.out.println("Thread is going to crash oh nooo");
            System.out.println("Our threads are broken, but out hearts not!");
            System.out.println("Unsere Faden brechen, aber unsere Herzen nicht!");
            Thread.sleep(1000); // Simulate some process is going on

            // Throwing an exception
            //throw new RuntimeException("Runtime exception for debug mode - crash");
            this.server_state.shutdown();
            System.exit(-1);
        } catch (InterruptedException e) {
            // Handle interrupted thread.
            System.err.println("Thread interrupted.");
        }
    }

    public void freezeThread() {
        synchronized (this.server_state.freezeLock) {
            try {
                System.out.println("Thread is frozen");
                this.server_state.freezeLock.wait();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted.");
            }
        }
    }

    public void unfreezeThread() {
        synchronized (this.server_state.freezeLock) {
            this.server_state.freezeLock.notifyAll();
            System.out.println("Thread is unfrozen");
        }
    }

    public void randomSlow() {
        Random rnd = new Random();
        int mimiting = rnd.nextInt(0, 10);
        try{
            Thread.sleep(mimiting* 1000L);
        } catch (InterruptedException e) {
            System.out.println("System is in Debug Mode - Slow");
        }
    }

    public void unSlow() {
        this.server_state.debug_mode.set(0);
        System.out.println("Thread is unslowed");
    }
}
