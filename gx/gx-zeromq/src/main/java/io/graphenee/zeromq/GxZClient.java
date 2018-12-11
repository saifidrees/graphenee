package io.graphenee.zeromq;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

import io.graphenee.zeromq.exception.GxZSendException;
import zmq.ZError;

public class GxZClient {

	private static final Logger L = LoggerFactory.getLogger(GxZClient.class);

	private Socket socket;

	GxZClient(Socket socket) {
		this.socket = socket;
	}

	public byte[] sendMessage(final byte[] message) throws GxZSendException {
		try {
			socket.send(message);
			if (socket.errno() == ZError.EAGAIN) {
				throw new GxZSendException(socket.errno(), ZError.toString(socket.errno()));
			} else {
				byte[] serverMessage = socket.recv();
				return serverMessage;
			}
		} catch (Exception ex) {
			if (socket.errno() == ZError.EFSM)
				socket.recv();
			L.warn(ex.getMessage(), ex);
			throw new GxZSendException(socket.errno(), ZError.toString(socket.errno()));
		}
	}

	public void sendMessageAsync(final byte[] message, final GxZSuccessCallback success) {
		sendMessageAsync(message, success, null);
	}

	public void sendMessageAsync(final byte[] message, final GxZSuccessCallback success, final GxZErrorCallback error) {
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				socket.send(message);
				//				if (socket.errno() == ZError.EAGAIN) {
				//					if (error != null)
				//						error.onError(socket.errno(), ZError.toString(socket.errno()));
				//				} else {
				byte[] serverMessage = socket.recv();
				if (serverMessage != null && success != null)
					success.onSuccess(serverMessage);
				//				}
			} catch (Exception ex) {
				if (socket.errno() == ZError.EFSM)
					socket.recv();
				L.warn(ex.getMessage(), ex);
				if (error != null)
					error.onError(socket.errno(), ZError.toString(socket.errno()));
			}
		});
	}

	void destroy() {
		socket.setLinger(0);
		socket.close();
	}

}
