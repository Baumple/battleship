package org.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.logging.Level;

import org.server.errorhandling.Result;
import org.server.errorhandling.ServerError;

import org.shared.LOG;
import org.shared.Move;

public class GameServer implements Closeable, Runnable {
    private ServerSocket socket;

    private Player player1;
    private Player player2;

    private BlockingQueue<Event> eventQueue;

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

    @Override
    public void run() {
        while (true) {
            switch (Result.runCatching(eventQueue::take)) {
                case Result.Error(InterruptedException e) -> {
                    e.printStackTrace();
                    System.exit(1);
                }
                case Result.Ok(Event e) -> handleEvent(e);
                default -> {}
            }
        }
    }

    private void handleEvent(Event e) {
        switch (e) {
            case Event.PlayerSentMove(Move m) -> {
            }
            case Event.PlayerConnectionErrored(ServerError error) -> {
            }
            case Event.PlayerSentInvalidEvent(String msg) -> {
            }
        }
    }

    private Result<Void, ServerError> gameLoop() {
        LOG.debug(Level.INFO, "Starting game.");

        while (true) {
            var res = handleTurn();
            if (res.isError())
                return res;

            if (player1.hasLost())
                return handleGameEnd(player2, player1);
            if (player2.hasLost())
                return handleGameEnd(player1, player2);

        }
    }

    private Result<Void, ServerError> handleTurn() {
        record Pair(Event e1, Event e2) {
        }
        if (player1.pollEvent() != null) {
        }
        if (player2.pollEvent() != null) {
        }
        player2.pollEvent();
        return null;
    }

    private Result<Void, ServerError> handleGameEnd(Player winner, Player loser) {
        return winner.sendWin().then(x -> loser.sendDefeat());
    }

    private Result<Void, ServerError> initialize() {
        try {
            socket = new ServerSocket(6969);
            socket.setSoTimeout(0);
            eventQueue = new LinkedTransferQueue<>();
        } catch (IOException e) {
            return Result.error(new ServerError.InitError(e));
        }
        return Result.ok(null);
    }

    private Result<Void, ServerError> connectPlayers() {
        LOG.debug(Level.INFO, "Waiting for player 1..");
        try {
            var s1 = socket.accept();
            switch (Player.fromSocket(s1, eventQueue)) {
                case Result.Error<Player, ServerError> e:
                    return e.convert();
                case Result.Ok<Player, ServerError> player:
                    this.player1 = player.value();
            }
            LOG.debug(Level.INFO, "Player 1 '%s' connected!".formatted(player1.getName()));

            var s2 = socket.accept();
            switch (Player.fromSocket(s2, eventQueue)) {
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
