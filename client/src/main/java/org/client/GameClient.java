package org.client;

import org.client.errorhandling.*;

import org.shared.ShipBoard;
import org.shared.MarkerBoard;
import org.shared.LOG;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Handles connection to the GameServer.
 */
public class GameClient implements Closeable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private String name;

    private final MarkerBoard markerBoard;

    public GameClient(String name, ShipBoard board) {
        this.name = name;
        this.markerBoard = new MarkerBoard();
    }

    public void start(Scanner scanner) throws ClientException {
        try {
            var board = ShipBoard.fromUserInput(scanner);
        } catch (Exception e) {

        }
        connectToServer(board);
        gameLoop();
    }

    private void gameLoop() throws ClientException {
        var instruction = reader.readLine();
        if (instruction.equals("UPDATE")) {
            System.out.println(receiveUpdatedBoard());
        }
    }

    private String readLine() throws ClientException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw ClientException.of(new ClientError.IOError(e));
        }
    }

    public void connectToServer(ShipBoard board) throws ClientException {
        try {
            LOG.debug(Level.INFO, "Connecting to game host");
            socket = new Socket("localhost", 6969);
        } catch (IOException e) {
            throw new ClientException(new ClientError.ServerConnectError(e));
        }
        performHandshake();

        LOG.debug(Level.INFO, "Exchanging game information.");
        writer.println(name);

        writer.println(board.encode());
        writer.println("END");

    }

    private void performHandshake() throws ClientException {
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()),
                    true);

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

    private String receiveUpdatedBoard() throws ClientException {
        try {
            var line = reader.readLine();
            assert line.equals("UPDATE");
            var boardStr = new StringBuilder();
            while (!line.equals("END"))
                boardStr.append(line);

            return boardStr.toString();
        } catch (IOException e) {
            throw ClientException.of(new ClientError.InitError(e));
        }
    }

    @Override
    public void close() throws IOException {
        this.socket.close(); // also closes in/output streams
    }
}
