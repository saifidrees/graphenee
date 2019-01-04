package io.graphenee.zeromq;

import io.graphenee.zeromq.exception.GxZRequestProcessingException;

public interface GxZRequestProcessor {

	byte[] onRequest(byte[] data) throws GxZRequestProcessingException;

}