package io.graphenee.zeromq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

public class GxZServer {

	private static final Logger L = LoggerFactory.getLogger(GxZServer.class);

	private Socket socket;
	private List<GxZMessageListener> listeners = new ArrayList<>();
	private ExecutorService executorService;

	GxZServer(Socket socket) {
		this.socket = socket;
	}

	public void register(GxZMessageListener listener) {
		listeners.add(listener);
	}

	public void unregister(GxZMessageListener listener) {
		listeners.remove(listener);
	}

	public void start() {
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			System.err.println("GxZServer is listening...");
			while (true) {
				try {
					byte[] message = socket.recv();
					listeners.forEach(listener -> {
						listener.onMessage(message);
					});
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
