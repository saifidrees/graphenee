package io.graphenee.zeromq;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import io.graphenee.zeromq.exception.GxZSendException;
import zmq.ZError;

public class GxZClient {

	private static final Logger L = LoggerFactory.getLogger(GxZClient.class);
	private GxZContext ctx;
	private Socket socket;
	private int retriesLimit;
	private long pollingTimeoutInMillis;

	public GxZClient(GxZContext gxZContext, int retriesLimit, long pollingTimeoutInMillis) {
		this.ctx = gxZContext;
		this.retriesLimit = retriesLimit;
		this.pollingTimeoutInMillis = pollingTimeoutInMillis;
	}

	public void sendMessage(final byte[] message, final GxZSuccessCallback success, final GxZErrorCallback error) throws GxZSendException {
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				socket = ctx.getContext().createSocket(ZMQ.REQ);
				socket.setReconnectIVL(5000);
				socket.connect(ctx.getConfig().getClientAddress());

				socket.send(message);
				ZMQ.Poller poller = ctx.getContext().createPoller(32);
				poller.register(socket, ZMQ.Poller.POLLIN);
				int i;
				for (i = 1; i <= retriesLimit; ++i) {
					L.debug("Polling on gx-zeromq proxy");
					int rc = poller.poll(pollingTimeoutInMillis);
					L.debug("Data received " + rc);
					if (poller.pollin(0)) {
						byte[] serverMessage = socket.recv();
						if (serverMessage != null && success != null)
							success.onSuccess(serverMessage);
						break;
					} else {
						// notification send
					}
				}
				poller.unregister(socket);
				poller.close();
				destroy();

				if (i > retriesLimit) {
					error.onError(500, "Unable to send message to server, please make sure the server is online and connected to gx-zeromq proxy.");
				}

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