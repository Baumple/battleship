package org.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

import org.shared.LOG;
import org.shared.ShipBoard;

import static org.shared.Constants.NUM_SHIPS;
import static org.shared.Constants.BOARD_WIDTH;

/**
 * Handles connection with the player client
 */
public class Player implements Runnable, Closeable {
    private String name;

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private ShipBoard shipBoard;

    public String getName() {
        return name;
    }

    public Player(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    private Result<Void, ServerError> doHandshake() {
        System.out.println("Performing handshake.");
        try {
            writer.println("OK");

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

        switch (readPlayerInfo()) {
            case Result.Error<Void, ServerError> e -> {
                return e;
            }
            case Result.Ok<Void, ServerError> o -> {
                return o;
            }
        }
    }

    // name
    // ships
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
                    .error(new ServerError.ClientPropertyError(
                            "Invalid ship placement: " + e.getMessage().toString()));
        }

        this.shipBoard.printBoard();

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

    // MOVE x y
    public record Move(int x, int y) {
        public static Move parse(String line) throws IllegalArgumentException {
            var split = line.trim().split(" ");
            int x, y;
            try {
                x = Integer.parseInt(split[1]);
                y = Integer.parseInt(split[2]);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            if (!(0 <= x && x < BOARD_WIDTH)) {
                throw new IllegalArgumentException("x is out of bounds");
            }
            if (!(0 <= x && x < BOARD_WIDTH)) {
                throw new IllegalArgumentException("y is out of bounds");
            }
            return new Move(x, y);
        }

        public String encode() {
            return "MOVE %d %d".formatted(x, y);
        }
    }

    public Result<Move, ServerError> getMove() {
        try {
            writer.println("SEND MOVE");
            return Result.ok(Move.parse(reader.readLine()));
        } catch (IOException e) {
            return Result.error(new ServerError.IOError(e));
        } catch (IllegalArgumentException e) {
            writer.println("ERROR: Received invalid move");
            return Result.error(new ServerError.InvalidMoveReceived(e));
        }
    }

    public Result<Boolean, ServerError> sendUpdate(Move m) {
        var isHit = shipBoard.registerHit(m.x(), m.y());
        writer.println("UPDATE");
        writer.println(shipBoard.toString());
        writer.println("END");
        return Result.ok(isHit);
    }

}
