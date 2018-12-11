package io.graphenee.zeromq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import zmq.ZError;

public class GxZServer {

	private static final Logger L = LoggerFactory.getLogger(GxZServer.class);

	private Socket socket;
	private GxZRequestProcessor processor;
	private ExecutorService executorService;

	GxZServer(Socket socket) {
		this.socket = socket;
	}

	public void setRequestProcessor(GxZRequestProcessor processor) {
		this.processor = processor;
	}

	public void start() {
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			System.err.println("GxZServer is listening...");
			while (true) {
				try {
					byte[] data = socket.recv();
					if (socket.errno() == ZError.EAGAIN)
						continue;
					if (processor != null) {
						byte[] response = processor.onRequest(data);
						socket.send(response, ZMQ.DONTWAIT);
					} else {
						socket.send("");
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
		socket.setLinger(0);
		socket.close();
	}

}
