package dadkvs;

import io.grpc.stub.StreamObserver;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 **
 * Service for subordinating servers to receive the order of requests from the leader server
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.36.0)",
    comments = "Source: DadkvsServerSync.proto")
public final class DadkvsServerSyncServiceGrpc {

  private DadkvsServerSyncServiceGrpc() {}

  public static final String SERVICE_NAME = "dadkvs.DadkvsServerSyncService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<dadkvs.DadkvsServerSync.RequestOrder,
      dadkvs.DadkvsServerSync.Empty> getReceiveReqOrderMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "receiveReqOrder",
      requestType = dadkvs.DadkvsServerSync.RequestOrder.class,
      responseType = dadkvs.DadkvsServerSync.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<dadkvs.DadkvsServerSync.RequestOrder,
      dadkvs.DadkvsServerSync.Empty> getReceiveReqOrderMethod() {
    io.grpc.MethodDescriptor<dadkvs.DadkvsServerSync.RequestOrder, dadkvs.DadkvsServerSync.Empty> getReceiveReqOrderMethod;
    if ((getReceiveReqOrderMethod = DadkvsServerSyncServiceGrpc.getReceiveReqOrderMethod) == null) {
      synchronized (DadkvsServerSyncServiceGrpc.class) {
        if ((getReceiveReqOrderMethod = DadkvsServerSyncServiceGrpc.getReceiveReqOrderMethod) == null) {
          DadkvsServerSyncServiceGrpc.getReceiveReqOrderMethod = getReceiveReqOrderMethod =
              io.grpc.MethodDescriptor.<dadkvs.DadkvsServerSync.RequestOrder, dadkvs.DadkvsServerSync.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "receiveReqOrder"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dadkvs.DadkvsServerSync.RequestOrder.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  dadkvs.DadkvsServerSync.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DadkvsServerSyncServiceMethodDescriptorSupplier("receiveReqOrder"))
              .build();
        }
      }
    }
    return getReceiveReqOrderMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DadkvsServerSyncServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DadkvsServerSyncServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DadkvsServerSyncServiceStub>() {
        @java.lang.Override
        public DadkvsServerSyncServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DadkvsServerSyncServiceStub(channel, callOptions);
        }
      };
    return DadkvsServerSyncServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DadkvsServerSyncServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DadkvsServerSyncServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DadkvsServerSyncServiceBlockingStub>() {
        @java.lang.Override
        public DadkvsServerSyncServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DadkvsServerSyncServiceBlockingStub(channel, callOptions);
        }
      };
    return DadkvsServerSyncServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DadkvsServerSyncServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DadkvsServerSyncServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DadkvsServerSyncServiceFutureStub>() {
        @java.lang.Override
        public DadkvsServerSyncServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DadkvsServerSyncServiceFutureStub(channel, callOptions);
        }
      };
    return DadkvsServerSyncServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   **
   * Service for subordinating servers to receive the order of requests from the leader server
   * </pre>
   */
  public static abstract class DadkvsServerSyncServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void receiveReqOrder(dadkvs.DadkvsServerSync.RequestOrder request,
        io.grpc.stub.StreamObserver<dadkvs.DadkvsServerSync.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReceiveReqOrderMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getReceiveReqOrderMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                dadkvs.DadkvsServerSync.RequestOrder,
                dadkvs.DadkvsServerSync.Empty>(
                  this, METHODID_RECEIVE_REQ_ORDER)))
          .build();
    }
  }

  /**
   * <pre>
   **
   * Service for subordinating servers to receive the order of requests from the leader server
   * </pre>
   */
  public static final class DadkvsServerSyncServiceStub extends io.grpc.stub.AbstractAsyncStub<DadkvsServerSyncServiceStub> {
    private DadkvsServerSyncServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DadkvsServerSyncServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DadkvsServerSyncServiceStub(channel, callOptions);
    }

    /**
     */
    public void receiveReqOrder(DadkvsServerSync.RequestOrder request,
                                StreamObserver<DadkvsServerSync.RequestOrder> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReceiveReqOrderMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   **
   * Service for subordinating servers to receive the order of requests from the leader server
   * </pre>
   */
  public static final class DadkvsServerSyncServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<DadkvsServerSyncServiceBlockingStub> {
    private DadkvsServerSyncServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DadkvsServerSyncServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DadkvsServerSyncServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public dadkvs.DadkvsServerSync.Empty receiveReqOrder(dadkvs.DadkvsServerSync.RequestOrder request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReceiveReqOrderMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   **
   * Service for subordinating servers to receive the order of requests from the leader server
   * </pre>
   */
  public static final class DadkvsServerSyncServiceFutureStub extends io.grpc.stub.AbstractFutureStub<DadkvsServerSyncServiceFutureStub> {
    private DadkvsServerSyncServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DadkvsServerSyncServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DadkvsServerSyncServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<dadkvs.DadkvsServerSync.Empty> receiveReqOrder(
        dadkvs.DadkvsServerSync.RequestOrder request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReceiveReqOrderMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RECEIVE_REQ_ORDER = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DadkvsServerSyncServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DadkvsServerSyncServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RECEIVE_REQ_ORDER:
          serviceImpl.receiveReqOrder((dadkvs.DadkvsServerSync.RequestOrder) request,
              (io.grpc.stub.StreamObserver<dadkvs.DadkvsServerSync.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class DadkvsServerSyncServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DadkvsServerSyncServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return dadkvs.DadkvsServerSync.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DadkvsServerSyncService");
    }
  }

  private static final class DadkvsServerSyncServiceFileDescriptorSupplier
      extends DadkvsServerSyncServiceBaseDescriptorSupplier {
    DadkvsServerSyncServiceFileDescriptorSupplier() {}
  }

  private static final class DadkvsServerSyncServiceMethodDescriptorSupplier
      extends DadkvsServerSyncServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DadkvsServerSyncServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DadkvsServerSyncServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DadkvsServerSyncServiceFileDescriptorSupplier())
              .addMethod(getReceiveReqOrderMethod())
              .build();
        }
      }
    }
    return result;
  }
}
