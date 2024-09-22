package dadkvs.server.entities;

public class ReadRequest extends AbsRequest{

    private int key;

    public ReadRequest(int reqid, int key) {
        super(reqid);
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
