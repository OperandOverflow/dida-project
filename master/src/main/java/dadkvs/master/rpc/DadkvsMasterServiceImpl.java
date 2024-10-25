package dadkvs.master.rpc;

import dadkvs.DadkvsMasterServiceGrpc;
import dadkvs.DadkvsMaster;

import dadkvs.master.VerticalPaxosMaster;
import io.grpc.stub.StreamObserver;

public class DadkvsMasterServiceImpl extends DadkvsMasterServiceGrpc.DadkvsMasterServiceImplBase {

    private VerticalPaxosMaster master;

    public DadkvsMasterServiceImpl(VerticalPaxosMaster master) {
        this.master = master;
    }

    @Override
    public void setleader(DadkvsMaster.DefineLeaderRequest request, StreamObserver<DadkvsMaster.DefineLeaderReply> responseObserver) {
        boolean isLeader = request.getIsleader();
        int leaderId = request.getServerid();

        boolean result = master.setLeader(isLeader, leaderId);

        DadkvsMaster.DefineLeaderReply reply = DadkvsMaster.DefineLeaderReply.newBuilder().setIsleaderack(result).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void reconfig(DadkvsMaster.ReconfigRequest request, StreamObserver<DadkvsMaster.ReconfigReply> responseObserver) {
        int config = request.getConfignum();

        boolean result = master.reconfig(config);

        DadkvsMaster.ReconfigReply reply = DadkvsMaster.ReconfigReply.newBuilder().setAck(result).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void reconfigchangeleader(DadkvsMaster.ReconfigChangeLeaderRequest request, StreamObserver<DadkvsMaster.ReconfigChangeLeaderReply> responseObserver) {
        int config = request.getConfignum();
        int leaderId = request.getServerid();

        boolean result = master.reconfigChangeLeader(config, leaderId);

        DadkvsMaster.ReconfigChangeLeaderReply reply = DadkvsMaster.ReconfigChangeLeaderReply.newBuilder().setAck(result).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void complete(DadkvsMaster.Complete request, StreamObserver<DadkvsMaster.Activated> responseObserver) {
        int ballot = request.getBallotnum();

        boolean activated = master.completed(ballot);

        DadkvsMaster.Activated reply = DadkvsMaster.Activated.newBuilder().setActivated(activated).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
