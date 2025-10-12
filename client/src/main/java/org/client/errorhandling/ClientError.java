package org.client.errorhandling;

import java.io.IOException;

public sealed interface ClientError permits
        ClientError.ServerConnectError,
        ClientError.InitError,
        ClientError.InvalidHandshake,
        ClientError.ExchangeError,
        ClientError.UserIOError {

    /*
     * Failed to initialize socket.
     */
    public record InitError(IOException e) implements ClientError {
    }

    /*
     * Failed to connect to the server socket
     */
    public record ServerConnectError(IOException e) implements ClientError {
    }

    /*
     * Received an invalid handshake.
     */
    public record InvalidHandshake() implements ClientError {
    }

    /*
     * Error that may occur when exchanging initial game state.
     */
    public record ExchangeError(IOException e) implements ClientError {
    }

    /*
     * Error while reading user input via a Scanner
     */
    public record UserIOError(Exception e) implements ClientError {
    }

    default ClientException asException() {
        return new ClientException(this);
    }
}
