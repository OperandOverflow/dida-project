
package dadkvs.server;

/* these imported classes are generated by the contract */
import dadkvs.*;

import dadkvs.server.paxos.messages.*;
import dadkvs.server.requests.OrdedRequest;

        import io.grpc.stub.StreamObserver;

public class DadkvsPaxosServiceImpl extends DadkvsPaxosServiceGrpc.DadkvsPaxosServiceImplBase {


    ServerState server_state;

    // TODO: we could put this in its own class?
    /**
     * You got it, boss
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⡠⠤⠤⠰⠒⠒⠒⠒⠒⠀⠤⠤⢀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡤⠒⠈⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠑⠢⢄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡴⠊⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⣀⠀⠀⠈⢲⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠾⠋⠀⠀⠀⢀⣾⡍⠉⠉⢙⣟⣻⣿⡆⠀⠀⠀⠀⠀⠀⢴⣯⣀⣀⣤⣚⣛⣿⡄⠀⠑⢦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠀⣀⠤⣀⡾⠁⠀⠀⠀⠀⠀⠘⢿⡿⠿⠟⠛⠛⠋⠉⠀⠀⠀⠀⠀⠀⠀⠈⠉⠉⠛⠛⠛⠿⠟⠀⠀⠀⠀⠑⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠀⢀⠤⠤⡠⡚⡁⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⢠⠏⠀⠀⢸⡷⢮⣶⡟⠒⠤⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⢸⡀⠒⠚⡷⣷⣾⡋⢠⣇⠀⠈⢱⠀⠀⠀⣀⣠⣤⣶⣶⣶⣶⣶⣶⣤⣄⡀⡀⠀⠀⠀⢀⣀⣤⣶⣿⣿⣿⣿⣿⣷⣷⣤⣤⣤⣤⣾⣿⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠸⡃⠤⠼⢣⢠⢳⣿⠿⣿⣷⣶⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⢻⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⣰⠝⠀⠀⣼⡾⣼⣄⣠⣾⡏⠀⢸⠛⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠋⠀⠸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⢠⡏⠀⠀⠀⢨⠟⠁⠀⠘⡇⠉⣤⠎⠀⢹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠇⠀⠀⠀⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠀⡾⠀⠀⠀⠠⡸⡀⠀⠀⢀⣠⠞⠀⠀⠀⠘⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡟⠙⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣿⣿⣿⠏⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⣴⠧⣀⣀⢠⡾⣥⠷⠒⠉⠉⠀⠀⠀⠀⠀⠀⠈⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠏⠀⠀⠘⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠃⠀⠀⠀⠀⢀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     * ⠈⠓⠦⢄⣩⠽⡏⠀⠀⠀⠀⠀⠀⠀⢀⡀⠠⣀⣀⡉⠛⠻⠿⣿⣿⡿⠿⠟⠋⠀⠀⠀⠀⠀⠀⠉⠙⠛⠛⠿⠛⢟⣋⣥⣴⢾⡄⠀⠀⠀⢠⠃⠀⠀⠀⠀⠀⣀⡀⠀⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⢣⠀⠀⠀⠀⠀⠀⠠⠟⠛⠻⣗⠒⣶⠶⠧⠤⣤⣤⣀⣀⣀⣀⣀⣀⣀⣀⣤⣤⡤⢤⣤⣶⡾⠿⣟⡿⠟⠁⠀⠘⠁⠀⠀⡼⠀⠀⠀⠀⠀⣾⠁⠈⢣⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠸⡠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠛⢿⣄⡀⢀⡆⠀⠀⠀⡇⠀⠀⠘⡏⠀⠀⢉⣇⣀⣀⣽⠤⣿⠟⠁⠀⠀⠀⠀⠀⠀⢰⠇⠀⠀⠀⠀⠀⡟⠤⠤⢺⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠑⢿⡢⣍⡟⠉⠉⠉⡏⠉⠉⠉⡏⠉⠉⢹⠁⠀⠀⢱⡴⠁⠀⠀⠀⠀⠀⠀⠀⠀⡜⠀⠀⠀⠀⠀⢰⢇⣀⡠⢿⠀⠀⠀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠈⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⠲⢤⣀⡇⠀⠀⠀⡇⠀⠀⢸⣀⠤⠖⠁⠀⢀⡄⠀⠀⠀⠀⠀⠀⡜⠀⠀⠀⠀⠀⠀⢸⡟⣤⣤⠈⡦⣀⣀⠀⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠘⢆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢄⡀⠀⠀⠀⠀⠈⠉⠉⠉⠉⠉⠉⠀⠀⠀⠀⢀⡴⠋⠀⠀⠀⠀⠀⢀⠜⠀⠀⠀⠀⠀⠀⠀⣸⢀⡿⠁⠀⠀⠀⠀⠙⣦
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢣⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⣤⣀⡀⢀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⠞⠉⠀⠀⠀⠀⠀⠀⡰⠋⠀⠀⠀⠀⠀⠀⠀⣼⠏⢈⣧⠧⠤⡤⣤⣤⣴⣟
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠑⠒⠒⠒⠒⠒⠂⠟⠋⠁⠀⠀⠀⠀⠀⢀⡰⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣦⣾⠹⠀⠀⠀⠀⠀⠉⢳
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠢⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢿⣫⣿⠦⠤⠤⠤⣤⡤⠿
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠢⠤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣠⠤⠒⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⡅⠀⠀⠀⠀⢘⡇⠀
     * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠐⠒⠒⠂⠤⠤⠤⠴⠒⠒⠒⠋⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠒⠂⠒⠦⠚⠁⠀
     */

