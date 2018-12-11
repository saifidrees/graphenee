package io.graphenee.zeromq;

import java.util.ArrayList;
import java.util.List;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class GxZContext {

	private ZContext context;

	private List<GxZClient> clients = new ArrayList<>();
	private List<GxZServer> servers = new ArrayList<>();
	private List<GxZPublisher> publishers = new ArrayList<>();
	private List<GxZSubscriber> subscribers = new ArrayList<>();

	private GxBrokerConfiguration config;

	public GxZContext() {
		this(new GxBrokerConfiguration());
	}

	public GxZContext(int ioThreads) {
		this(new GxBrokerConfiguration(), ioThreads);
	}

	public GxZContext(GxBrokerConfiguration config) {
		this.config = config;
		context = new ZContext();
	}

	public GxZContext(GxBrokerConfiguration config, int ioThreads) {
		this.config = config;
		context = new ZContext(ioThreads);
	}

	public GxZClient createClient() {
		return createClient(5000);
	}

	public GxZClient createClient(int reconnectIntervalInMillis) {
		Socket socket = context.createSocket(ZMQ.REQ);
		socket.setReconnectIVL(reconnectIntervalInMillis);
		socket.connect(config.getClientAddress());
		socket.setReqRelaxed(true);
		socket.setReqCorrelate(true);
		GxZClient client = new GxZClient(socket);
		clients.add(client);
		return client;
	}

	public GxZServer createServer() {
		return createServer(5000);
	}

	public GxZServer createServer(int reconnectIntervalInMillis) {
		Socket socket = context.createSocket(ZMQ.REP);
		socket.setReconnectIVL(reconnectIntervalInMillis);
		socket.connect(config.getServerAddress());
		GxZServer server = new GxZServer(socket);
		servers.add(server);
		return server;
	}

	public GxZPublisher createPublisher() {
		return createPublisher(5000);
	}

	public GxZPublisher createPublisher(int reconnectIntervalInMillis) {
		Socket socket = context.createSocket(ZMQ.PUSH);
		socket.setReconnectIVL(reconnectIntervalInMillis);
		socket.connect(config.getPublisherAddress());
		GxZPublisher publisher = new GxZPublisher(socket);
		publishers.add(publisher);
		return publisher;
	}

	public GxZSubscriber createSubscriber() {
		return createSubscriber(5000);
	}

	public GxZSubscriber createSubscriber(int reconnectIntervalInMillis) {
		Socket socket = context.createSocket(ZMQ.SUB);
		socket.setReconnectIVL(reconnectIntervalInMillis);
		socket.connect(config.getSubscriberAddress());
		GxZSubscriber subscriber = new GxZSubscriber(socket);
		subscribers.add(subscriber);
		return subscriber;
	}

	public void destroy() {
		subscribers.forEach(subscriber -> {
			subscriber.destroy();
		});
		publishers.forEach(publisher -> {
			publisher.destroy();
		});
		clients.forEach(client -> {
			client.destroy();
		});
		servers.forEach(server -> {
			server.destroy();
		});
		context.close();
	}

}
