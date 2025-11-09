package org.server.errorhandling;

import java.io.IOException;

import org.server.Player;

/**
 * Various errors that may occur during server operation.
 *
 * @see InitError
 * @see IOError
 * @see PlayerConnectError
 * @see PlayerPropertyError
 * @see InvalidHandshake
 * @see PlayerPropertyError
 */
public sealed interface ServerError permits
        ServerError.InitError,
        ServerError.IOError,
        ServerError.PlayerConnectError,
        ServerError.PlayerPropertyError,
        ServerError.InvalidHandshake,
        ServerError.InvalidMoveReceived,
        ServerError.Interrupted {

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
    public record PlayerConnectError(IOException exception)
            implements ServerError {
    }

    /**
     * A property was missing/invalid during inital information exchange
     * between server and client.
     */
    public record PlayerPropertyError(String msg)
            implements ServerError {
    }

    /**
     * Error hat is returned when the client did not send OK
     */
    public record InvalidHandshake()
            implements ServerError {
    }

    public record InvalidMoveReceived(IllegalArgumentException exception, Player from)
            implements ServerError {
    }

    public record Interrupted(InterruptedException e) implements ServerError {
    }

}
