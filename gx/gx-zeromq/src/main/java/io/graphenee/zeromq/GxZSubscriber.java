package io.graphenee.zeromq;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class GxZSubscriber {

	private static final Logger L = LoggerFactory.getLogger(GxZSubscriber.class);

	private Set<GxZMessageListener> listeners = new HashSet<>();
	private List<ExecutorService> executorServices = new ArrayList<>();

	private GxZContext ctx;
	private List<Socket> sockets = new ArrayList<>();

	public GxZSubscriber(GxZContext gxZContext) {
		this.ctx = gxZContext;
	}

	public void addMessageListener(GxZMessageListener listener) {
		listeners.add(listener);
	}

	public void removeMessageListener(GxZMessageListener listener) {
		listeners.remove(listener);
	}

	public void subscribe(String topic) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			L.info("Subscriber is connecting to gx-zeromq proxy...");
			Socket socket = ctx.getContext().createSocket(ZMQ.SUB);
			sockets.add(socket);
			socket.setReconnectIVL(5000);
			socket.connect(ctx.getConfig().getSubscriberAddress());
			socket.subscribe(topic.getBytes());
			L.info("Subscribed to " + topic + ", waiting...");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					String data = socket.recvStr();
					if (data != null) {
						String[] parts = data.split("\\|");
						if (parts.length == 2) {
							byte[] message = Base64.getDecoder().decode(parts[1]);
							L.debug("Data received, now processing...");
							listeners.forEach(listener -> {
								listener.onMessage(message);
							});
						}
					}
				} catch (Exception ex) {
					L.warn(ex.getMessage(), ex);
				}
			}
		});
		executorServices.add(executorService);
	}

	void destroy() {
		executorServices.forEach(executorService -> {
			executorService.shutdownNow();
		});
		sockets.forEach(socket -> {
			socket.setLinger(0);
			socket.close();
		});
	}

}
