package org.example;

import org.example.errorhandling.*;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

public class GameClient implements Closeable {
    private final String name;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public GameClient(String name) {
        this.name = name;
    }

    public void start() throws ClientException {
        connect();
    }

    private void connect() throws ClientException {
        try {
            LOG.debug(Level.INFO, "Connecting to game host");
            socket = new Socket("localhost", 6969);
        } catch (IOException e) {
            throw new ClientException(new ClientError.ServerConnectError(e));
        }
        doHandshake();

        try {
            // Send information
            LOG.debug(Level.INFO, "Exchanging properties.");
            var props = new java.util.Properties();
            props.setProperty("player-name", name);
            props.store(writer, null);

        } catch (IOException e) {
            throw ClientException.of(new ClientError.ExchangeError(e));
        }
    }

    private void doHandshake() throws ClientException {
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
            writer.println(name);
            writer.flush();

        } catch (IOException e) {
            throw ClientException.of(new ClientError.ServerConnectError(e));
        }
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
        this.writer.close();
        this.socket.close();
    }
}
