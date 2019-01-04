package io.graphenee.zeromq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

public class GxZSubscriber {

	private static final Logger L = LoggerFactory.getLogger(GxZSubscriber.class);

	private Socket socket;
	private List<GxZMessageListener> listeners = new ArrayList<>();
	private ExecutorService executorService;

	GxZSubscriber(Socket socket) {
		this.socket = socket;
	}

	public void register(GxZMessageListener listener) {
		listeners.add(listener);
	}

	public void unregister(GxZMessageListener listener) {
		listeners.remove(listener);
	}

	public void subscribe() {
		subscribe("");
	}

	public void subscribe(String topic) {
		socket.subscribe(topic);
	}

	public void unsubscribe() {
		unsubscribe("");
	}

	public void unsubscribe(String topic) {
		socket.unsubscribe(topic);
	}

	public void start() {
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			System.err.println("GxZSubscriber is listening...");
			while (true) {
				try {
					byte[] message = socket.recv();
					if (message != null) {
						listeners.forEach(listener -> {
							listener.onMessage(message);
						});
					}
				} catch (Exception ex) {
					L.warn(ex.getMessage(), ex);
				}
			}
		});
	}

	void destroy() {
		if (executorService != null)
			executorService.shutdownNow();
		listeners.clear();
		socket.setLinger(0);
		socket.close();
	}

}
