package io.graphenee.zeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

public class GxZPublisher {

	private static final Logger L = LoggerFactory.getLogger(GxZPublisher.class);

	private Socket socket;

	GxZPublisher(Socket socket) {
		this.socket = socket;
	}

	public void publish(byte[] message) {
		socket.send(message);
	}

	public void publish(String message) {
		try {
			if (message != null)
				socket.send(message.getBytes());
		} catch (Exception ex) {
			L.warn(ex.getMessage(), ex);
		}
	}

	void destroy() {
		socket.setLinger(0);
		socket.close();
	}

}
