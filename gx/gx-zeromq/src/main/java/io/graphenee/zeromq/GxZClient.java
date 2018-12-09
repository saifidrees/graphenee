package io.graphenee.zeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

import zmq.ZError;

public class GxZClient {

	private static final Logger L = LoggerFactory.getLogger(GxZClient.class);

	private Socket socket;

	GxZClient(Socket socket) {
		this.socket = socket;
	}

	public void sendMessage(byte[] message) {
		sendMessage(message, null, null);
	}

	public void sendMessage(byte[] message, GxZSuccessCallback success) {
		sendMessage(message, success, null);
	}

	public void sendMessage(byte[] message, GxZSuccessCallback success, GxZErrorCallback error) {
		try {
			socket.send(message);
			if (socket.errno() == ZError.EAGAIN) {
				if (error != null)
					error.onError(socket.errno(), ZError.toString(socket.errno()));
			} else {
				byte[] serverMessage = socket.recv();
				if (serverMessage != null && success != null)
					success.onSuccess(serverMessage);
			}
		} catch (Exception ex) {
			if (socket.errno() == ZError.EFSM)
				socket.recv();
			L.warn(ex.getMessage(), ex);
			if (error != null)
				error.onError(socket.errno(), ZError.toString(socket.errno()));
		}
	}

	void destroy() {
		socket.setLinger(0);
		socket.close();
	}

}