    public DadkvsPaxosServiceImpl(ServerState state) {
        this.server_state = state;
    }

    @Override
    public void phaseone(DadkvsPaxos.PhaseOneRequest request, StreamObserver<DadkvsPaxos.PhaseOneReply> responseObserver) {
	    // for debug purposes
	    System.out.println("Receive phase1 request: " + request);

        // Extract details from the PhaseOneRequest
        int phase1config = request.getPhase1Config(); //for next steps of the project
        int phase1index = request.getPhase1Index();
        int phase1timestamp = request.getPhase1Timestamp();
        int phase1transactionNumber = request.getPhase1Txid();

        PrepareMsg prepareMsg = new PrepareMsg(phase1timestamp, phase1index, phase1config, phase1transactionNumber);

        PromiseMsg promiseMsg = server_state.paxos.prepare(prepareMsg);

        DadkvsPaxos.PaxosValue acceptedValue = null;
        if (promiseMsg.prevAcceptedValue != null && promiseMsg.prevAcceptedValue.getValue() != null) {
            acceptedValue = DadkvsPaxos.PaxosValue.newBuilder()
                    .setRequestid(promiseMsg.prevAcceptedValue.getValue().getRequestId())
                    .setRequestseq(promiseMsg.prevAcceptedValue.getValue().getRequestSeq())
                    .build();
        } else {
            acceptedValue = DadkvsPaxos.PaxosValue.newBuilder()
                    .setRequestid(-1)
                    .setRequestseq(-1)
                    .build();
        }

        DadkvsPaxos.PhaseOneReply reply = DadkvsPaxos.PhaseOneReply.newBuilder()
                                                    .setPhase1Config(promiseMsg.configNumber)
                                                    .setPhase1Index(promiseMsg.leaderId)
                                                    .setPhase1Timestamp(promiseMsg.prevAcceptedRoundNumber)
                                                    .setPhase1Accepted(promiseMsg.accepted)
                                                    .setPhase1Value(acceptedValue)
                                                    .build();

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
        int phase2transactionNumber = request.getPhase2Txid();

        OrdedRequest ordedRequest = new OrdedRequest(phase2value.getRequestseq(), phase2value.getRequestid());
        dadkvs.server.paxos.PaxosValue acceptValue = new dadkvs.server.paxos.PaxosValue(ordedRequest);

        AcceptMsg acceptMsg = new AcceptMsg(phase2timestamp, phase2index, phase2config, acceptValue, phase2transactionNumber);

        AcceptedMsg acceptedMsg = server_state.paxos.accept(acceptMsg);

        DadkvsPaxos.PhaseTwoReply reply = DadkvsPaxos.PhaseTwoReply.newBuilder()
                                                    .setPhase2Accepted(acceptedMsg.accepted)
                                                    .setPhase2Config(phase2config)
                                                    .setPhase2Index(phase2index)
                                                    .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }


    @Override
    public void learn(DadkvsPaxos.LearnRequest request, StreamObserver<DadkvsPaxos.LearnReply> responseObserver) {

        // for debug purposes
        System.out.println("Receive learn request: " + request);

        int learnConfig = request.getLearnconfig();  
        int learnIndex = request.getLearnindex();
        int learnTimestamp = request.getLearntimestamp();
        DadkvsPaxos.PaxosValue learnValue = request.getLearnvalue();
        int learnTransactionNumber = request.getLearntxid();

        OrdedRequest ordedRequest = new OrdedRequest(learnValue.getRequestseq(), learnValue.getRequestid());
        dadkvs.server.paxos.PaxosValue acceptValue = new dadkvs.server.paxos.PaxosValue(ordedRequest);

        LearnMsg learnMsg = new LearnMsg(learnTimestamp, learnIndex, learnConfig, acceptValue, learnTransactionNumber);

        LearnedMsg learnedMsg = server_state.paxos.learn(learnMsg);

        DadkvsPaxos.LearnReply reply = DadkvsPaxos.LearnReply.newBuilder()
                                                .setLearnaccepted(learnedMsg.accepted)
                                                .setLearnconfig(learnMsg.configNumber)
                                                .setLearnindex(learnMsg.leaderId)
                                                .build();


        System.out.println("Learned value for index " + learnIndex + ": " + learnValue);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
        
    }

}
