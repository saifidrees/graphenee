import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.graphenee.zeromq.GxZClient;
import io.graphenee.zeromq.GxZContext;
import io.graphenee.zeromq.GxZErrorCallback;
import io.graphenee.zeromq.GxZMessageListener;
import io.graphenee.zeromq.GxZPublisher;
import io.graphenee.zeromq.GxZRequestProcessor;
import io.graphenee.zeromq.GxZServer;
import io.graphenee.zeromq.GxZSubscriber;
import io.graphenee.zeromq.GxZSuccessCallback;
import io.graphenee.zeromq.exception.GxZRequestProcessingException;
import io.graphenee.zeromq.exception.GxZSendException;

public class ZeromqTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testClientServer() {
		GxZContext context = new GxZContext();
		GxZServer server = context.createServer();
		server.setRequestProcessor(new GxZRequestProcessor() {

			@Override
			public byte[] onRequest(byte[] data) throws GxZRequestProcessingException {
				System.err.println("Request: " + new String(data));
				return "Bye".getBytes();
			}
		});
		server.start();

		GxZClient client = context.createClient();
		try {
			client.sendMessageAsync("Hello World".getBytes(), new GxZSuccessCallback() {

				@Override
				public void onSuccess(byte[] serverMessage) {
					System.err.println("Response: " + new String(serverMessage));
				}
			}, new GxZErrorCallback() {

				@Override
				public void onError(int errorCode, String errorMessage) {
					System.err.println(errorMessage);
				}
			});
		} catch (GxZSendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testPubSub() {
		GxZContext context = new GxZContext();
		GxZPublisher publisher = context.createPublisher();
		GxZSubscriber s1 = context.createSubscriber();
		s1.addMessageListener(new GxZMessageListener() {

			@Override
			public void onMessage(byte[] message) {
				System.err.println("Received: " + new String(message));
			}
		});
		GxZSubscriber s2 = context.createSubscriber();
		s2.addMessageListener(new GxZMessageListener() {

			@Override
			public void onMessage(byte[] message) {
				System.err.println("Received: " + new String(message));
			}
		});
		s1.subscribe("farrukh");
		s2.subscribe("ismail");

		try {
			publisher.publishAsync("farrukh", "This is for Farrukh".getBytes(), new GxZSuccessCallback() {

				@Override
				public void onSuccess(byte[] serverMessage) {
					System.err.println("Published!");
				}
			}, new GxZErrorCallback() {

				@Override
				public void onError(int errorCode, String errorMessage) {
					System.err.println(errorMessage);
				}
			});
		} catch (GxZSendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
