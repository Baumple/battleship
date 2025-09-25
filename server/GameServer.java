import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

public class GameServer implements Closeable {
    private ServerSocket socket;

    private Player player1;
    private Player player2;

    public Result<Void, ServerError> start() {
        try {
            socket = new ServerSocket(6969);
            socket.setSoTimeout(10000);
        } catch (IOException e) {
            return new Result.Error<>(new ServerError.InitError(e));
        }

        System.out.println("Waiting for player 1..");
        try {
            var s1 = socket.accept();
            System.out.println("Connected to socket");
            switch (Player.fromSocket(s1)) {
                case Result.Error<Player, ServerError> e:
                    return e.convert();
                case Result.Ok<Player, ServerError> player:
                    this.player1 = player.value();
            }
            System.out.println("Player 1 connected!");

            var s2 = socket.accept();
            switch (Player.fromSocket(s2)) {
                case Result.Error<Player, ServerError> e:
                    return e.convert();
                case Result.Ok<Player, ServerError> player:
                    this.player2 = player.value();
            }
            System.out.println("Player 2 connected!");

        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        }

        return new Result.Ok<>(null); // ok
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
