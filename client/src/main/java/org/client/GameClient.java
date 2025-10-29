package org.client;

import org.client.errorhandling.*;

import org.shared.ShipBoard;
import org.shared.Move;
import org.shared.LOG;

import static org.shared.Utils.printError;
import static org.shared.Utils.promptBoolean;
import static org.shared.Utils.printColoredInformation;
import static org.shared.Utils.printColored;
import static org.shared.Utils.promptInt;
import static org.shared.Utils.Color;

import static org.shared.Constants.BOARD_WIDTH;

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

    private Scanner scanner;

    private String name;

    private final MarkerBoard markerBoard;

    public GameClient(String name) {
        this.name = name;
        this.markerBoard = new MarkerBoard();
    }

    public void start(Scanner scanner) throws ClientException {
        this.scanner = scanner;
        var board = ShipBoard.fromUserInput(scanner);

        connectToServer(board);
        System.out.println("Waiting for server to start the game.");
        gameLoop();
        disconnectFromServer();
    }

    private void gameLoop() throws ClientException {
        System.out.println("The game begins!");

        while (true) {
            var instruction = readLineOrThrow();
            if (instruction == null) {
                printError("Lost connection to server..");
                return;
            }
            switch (instruction) {
                case "UPDATE" ->
                    handleUpdateBoard();
                case "SEND MOVE" -> handleSendMove();
                case "AWAIT MOVE" -> handleAwaitMove();
                case "DEFEAT" -> {
                    handleDefeat();
                    return;
                }
                case "WIN" -> {
                    handleWin();
                    return;
                }
                default -> {
                    printError("Invalid instruction received: '%s'".formatted(instruction));
                    throw ClientException.of(new ClientError.UnknownInstructionError(instruction));
                }
            }
        }
    }

    private void handleDefeat() {
        System.out.println("You have lost..");
    }

    private void handleAwaitMove() {
        printColoredInformation("Enemy's turn.", Color.RED);
    }

    private void handleWin() {
        System.out.println("You have won!!!");
    }

    private void handleSendMove() throws ClientException {
        var move = promptMove();
        writer.println(move.encode());
        MarkerBoard.Marker marker;

        if (receiveMoveResult()) {
            printColored("You hit!!", Color.GREEN);
            marker = MarkerBoard.Marker.Hit;
        } else {
            printColored("You missed..", Color.RED);
            marker = MarkerBoard.Marker.Miss;
        }
        markerBoard.placeMarker(move.x(), move.y(), marker);
    }

    private void handleUpdateBoard() throws ClientException {
        var boardStr = new StringBuilder();
        String line;
        while (!(line = readLineOrThrow()).equals("END"))
            boardStr.append(line + "\n");

        printColoredInformation("Your board:", Color.GREEN);
        System.out.println(boardStr.toString());
    }

    private boolean receiveMoveResult() throws ClientException {
        return readLineOrThrow().equals("HIT");
    }

    private Move promptMove() {
        printColoredInformation("Your turn! Enter a move.", Color.GREEN);
        System.out.println(markerBoard.toString());
        int x, y = -1;

        while (true) {
            x = promptInt(scanner, "Enter column: ") - 1;
            if (0 <= x && x < BOARD_WIDTH) {
                break;
            }
            printError("x is out of bounds");
        }

        while (true) {
            y = promptInt(scanner, "Enter row: ") - 1;
            if (0 <= y && y < BOARD_WIDTH) {
                break;
            }
            printError("y is out of bounds");
        }

        return new Move(x, y);
    }

    private String readLineOrThrow() throws ClientException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw ClientException.of(new ClientError.IOError(e));
        }
    }

    private void disconnectFromServer() throws ClientException {
        try {
            this.close();
        } catch (IOException e) {
            throw ClientException.of(new ClientError.IOError(e));
        }
    }

    private void connectToServer(ShipBoard board) throws ClientException {
        while (true) {
            try {
                LOG.debug(Level.INFO, "Connecting to game host");
                socket = new Socket("localhost", 6969);
                break;
            } catch (IOException e) {
                printError("There was an error while connecting to the server.");
                if (!promptBoolean(scanner, "Do you want to try again?")) {
                    throw new ClientException(new ClientError.ServerConnectError(e));
                }
            }
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
                throw ClientException.of(new ClientError.InvalidHandshakeError());
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
