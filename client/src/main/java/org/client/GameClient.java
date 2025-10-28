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
import java.util.logging.Level;

/**
 * Handles connection to the GameServer.
 */
public class GameClient implements Closeable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private String name;

    private final ShipBoard shipBoard;
    private final MarkerBoard markerBoard;

    public GameClient(String name, ShipBoard board) {
        this.name = name;
        this.shipBoard = board;
        this.markerBoard = new MarkerBoard();
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

        writer.println(shipBoard.encode());
        writer.println("END");
        writer.flush();

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
