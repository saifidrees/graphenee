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

	public byte[] sendMessage(final byte[] message) throws GxZSendException {
		try {
			socket = ctx.getContext().createSocket(ZMQ.REQ);
			socket.setReconnectIVL(5000);
			socket.connect(ctx.getConfig().getClientAddress());

			socket.send(message);
			ZMQ.Poller poller = ctx.getContext().createPoller(32);
			poller.register(socket, ZMQ.Poller.POLLIN);
			int i;
			byte[] serverMessage = null;
			for (i = 1; i <= retriesLimit; ++i) {
				poller.poll(pollingTimeoutInMillis);
				if (poller.pollin(0)) {
					serverMessage = socket.recv();
					break;
				} else {
					// notification send
				}
			}
			poller.unregister(socket);
			poller.close();
			destroy();

			if (i > retriesLimit)
				throw new GxZSendException(500,
						"Failed to connect to gx-zeromq-proxy, make sure gx-zeromq-proxy is running with client port set to " + ctx.getConfig().getClientAddress());
			else
				return serverMessage;
		} catch (Exception ex) {
			throw new GxZSendException(-1, ex.getMessage());
		}
	}

	public void sendMessageAsync(final byte[] message, final GxZSuccessCallback success, final GxZErrorCallback error) {
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
					poller.poll(pollingTimeoutInMillis);
					if (poller.pollin(0)) {
						byte[] serverMessage = socket.recv();
						if (serverMessage != null && success != null)
							success.onSuccess(serverMessage);
						break;
					} else {
						if (i == 1) {
							String msg = "Trying to connect to gx-zeromq-proxy frontend on " + ctx.getConfig().getClientAddress() + "...";
							L.warn(msg);
							error.onError(500, msg);
						} else {
							String msg = "Trying to connect to gx-zeromq-proxy frontend on " + ctx.getConfig().getClientAddress() + "... attempt #" + i;
							L.warn(msg);
							error.onError(500, msg);
						}
					}
				}
				poller.unregister(socket);
				poller.close();
				destroy();

				if (i > retriesLimit) {
					error.onError(500, "Failed to connect to gx-zeromq-proxy, make sure gx-zeromq-proxy is running with frontend on " + ctx.getConfig().getClientAddress());
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
		L.debug("Socket destroyed");
	}

}