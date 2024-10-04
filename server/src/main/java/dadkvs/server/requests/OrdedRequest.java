package dadkvs.server.requests;

public class OrdedRequest {

    private int reqid;
    private int requestSeq;


    public OrdedRequest(int reqid, int requestSeq) {
        super();
        this.reqid = reqid;
        this.requestSeq = requestSeq;
    }


    public int getRequestSeq() {
        return reqid;
    }

    public int getRequestId() {
        return requestSeq;
    }
}
