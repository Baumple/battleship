package org.client;

import org.client.errorhandling.*;

import org.shared.Ship;
import org.shared.ShipBoard;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Handles connection to the GameServer.
 */
public class GameClient implements Closeable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private String name;

    // private Board board;

    public GameClient(String name) {
        this.name = name;
        // this.board = new Board(Board.DEFAULT_BOARD_SIZE);
    }

    public void constructBoard() {
        ShipBoard board = new ShipBoard(ShipBoard.DEFAULT_BOARD_SIZE, new Ship[] {
                new Ship(0, 0, 5, Ship.Orientation.Horizontal),
                new Ship(0, 1, 4, Ship.Orientation.Horizontal),
                new Ship(0, 2, 3, Ship.Orientation.Horizontal),
                new Ship(0, 3, 2, Ship.Orientation.Horizontal),
                new Ship(0, 4, 1, Ship.Orientation.Horizontal),
        });
        board.printBoard();
    }

    public void connectToServer() throws ClientException {
        try {
            LOG.debug(Level.INFO, "Connecting to game host");
            socket = new Socket("localhost", 6969);
        } catch (IOException e) {
            throw new ClientException(new ClientError.ServerConnectError(e));
        }
        performHandshake();

        LOG.debug(Level.INFO, "Exchanging game information.");
        writer.println(name);
    }

    private void performHandshake() throws ClientException {
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            LOG.debug(Level.INFO, "Performing Handshake");
            var msg = reader.readLine();
            if (!msg.equals("OK")) {
                throw ClientException.of(new ClientError.InvalidHandshake());
            }
            writer.println("OK");

            LOG.debug(Level.INFO, "Handshake successful.");
            writer.flush();

        } catch (IOException e) {
            throw ClientException.of(new ClientError.ServerConnectError(e));
        }
    }

    @Override
    public void close() throws IOException {
        this.socket.close(); // also closes in/output streams
    }
}
