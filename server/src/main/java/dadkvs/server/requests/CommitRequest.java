package dadkvs.server.requests;

public class CommitRequest extends AbsRequest{

    private int key1;

    private int version1;

    private int key2;

    private int version2;

    private int writeKey;

    private int writeValue;

    public CommitRequest(int reqid, int key1, int version1, int key2, int version2, int writeKey, int writeValue) {
        super(reqid);
        this.key1 = key1;
        this.version1 = version1;
        this.key2 = key2;
        this.version2 = version2;
        this.writeKey = writeKey;
        this.writeValue = writeValue;
    }

    public int getKey1() {
        return this.key1;
    }

    public int getVersion1() {
        return this.version1;
    }

    public int getKey2() {
        return this.key2;
    }

    public int getVersion2() {
        return this.version2;
    }

    public int getWriteKey() {
        return this.writeKey;
    }

    public int getWriteValue() {
        return this.writeValue;
    }

}
