import java.io.IOException;

public sealed interface ClientError
        permits ClientError.ConnectException,
        ClientError.InitException,
        ClientError.InvalidHandshake {
    public record ConnectException(IOException e) implements ClientError {
    }

    public record InitException(IOException e) implements ClientError {
    }

    public record InvalidHandshake() implements ClientError {
    }

    default ClientException asException() {
        return new ClientException(this);
    }
}
