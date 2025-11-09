package org.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import org.server.errorhandling.Result;
import org.server.errorhandling.ServerError;

import org.shared.LOG;
import org.shared.ShipBoard;
import org.shared.Move;

/**
 * Handles connection with the player client
 */
public class Player implements Closeable, Runnable {
    public static Result<Player, ServerError> fromSocket(
            Socket socket,
            BlockingQueue<Result<Event, ServerError>> eventQueue) {
        try {
            var player = new Player(socket, eventQueue);
            System.out.println("Created player object.");
            return player.connect()
                    .mapOk(x -> player);
        } catch (IOException e) {
            return Result.error(new ServerError.PlayerConnectError(e));
        }
    }

    private String name;
    private final Socket socket;
    private final BufferedReader reader;

    private final PrintWriter writer;

    private ShipBoard shipBoard;

    private BlockingQueue<Result<Event, ServerError>> eventQueue;
    /**
     * A volatile boolean indicating whether the reading thread of the Player object
     * should continue reading.
     *
     * Calling {@link Player.close} will set running to false and close the Socket
     * and
     * its i/o streams, thus shutting down the reading thread.
     */
    private volatile boolean running = false;

    private Player(
            Socket socket,
            BlockingQueue<Result<Event, ServerError>> eventQueue) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        this.eventQueue = eventQueue;
    }

    public String getName() {
        return name;
    }

    public Result<Void, ServerError> connect() {
        var res = doHandshake();

        if (res.isError())
            return res;
        System.out.println("Handshake ok");

        var playerInfoRes = readPlayerInfo();

        // leave reading to the reader thread now.
        new Thread(this).start();

        return switch (playerInfoRes) {
            case Result.Error<Void, ServerError> e -> e;
            case Result.Ok<Void, ServerError> o -> o;
        };
    }

    public void sendAwaitingMove() {
        writer.println("SEND MOVE");
    }

    public boolean hasLost() {
        return !shipBoard.hasAliveShips();
    }

    public boolean registerHit(Move m) {
        var isHit = shipBoard.registerHit(m.x(), m.y());
        return isHit;
    }

    public void sendTurnResult(boolean isHit) {
        if (isHit) {
            writer.println("HIT");
        } else {
            writer.println("MISS");
        }
        writer.println("UPDATE");
        writer.println(shipBoard.toString());
        writer.println("END");
    }

    public void sendDefeat() {
        writer.println("DEFEAT");
    }

    public void sendWin() {
        writer.println("WIN");
    }

    public void sendDraw() {
        writer.println("DRAW");
    }

    /**
     * Reads messages and puts the in the {@link LinkedTransferQueue} of the
     * GameServer. The GameServer then waits for an event in that
     * LinkedTransferQueue and handles it as it comes in.
     */
    @Override
    public void run() {
        running = true;
        LOG.debug(Level.INFO, "Starting player thread.");
        while (running) {
            var line = readLine();
            switch (line) {
                case Result.Ok(String value) -> {
                    if (value != null)
                        eventQueue.offer(Event.parseEvent(this, value));
                }
                case Result.Error(ServerError error) ->
                    eventQueue.offer(Result.error(error));
            }
        }
        LOG.debug(Level.INFO, "Shutting down player thread.");
    }

    @Override
    public synchronized void close() throws IOException {
        this.running = false;
        this.socket.close();
    }

    @Override
    public String toString() {
        return "Player { name: %s }".formatted(this.name);
    }

    private Result<Void, ServerError> doHandshake() {
        LOG.debug(Level.INFO, "Performing handshake.");
        try {
            writer.println("OK");

            LOG.debug(Level.INFO, "Sent OK. Waiting for OK.");

            if (!reader.readLine().equals("OK")) {
                return Result.error(new ServerError.InvalidHandshake());
            }
            LOG.debug(Level.INFO, "Received OK. Handshake done.");

        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        }
        return Result.ok(null);
    }

    private Result<Void, ServerError> readPlayerInfo() {
        try {
            LOG.debug(Level.INFO, "Exchanging game information.");
            this.name = reader.readLine();

            var shipBlock = new StringBuilder();
            String line = reader.readLine();
            while (!line.equals("END")) {
                shipBlock.append(line);
                line = reader.readLine();
            }

            LOG.debug(Level.INFO, "Received ship placement");
            this.shipBoard = ShipBoard.decode(shipBlock.toString());

        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        } catch (IllegalArgumentException e) {
            return Result
                    .error(new ServerError.PlayerPropertyError(
                            "Invalid ship placement: " + e.getMessage().toString()));
        }

        return Result.ok(null);
    }

    /**
     * Reads from Players {@link BufferedReader} and wraps the returned linke or
     * thrown Exception in a Result object
     */
    private synchronized Result<String, ServerError> readLine() {
        return Result.runCatching(this.reader::readLine)
                .mapError(ex -> new ServerError.IOError((IOException) ex));
    }

}
