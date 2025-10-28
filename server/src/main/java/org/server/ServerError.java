package org.server;

import java.io.IOException;

/**
 * Various errors that may occur during server operation.
 *
 * @see InitError
 * @see IOError
 * @see ClientConnectError
 * @see ClientPropertyError
 * @see InvalidHandshake
 * @see ClientPropertyError
 */
public sealed interface ServerError permits
        ServerError.InitError,
        ServerError.IOError,
        ServerError.ClientConnectError,
        ServerError.ClientPropertyError,
        ServerError.InvalidHandshake,
        ServerError.InvalidMoveReceived {

    /**
     * Error that is returned when the ServerSocket failed to initialize.
     */
    public record InitError(Exception exception)
            implements ServerError {
    }

    /**
     * Error that is returned when a general client/server IOException occurs
     */
    public record IOError(IOException exception)
            implements ServerError {
    }

    /**
     * Error hat is returned when there was an error initializing
     * the client socket and reader/writer
     */
    public record ClientConnectError(IOException exception)
            implements ServerError {
    }

    /**
     * A property was missing/invalid during inital information exchange
     * between server and client.
     */
    public record ClientPropertyError(String msg)
            implements ServerError {
    }

    /**
     * Error hat is returned when the client did not send OK
     */
    public record InvalidHandshake()
            implements ServerError {
    }

    public record InvalidMoveReceived(IllegalArgumentException exception)
            implements ServerError {
    }

}
