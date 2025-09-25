package org.example.errorhandling;

import java.io.IOException;

public sealed interface ClientError permits
        ClientError.ServerConnectError,
        ClientError.InitError,
        ClientError.InvalidHandshake,
        ClientError.ExchangeError {
    public record ServerConnectError(IOException e) implements ClientError {
    }

    public record InitError(IOException e) implements ClientError {
    }

    public record InvalidHandshake() implements ClientError {
    }

    public record ExchangeError(IOException e) implements ClientError {
    }

    default ClientException asException() {
        return new ClientException(this);
    }
}
