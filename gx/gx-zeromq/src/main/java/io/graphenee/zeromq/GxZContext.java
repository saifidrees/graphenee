package io.graphenee.zeromq;

import java.util.ArrayList;
import java.util.List;

import org.zeromq.ZContext;

public class GxZContext {

	private ZContext context;

	private List<GxZClient> clients = new ArrayList<>();
	private List<GxZServer> servers = new ArrayList<>();

	private GxBrokerConfiguration config;

	public GxZContext() {
		this(new GxBrokerConfiguration());
	}

	public GxZContext(int ioThreads) {
		this(new GxBrokerConfiguration(), ioThreads);
	}

	public GxZContext(GxBrokerConfiguration config) {
		this.setConfig(config);
		setContext(new ZContext());
	}

	public GxZContext(GxBrokerConfiguration config, int ioThreads) {
		this.setConfig(config);
		setContext(new ZContext(ioThreads));
	}

	public GxZClient createClient() {
		return createClient(3, 5000);
	}

	public GxZClient createClient(int retriesLimit, int pollingTimeoutInMillis) {
		GxZClient client = new GxZClient(this, retriesLimit, pollingTimeoutInMillis);
		clients.add(client);
		return client;
	}

	public GxZServer createServer() {
		return createServer(Runtime.getRuntime().availableProcessors());
	}

	public GxZServer createServer(int totalServerCount) {
		GxZServer server = new GxZServer(this, totalServerCount);
		servers.add(server);
		return server;
	}

	public GxZPublisher createPublisher() {
		GxZPublisher publisher = new GxZPublisher(this);
		return publisher;
	}

	public GxZSubscriber createSubscriber() {
		GxZSubscriber subscriber = new GxZSubscriber(this);
		return subscriber;
	}

	public GxBrokerConfiguration getConfig() {
		return config;
	}

	public void setConfig(GxBrokerConfiguration config) {
		this.config = config;
	}

	public ZContext getContext() {
		return context;
	}

	public void setContext(ZContext context) {
		this.context = context;
	}

	public void destroy() {
		clients.forEach(client -> {
			client.destroy();
		});
		servers.forEach(server -> {
			server.destroy();
		});
		context.close();
	}

}
