package org.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;

import org.shared.LOG;

public class GameServer implements Closeable {
    private ServerSocket socket;

    private Player player1;
    private Player player2;

    public Result<Void, ServerError> start() {
        var res = connectPlayers();
        if (res.isError())
            return res;

        startGame();
        return Result.ok(null);
    }

    private Result<Void, ServerError> startGame() {
        throw new UnsupportedOperationException("startGame not implemented.");
        // return Result.ok(null);
    }

    private Result<Void, ServerError> connectPlayers() {
        try {
            socket = new ServerSocket(6969);
            socket.setSoTimeout(100_000);
        } catch (IOException e) {
            return Result.error(new ServerError.InitError(e));
        }

        LOG.debug(Level.INFO, "Waiting for player 1..");
        try {
            var s1 = socket.accept();
            switch (Player.fromSocket(s1)) {
                case Result.Error<Player, ServerError> e:
                    return e.convert();
                case Result.Ok<Player, ServerError> player:
                    this.player1 = player.value();
            }
            LOG.debug(Level.INFO, "Player 1 '%s' connected!".formatted(player1.getName()));

            var s2 = socket.accept();
            switch (Player.fromSocket(s2)) {
                case Result.Error<Player, ServerError> e:
                    return e.convert();
                case Result.Ok<Player, ServerError> player:
                    this.player2 = player.value();
            }
            LOG.debug(Level.INFO, "Player 2 '%s' connected!".formatted(player2.getName()));

        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        }

        return Result.ok(null); // ok
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
