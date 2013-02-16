Stampy, a Java implementation of the STOMP 1.2 specification

This project was built using Java 7 in Eclipse, with Apache Ant & Apache Ivy.  The artifacts produced are Java 5 compatible.
The text below is taken from the generated overview html in the Javadocs.  The build produces 6 artifacts:

stampy-core - the core message classes
stampy-client-server - the structure for building STOMP 1.2 clients and servers using stampy-core
stampy-NETTY-client-server-RI - the NETTY reference implementation of stampy-client-server
stampy-MINA-client-server-RI - the MINA reference implementation of stampy-client-server
stampy-examples - Examples using both the NETTY & MINA RIs
stampy-all - all of the above.

Release $version, $date

stampy-examples-$version

The Stampy example code demonstrates how to configure Stampy clients and servers to perform customized tasks.

Load Test

The load test attempts to send a configurable one million ACK messages from the client to the server and requests one million receipts from the server.

Log4j Level Changer

This example shows how to send a serializable Java object for execution on a server.

System

This example shows how to configure a STOMP 1.2 compliant client and server and tests for compliancy.

stampy-NETTY-client-server-RI-$version

This library provides a Netty implementation of the stampy-client-server-$version STOMP 1.2 architecture.

Design considerations

Compiled for Java 5 compatibility
Ease of configuration and use
DI framework-friendly
Minimal dependencies
Massively multithreaded, many clients can communicate with a server simultaneously
Fast (6 microseconds per message during testing)
Reliable
Extensible
Security focused

Dependencies

<dependency org="asia.stampy" name="stampy-core" rev="$version"/>
<dependency org="asia.stampy" name="stampy-client-server" rev="$version"/>
<dependency org="io.netty" name="netty" rev="3.6.2.Final"/>
<dependency org="org.slf4j" name="slf4j-api" rev="1.7.2"/>
<dependency org="commons-lang" name="commons-lang" rev="2.6"/>
<dependency org="commons-codec" name="commons-codec" rev="1.7"/>

Client Configuration (from examples)

While easy to programmatically configure a Netty client gateway, it is expected that a DI framework such as Spring or Guice will be used to perform this task.

The following code demonstrates all that is necessary to configure a STOMP 1.2 compliant client:

  public static AbstractStampyMessageGateway initialize() {
    HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ClientNettyMessageGateway gateway = new ClientNettyMessageGateway();
    gateway.setPort(1234);
    gateway.setHost("localhost");
    gateway.setHeartbeat(1000);

    ClientNettyChannelHandler channelHandler = new ClientNettyChannelHandler();
    channelHandler.setGateway(gateway);
    channelHandler.setHeartbeatContainer(heartbeatContainer);

    gateway.addMessageListener(new IDontNeedSecurity()); // DON'T DO THIS!!!
    
    gateway.addMessageListener(new ClientMessageValidationListener());

    NettyConnectedMessageListener cml = new NettyConnectedMessageListener();
    cml.setHeartbeatContainer(heartbeatContainer);
    cml.setGateway(gateway);
    gateway.addMessageListener(cml);

    NettyDisconnectListenerAndInterceptor disconnect = new NettyDisconnectListenerAndInterceptor();
    disconnect.setCloseOnDisconnectMessage(false);
    gateway.addMessageListener(disconnect);
    gateway.addOutgoingMessageInterceptor(disconnect);
    disconnect.setGateway(gateway);

    gateway.setHandler(channelHandler);

    return gateway;
  }
        
Server Configuration (from examples)

While easy to programmatically configure a Netty server gateway, it is expected that a DI framework such as Spring or Guice will be used to perform this task.

The following code demonstrates all that is necessary to configure a STOMP 1.2 compliant server:

  public static AbstractStampyMessageGateway initialize() {
    HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();
    gateway.setPort(1234);
    gateway.setHeartbeat(1000);
    gateway.setAutoShutdown(true);

    ServerNettyChannelHandler channelHandler = new ServerNettyChannelHandler();
    channelHandler.setGateway(gateway);
    channelHandler.setHeartbeatContainer(heartbeatContainer);

    gateway.addMessageListener(new IDontNeedSecurity()); // DON'T DO THIS!!!
    
    gateway.addMessageListener(new ServerMessageValidationListener());

    gateway.addMessageListener(new VersionListener());

    NettyLoginMessageListener login = new NettyLoginMessageListener();
    login.setGateway(gateway);
    login.setLoginHandler(new SystemLoginHandler());
    gateway.addMessageListener(login);

    NettyConnectStateListener connect = new NettyConnectStateListener();
    connect.setGateway(gateway);
    gateway.addMessageListener(connect);

    NettyHeartbeatListener heartbeat = new NettyHeartbeatListener();
    heartbeat.setHeartbeatContainer(heartbeatContainer);
    heartbeat.setGateway(gateway);
    gateway.addMessageListener(heartbeat);

    NettyTransactionListener transaction = new NettyTransactionListener();
    transaction.setGateway(gateway);
    gateway.addMessageListener(transaction);

    SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

    NettyAcknowledgementListenerAndInterceptor acknowledgement = new NettyAcknowledgementListenerAndInterceptor();
    acknowledgement.setHandler(sys);
    acknowledgement.setGateway(gateway);
    acknowledgement.setAckTimeoutMillis(200);
    gateway.addMessageListener(acknowledgement);
    gateway.addOutgoingMessageInterceptor(acknowledgement);

    NettyReceiptListener receipt = new NettyReceiptListener();
    receipt.setGateway(gateway);
    gateway.addMessageListener(receipt);

    NettyConnectResponseListener connectResponse = new NettyConnectResponseListener();
    connectResponse.setGateway(gateway);
    gateway.addMessageListener(connectResponse);

    gateway.setHandler(channelHandler);

    return gateway;
  }
        
