import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Player implements Runnable, Closeable {
    private String name;

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public Player(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private Result<Void, ServerError> doHandshake() {
        System.out.println("Performing handshake.");
        try {
            writer.println("OK");
            writer.flush();

            System.out.println("Sent OK. Waiting for OK.");

            if (!reader.readLine().equals("OK")) {
                return Result.error(new ServerError.InvalidHandshake());
            }
            System.out.println("Received OK. Handshake done.");

        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        }
        return Result.ok(null);
    }

    public Result<Void, ServerError> connect() {
        var res = doHandshake();
        if (res.isError())
            return res;
        System.out.println("Handshake ok");

        try {
            var msg = reader.readLine();
            this.name = msg;
        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        }

        return Result.ok(null);
    }

    public static Result<Player, ServerError> fromSocket(Socket socket) {
        try {
            var player = new Player(socket);
            System.out.println("Created player object.");
            return player.connect()
                    .mapOk(x -> player);

        } catch (IOException e) {
            return Result.error(new ServerError.ClientConnectError(e));
        }
    }

    @Override
    public void run() {
        doHandshake();
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    @Override
    public String toString() {
        return "Player { name: %s }".formatted(this.name);
    }

}
