package io.graphenee.zeromq;

public class GxBrokerConfiguration {

	private String brokerNetworkAddress;
	private int clientPort;
	private int serverPort;
	private int publisherPort;
	private int subscriberPort;

	public GxBrokerConfiguration() {
		this("0.0.0.0");
	}

	public GxBrokerConfiguration(String brokerNetworkAddress) {
		this(brokerNetworkAddress, 60051, 60052, 60053, 60054);
	}

	public GxBrokerConfiguration(String borkerNetworkAddress, int clientPort, int serverPort, int publisherPort, int subscriberPort) {
		this.brokerNetworkAddress = borkerNetworkAddress;
		this.clientPort = clientPort;
		this.serverPort = serverPort;
		this.publisherPort = publisherPort;
		this.subscriberPort = subscriberPort;
	}

	public String getBrokerNetworkAddress() {
		return brokerNetworkAddress;
	}

	public String getClientAddress() {
		return "tcp://" + brokerNetworkAddress + ":" + clientPort;
	}

	public String getServerAddress() {
		return "tcp://" + brokerNetworkAddress + ":" + serverPort;
	}

	public String getPublisherAddress() {
		return "tcp://" + brokerNetworkAddress + ":" + publisherPort;
	}

	public String getSubscriberAddress() {
		return "tcp://" + brokerNetworkAddress + ":" + subscriberPort;
	}

}
