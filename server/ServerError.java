import java.io.IOException;

public sealed interface ServerError permits
        ServerError.InitError,
        ServerError.IOError,
        ServerError.ClientConnectError,
        ServerError.InvalidHandshake {

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
     * Error hat is returned when the client did not send OK
     */
    public record InvalidHandshake()
            implements ServerError {
    }

}