stampy-MINA-client-server-RI-$version

This library provides a Mina implementation of the stampy-client-server-$version STOMP 1.2 architecture.

Design considerations

Compiled for Java 5 compatibility
Ease of configuration and use
DI framework-friendly
Minimal dependencies
Massively multithreaded, many clients can communicate with a server simultaneously
Fast (11 microseconds per message during testing)
Reliable
Extensible
Security focused

Dependencies

<dependency org="asia.stampy" name="stampy-core" rev="$version"/>
<dependency org="asia.stampy" name="stampy-client-server" rev="$version"/>
<dependency org="org.apache.mina" name="mina-core" rev="2.0.7"/>
<dependency org="org.slf4j" name="slf4j-api" rev="1.7.2"/>
<dependency org="commons-lang" name="commons-lang" rev="2.6"/>
<dependency org="commons-codec" name="commons-codec" rev="1.7"/>

Client Configuration (from examples)

While easy to programmatically configure a Mina client gateway, it is expected that a DI framework such as Spring or Guice will be used to perform this task.

The following code demonstrates all that is necessary to configure a STOMP 1.2 compliant client:

  public static AbstractStampyMessageGateway initialize() {
    HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ClientMinaMessageGateway gateway = new ClientMinaMessageGateway();
    gateway.setPort(1234);
    gateway.setHost("localhost");
    gateway.setHeartbeat(1000);

    RawClientMinaHandler handler = new RawClientMinaHandler();
    handler.setGateway(gateway);
    handler.setHeartbeatContainer(heartbeatContainer);

    gateway.addMessageListener(new IDontNeedSecurity()); // DON'T DO THIS!!!
    
    gateway.addMessageListener(new ClientMessageValidationListener());

    MinaConnectedMessageListener cml = new MinaConnectedMessageListener();
    cml.setHeartbeatContainer(heartbeatContainer);
    cml.setGateway(gateway);
    gateway.addMessageListener(cml);

    MinaDisconnectListenerAndInterceptor disconnect = new MinaDisconnectListenerAndInterceptor();
    disconnect.setCloseOnDisconnectMessage(false);
    gateway.addMessageListener(disconnect);
    gateway.addOutgoingMessageInterceptor(disconnect);
    disconnect.setGateway(gateway);

    gateway.setHandler(handler);

    return gateway;
  }
        
Server Configuration (from examples)

While easy to programmatically configure a Mina server gateway, it is expected that a DI framework such as Spring or Guice will be used to perform this task.

The following code demonstrates all that is necessary to configure a STOMP 1.2 compliant server:

  public static AbstractStampyMessageGateway initialize() {
    HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
    gateway.setPort(1234);
    gateway.setHeartbeat(1000);
    gateway.setAutoShutdown(true);

    RawServerMinaHandler handler = new RawServerMinaHandler();
    handler.setGateway(gateway);
    handler.setHeartbeatContainer(heartbeatContainer);

    gateway.addMessageListener(new IDontNeedSecurity()); // DON'T DO THIS!!!
    
    gateway.addMessageListener(new ServerMessageValidationListener());

    gateway.addMessageListener(new VersionListener());

    MinaLoginMessageListener login = new MinaLoginMessageListener();
    login.setGateway(gateway);
    login.setLoginHandler(new SystemLoginHandler());
    gateway.addMessageListener(login);

    MinaConnectStateListener connect = new MinaConnectStateListener();
    connect.setGateway(gateway);
    gateway.addMessageListener(connect);

    MinaHeartbeatListener heartbeat = new MinaHeartbeatListener();
    heartbeat.setHeartbeatContainer(heartbeatContainer);
    heartbeat.setGateway(gateway);
    gateway.addMessageListener(heartbeat);

    MinaTransactionListener transaction = new MinaTransactionListener();
    transaction.setGateway(gateway);
    gateway.addMessageListener(transaction);

    SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

    MinaAcknowledgementListenerAndInterceptor acknowledgement = new MinaAcknowledgementListenerAndInterceptor();
    acknowledgement.setHandler(sys);
    acknowledgement.setGateway(gateway);
    acknowledgement.setAckTimeoutMillis(200);
    gateway.addMessageListener(acknowledgement);
    gateway.addOutgoingMessageInterceptor(acknowledgement);

    MinaReceiptListener receipt = new MinaReceiptListener();
    receipt.setGateway(gateway);
    gateway.addMessageListener(receipt);

    MinaConnectResponseListener connectResponse = new MinaConnectResponseListener();
    connectResponse.setGateway(gateway);
    gateway.addMessageListener(connectResponse);

    gateway.setHandler(handler);

    return gateway;
  }
        
