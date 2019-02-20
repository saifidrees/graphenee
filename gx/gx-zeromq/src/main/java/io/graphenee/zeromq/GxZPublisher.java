package io.graphenee.zeromq;

import java.util.Base64;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import io.graphenee.zeromq.exception.GxZSendException;
import zmq.ZError;

public class GxZPublisher {

	private static final Logger L = LoggerFactory.getLogger(GxZPublisher.class);
	private GxZContext ctx;
	private Socket socket;

	public GxZPublisher(GxZContext gxZContext) {
		this.ctx = gxZContext;
	}

	public void publish(final String topic, final byte[] message) throws GxZSendException {
		try {
			socket = ctx.getContext().createSocket(ZMQ.PUSH);
			socket.setReconnectIVL(5000);
			socket.connect(ctx.getConfig().getPublisherAddress());
			String encodedMessage = Base64.getEncoder().encodeToString(message);
			socket.send(topic + "|" + encodedMessage);
		} catch (Exception ex) {
			throw new GxZSendException(-1, ex.getMessage());
		}
	}

	public void publishAsync(final String topic, final byte[] message, final GxZSuccessCallback success, final GxZErrorCallback error) {
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				socket = ctx.getContext().createSocket(ZMQ.PUSH);
				socket.setReconnectIVL(5000);
				socket.connect(ctx.getConfig().getPublisherAddress());
				String encodedMessage = Base64.getEncoder().encodeToString(message);
				socket.send(topic + "|" + encodedMessage);
				success.onSuccess("".getBytes());
			} catch (Exception ex) {
				L.warn(ex.getMessage(), ex);
				if (error != null)
					error.onError(socket.errno(), ZError.toString(socket.errno()));
			}
		});
	}

	void destroy() {
		socket.setLinger(0);
		socket.close();
		L.debug("Socket destroyed");
	}

}
