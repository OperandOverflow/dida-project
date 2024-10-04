
package dadkvs.server;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/* these imported classes are generated by the contract */
import dadkvs.*;
import dadkvs.DadkvsPaxos.PaxosValue;

import dadkvs.util.GenericResponseCollector;
import dadkvs.util.CollectorStreamObserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class DadkvsPaxosServiceImpl extends DadkvsPaxosServiceGrpc.DadkvsPaxosServiceImplBase {


    DadkvsServerState server_state;

    // TODO: we could put this in its own class?

    /** Broadcast control variables */
    private final int   n_servers = 5;
    private ManagedChannel[] channels;
    private DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] async_stubs;

    private int lastTimestamp;

    private int acceptedTimestamp;
    private DadkvsPaxos.PaxosValue acceptedValue;


    public DadkvsPaxosServiceImpl(DadkvsServerState state) {
        this.server_state = state;
        this.lastTimestamp = -1;
        this.acceptedTimestamp = -1;
        this.acceptedValue = null;
        initiate();
    }

    /**
     * This method initializes the gRPC channels and stubs for
     * the server-to-server communication.
     */
    private void initiate() {
        this.channels = new ManagedChannel[n_servers];
        this.async_stubs = new DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[n_servers];
        String localhost = "localhost";
        for(int i = 0; i < n_servers; i++) {
            int port = this.server_state.base_port + i;
            this.channels[i] = ManagedChannelBuilder.forAddress(localhost, port).usePlaintext().build();
            this.async_stubs[i] = DadkvsPaxosServiceGrpc.newStub(this.channels[i]);
        }
    }


    @Override
    public void phaseone(DadkvsPaxos.PhaseOneRequest request, StreamObserver<DadkvsPaxos.PhaseOneReply> responseObserver) {
	    // for debug purposes
	    System.out.println("Receive phase1 request: " + request);

        // Extract details from the PhaseOneRequest
        int phase1config = request.getPhase1Config(); //for next steps of the project
        int phase1index = request.getPhase1Index();
        int phase1timestamp = request.getPhase1Timestamp();

        DadkvsPaxos.PhaseOneReply reply;

        if (phase1timestamp < lastTimestamp) {
            reply = DadkvsPaxos.PhaseOneReply.newBuilder()
                    .setPhase1Config(phase1config)
                    .setPhase1Index(phase1index)
                    .setPhase1Accepted(false)
                    .build();
        } else {
            lastTimestamp = phase1timestamp;
            reply = DadkvsPaxos.PhaseOneReply.newBuilder()
                    .setPhase1Config(phase1config)
                    .setPhase1Index(phase1index)
                    .setPhase1Timestamp(acceptedTimestamp)
                    .setPhase1Accepted(true)
                    .setPhase1Value(acceptedValue)
                    .build();
        }

        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

    @Override
    public void phasetwo(DadkvsPaxos.PhaseTwoRequest request, StreamObserver<DadkvsPaxos.PhaseTwoReply> responseObserver) {
	    // for debug purposes
	    System.out.println ("Receive phase two request: " + request);

        //Variables regarding the phaseTwoRequest
        int phase2config = request.getPhase2Config();
        int phase2index = request.getPhase2Index();
        DadkvsPaxos.PaxosValue phase2value = request.getPhase2Value();
        int phase2timestamp = request.getPhase2Timestamp();

        DadkvsPaxos.PhaseTwoReply reply;

        if (phase2timestamp < lastTimestamp) {
            reply = DadkvsPaxos.PhaseTwoReply.newBuilder()
                    .setPhase2Accepted(false)
                    .setPhase2Config(request.getPhase2Config())
                    .setPhase2Index(phase2index)
                    .build();
        } else {
            reply = DadkvsPaxos.PhaseTwoReply.newBuilder()
                    .setPhase2Accepted(true)
                    .setPhase2Config(request.getPhase2Config())
                    .setPhase2Index(phase2index)
                    .build();

            // Tell all the learners about the accepted value
            DadkvsPaxos.LearnRequest learnRequest = DadkvsPaxos.LearnRequest.newBuilder()
                    .setLearnconfig(phase2config)
                    .setLearnindex(phase2index)
                    .setLearnvalue(phase2value)
                    .setLearntimestamp(phase2timestamp)
                    .build();

            ArrayList<DadkvsPaxos.LearnReply> responseList = new ArrayList<>();
            GenericResponseCollector<DadkvsPaxos.LearnReply> responseCollector = new GenericResponseCollector<>(responseList, n_servers);

            for (int i = 0; i < n_servers; i++) {
                DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub stub = this.async_stubs[i];
                StreamObserver<DadkvsPaxos.LearnReply> learnObserver = new CollectorStreamObserver<>(responseCollector);
                stub.learn(learnRequest, learnObserver);
            }
        }

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }


    @Override
    public void learn(DadkvsPaxos.LearnRequest request, StreamObserver<DadkvsPaxos.LearnReply> responseObserver) {

        // for debug purposes
        System.out.println("Receive learn request: " + request);

        int learnConfig = request.getLearnconfig();  
        int learnIndex = request.getLearnindex();  

        DadkvsPaxos.PaxosValue learnValue = request.getLearnvalue();

        acceptedValue = learnValue;

        DadkvsPaxos.LearnReply reply = DadkvsPaxos.LearnReply.newBuilder()
        .setLearnaccepted(true) 
        .setLearnconfig(learnConfig)
        .setLearnindex(learnIndex)
        .build();

        System.out.println("Learned value for index " + learnIndex + ": " + learnValue);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
        
    }

}
