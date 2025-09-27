package org.client.errorhandling;

public class ClientException extends Exception {
    public final ClientError error;

    public ClientException(ClientError error) {
        this.error = error;
    }

    public static ClientException of(ClientError e) {
        return new ClientException(e);
    }
}
