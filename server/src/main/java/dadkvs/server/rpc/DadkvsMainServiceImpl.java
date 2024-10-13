package dadkvs.server.rpc;

/* these imported classes are generated by the contract */
import dadkvs.*;

import dadkvs.server.ServerState;
import dadkvs.server.VersionedValue;
import dadkvs.server.requests.*;
import io.grpc.stub.StreamObserver;

public class DadkvsMainServiceImpl extends DadkvsMainServiceGrpc.DadkvsMainServiceImplBase {

    ServerState server_state;
    int               timestamp;
    
    public DadkvsMainServiceImpl(ServerState state) {
        this.server_state = state;
		this.timestamp = 0;
    }

    @Override
    public void read(DadkvsMain.ReadRequest request, StreamObserver<DadkvsMain.ReadReply> responseObserver) {
		// for debug purposes
		System.out.println("Receiving read request:" + request);

		// Convert the request to the internal format
		ReadRequest readRequest = new ReadRequest(request.getReqid(), request.getKey());

		// Process the request
		VersionedValue vv = this.server_state.request_handler.handleReadRequest(readRequest);

		DadkvsMain.ReadReply response =DadkvsMain.ReadReply.newBuilder()
			.setReqid(request.getReqid()).setValue(vv.getValue()).setTimestamp(vv.getVersion()).build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
    }

    @Override
    public void committx(DadkvsMain.CommitRequest request, StreamObserver<DadkvsMain.CommitReply> responseObserver) {
		// for debug purposes
		System.out.println("Receiving commit request:" + request);


		// Convert the request to the internal format
		CommitRequest commitRequest = new CommitRequest(
					request.getReqid(), request.getKey1(), request.getVersion1(),
					request.getKey2(), request.getVersion2(), request.getWritekey(),
					request.getWriteval()
					);

		boolean result = this.server_state.request_handler.handleCommitRequest(commitRequest);

		// for debug purposes
		System.out.println("Result is ready for request with reqid " + request.getReqid());

		DadkvsMain.CommitReply response = DadkvsMain.CommitReply.newBuilder()
			.setReqid(request.getReqid()).setAck(result).build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
    }
}