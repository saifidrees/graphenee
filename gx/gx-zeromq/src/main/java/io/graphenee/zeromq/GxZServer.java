package io.graphenee.zeromq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class GxZServer {

	private static final Logger L = LoggerFactory.getLogger(GxZServer.class);

	private GxZRequestProcessor processor;
	private List<ExecutorService> executorServices = new ArrayList<>();

	private GxZContext ctx;
	private int totalServerCount;
	private List<Socket> sockets = new ArrayList<>();

	public GxZServer(GxZContext gxZContext, int totalServerCount) {
		this.ctx = gxZContext;
		this.totalServerCount = totalServerCount;
	}

	public void setRequestProcessor(GxZRequestProcessor processor) {
		this.processor = processor;
	}

	public void start() {
		for (int i = 0; i < totalServerCount; ++i) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			final Integer count = i + 1;
			executorService.execute(() -> {
				Socket socket = ctx.getContext().createSocket(ZMQ.REP);
				sockets.add(socket);
				socket.setReconnectIVL(5000);
				socket.connect(ctx.getConfig().getServerAddress());
				L.warn("Server Thread-" + count + " listening on gx-zeromq-proxy backend on " + ctx.getConfig().getServerAddress());
				while (!Thread.currentThread().isInterrupted()) {
					try {
						byte[] data = socket.recv();
						if (processor != null && data != null) {
							byte[] response = processor.onRequest(data);
							socket.send(response);
						} else {
							socket.send("");
						}
					} catch (Exception ex) {
						L.warn(ex.getMessage(), ex);
					}
				}
			});
			executorServices.add(executorService);
		}
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
