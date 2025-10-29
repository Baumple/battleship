package org.client.errorhandling;

import java.io.IOException;

public sealed interface ClientError permits
        ClientError.ServerConnectError,
        ClientError.InitError,
        ClientError.InvalidHandshakeError,
        ClientError.UserIOError,
        ClientError.IOError,
        ClientError.UnknownInstructionError,
        ClientError.LostConnectionError {

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
    public record InvalidHandshakeError() implements ClientError {
    }

    public record UnknownInstructionError(String instruction)
            implements ClientError {
    }

    /*
     * Error while reading user input via a Scanner
     */
    public record UserIOError(Exception e) implements ClientError {
    }

    public record IOError(IOException e) implements ClientError {
    }

    default ClientException asException() {
        return new ClientException(this);
    }

    public record LostConnectionError() implements ClientError {
    }
}
