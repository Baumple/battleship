package org.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;

import org.server.errorhandling.Result;
import org.server.errorhandling.ServerError;

import org.shared.LOG;

public class GameServer implements Closeable {
    private ServerSocket socket;

    private Player player1;
    private Player player2;

    public Result<Void, ServerError> start() {
        var initRes = initialize();
        if (initRes.isError())
            return initRes;

        while (true) {
            var res = connectPlayers();
            if (res.isError())
                return res;

            res = gameLoop();
            if (res.isError())
                return res;

            res = disconnectPlayers();
            if (res.isError())
                return res;
        }
    }

    private Result<Void, ServerError> disconnectPlayers() {
        try {
            player1.close();
            player2.close();
        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        }
        return Result.ok(null);
    }

    private Result<Void, ServerError> gameLoop() {
        LOG.debug(Level.INFO, "Starting game.");

        while (true) {
            var res = handleAttack(player1, player2);
            if (res.isError())
                return res;

            if (player2.hasLost())
                return handleGameEnd(player1, player2);

            res = handleAttack(player2, player1);
            if (res.isError())
                return res;

            if (player1.hasLost())
                return handleGameEnd(player2, player1);
        }
    }

    private Result<Void, ServerError> handleAttack(Player attacker, Player defender) {
        defender.sendAwaitMove();
        return attacker.getMove()
                .then(defender::sendBoardUpdate)
                .mapOk(isHit -> {
                    attacker.sendResponse(isHit.booleanValue());
                    return null;
                });

    }

    private Result<Void, ServerError> handleGameEnd(Player winner, Player loser) {
        return winner.sendWin().then(x -> loser.sendDefeat());
    }

    private Result<Void, ServerError> initialize() {
        try {
            socket = new ServerSocket(6969);
            socket.setSoTimeout(0);
        } catch (IOException e) {
            return Result.error(new ServerError.InitError(e));
        }
        return Result.ok(null);
    }


    private Result<Void, ServerError> connectPlayers() {
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
