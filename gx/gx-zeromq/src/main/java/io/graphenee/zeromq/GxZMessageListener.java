package io.graphenee.zeromq;

public interface GxZMessageListener {

	void onMessage(byte[] message);

}