stampy-client-server-$version

This library provides the structure to implement STOMP 1.2 clients and servers using the stampy-core library.

Design considerations

Compiled for Java 5 compatibility
Ease of configuration and use
DI framework-friendly
Minimal dependencies
Reliable
Extensible
Security focused

Dependencies

<dependency org="asia.stampy" name="stampy-core" rev="$version"/>
<dependency org="org.slf4j" name="slf4j-api" rev="1.7.2"/>
<dependency org="commons-lang" name="commons-lang" rev="2.6"/>
<dependency org="commons-codec" name="commons-codec" rev="1.7"/>

Gateways

A Stampy gateway is the interface between an application and the technology used to create/accept connections from STOMP 1.2 compliant clients and servers. All gateways extend from AbstractStampyMessageGateway. This class contains common methods for all Stampy gateways and defines abstract methods which must be implemented in an implementation.

StampyMessageListener

All gateways allow the addition of any number of StampyMessageListeners which are invoked when a STOMP 1.2 message has been received. By themselves the gateway implementations are not STOMP 1.2 compliant. Compliancy is obtained by adding implementations of this listener. The stampy-client-server library contains implementations which enforce compliance. Any additional custom functionality is obtained by creating a new implementation & adding it to the gateway ie: intercepting a specific message type and notifying the application to perform some bit of functionality. Note: the order of addition of these listeners is important.

SecurityMessageListener

At least one implementation of this interface must be included in the list of message listeners, and one must be first in the list else a security exception is thrown. This is to ensure that any custom implementations implement any necessary security for their platform.

Additional security pre-Stampy can be added to the gateway implementations.

Interceptors

All gateways allow the addition of interceptors which are invoked for outgoing messages. There are two types; a message interceptor and a text interceptor. Message interceptors can be tailored to intercept specific types & messages while text interceptors inspect all strings prior to sending them down the pipe. They can be used for outgoing security but are more useful for capturing the state of messages prior to the send. This state can be used to then evaluate any subsequent messages received.

The AbstractDisconnectListenerAndInterceptor and AbstractAcknowledgementListenerAndInterceptor are two classes which make effective use of this functionality.

Heartbeats

A heartbeat container instance is used by the gateway to automatically send heartbeats at the agreed upon rate. Heartbeats are started by the client and server using the AbstractConnectedMessageListener and AbstractHeartbeatListener StampyMessageListeners.

stampy-core-$version

This library provides class representations of STOMP 1.2 messages and the ability to convert to and from the classes. It can be used with the additional Stampy libraries or it can be used with existing STOMP client or server implementations.

Design considerations

Compiled for Java 5 compatibility
Ease of configuration and use
DI framework-friendly
Minimal dependencies
Reliable
Extensible
Dependencies

<dependency org="org.slf4j" name="slf4j-api" rev="1.7.2"/>
<dependency org="commons-lang" name="commons-lang" rev="2.6"/>
<dependency org="commons-codec" name="commons-codec" rev="1.7"/>

Messages

All STOMP message classes implement the StampyMessage interface. Message classes typically have at least two constructors, one blank & the other with parameters - the parameterized constructors are all the mandatory header values that must be set for a valid STOMP message. Should any mandatory header information be missing an exception will be thrown when the message's

validate()
method is called.
Each StampyMessage class has an associated header class which can be obtained by calling the

getHeader()
method. The header classes are wrappers around a map which manages the key value pairs. Known key names have their own getter and setter methods; any key value pair can be added to a Stampy message header.
Note: should a header key already exist in the map any subsequent adds for that key will be ignored. This is to enforce the specification: If a client or a server receives repeated frame header entries, only the first header entry SHOULD be used as the value of header entry. Subsequent values are only used to maintain a history of state changes of the header and MAY be ignored. To replace an existing key the header's

removeHeader(existingKey)
method must be called first.
Parsing

A message parser is included for converting the raw string messages to their associated StampyMessage objects.

Message Bodies

Any STOMP message that has a body can accept a string as the body as well as any Java serializable object. The mime type used by Stampy to represent such serialized objects is

java/base64
Encoding to and from Base64 is handled by the message superclasses & the parser, respectively.
Additional functionality is described in these JavaDocs. This work is released under the GPL 2.0 license. No warranty of any kind is offered. Stampy Copyright (C) 2013 Burton Alexander.