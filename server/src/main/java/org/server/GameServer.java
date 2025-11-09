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

public class GameServer implements Closeable {
    private ServerSocket socket;

    private Player player1;
    private Player player2;

    private BlockingQueue<Result<Event, ServerError>> eventQueue;

    public void start() {
        var initRes = initialize();
        initRes.ifError(this::handleError);

        while (true) {
            connectPlayers()
                    .ifError(this::handleError);
            gameLoop()
                    .ifError(this::handleError);
            disconnectPlayers()
                    .ifError(this::handleError);
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

    // TODO: send appropriate responses to clients
    private void handleError(ServerError error) {
        switch (error) {
            case ServerError.InitError ie ->
                System.out.println("Could not initialize server socket: "
                        + ie.exception().getMessage());

            case ServerError.InvalidHandshake h ->
                System.out.println("Received an invalid handshake");

            case ServerError.PlayerConnectError e ->
                System.out.println("Could not connect to client socket: "
                        + e.exception().getMessage());

            case ServerError.PlayerPropertyError e ->
                System.out.println("Invalid properties received from Client" + e.msg());

            case ServerError.IOError e ->
                System.out.println("Communication error: "
                        + e.exception().getMessage());

            case ServerError.InvalidMoveReceived i ->
                System.out.println("Received an invalid move from a client: "
                        + i.exception().getMessage());

            case ServerError.Interrupted e ->
                System.out.println("A thread was interrupted: " + e.e().getMessage());
        }
        System.exit(1);
    }

    private Result<Event, ServerError> awaitEvent() {
        LOG.debug(Level.INFO, "Awaiting events..");
        try {
            return eventQueue.take();
        } catch (InterruptedException e) {
            return Result.error(new ServerError.Interrupted(e));
        }
    }

    private void handleTurn() {
        Move player1Move = null;
        Move player2Move = null;

        player1.sendAwaitingMove();
        player2.sendAwaitingMove();

        while (player1Move == null || player2Move == null) {
            switch (awaitEvent()) {
                case Result.Ok(Event.PlayerSentMove(Player p, Move m)) -> {
                    if (player1Move == null && p == player1) {
                        player1Move = m;
                    } else if (player2Move == null && p == player2) {
                        player2Move = m;
                    }
                }
                case Result.Error(ServerError error) ->
                    handleError(error);
            }
        }

        var hasPlayer1Hit = player2.registerHit(player1Move);
        var hasPlayer2Hit = player1.registerHit(player2Move);
        player1.sendTurnResult(hasPlayer1Hit);
        player2.sendTurnResult(hasPlayer2Hit);
    }

    private Result<Void, ServerError> gameLoop() {
        LOG.debug(Level.INFO, "Starting game.");
        while (true) {
            handleTurn();
            if (player1.hasLost() && player2.hasLost())
                return handleGameEndDraw();
            if (player1.hasLost())
                return handleGameEnd(player2, player1);
            if (player2.hasLost())
                return handleGameEnd(player1, player2);
        }
    }

    private Result<Void, ServerError> handleGameEnd(Player winner, Player loser) {
        return winner.sendWin().then(x -> loser.sendDefeat());
    }
    private Result<Void, ServerError> handleGameDraw() {
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
