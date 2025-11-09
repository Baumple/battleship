package org.client;

import org.client.errorhandling.*;

import org.shared.ShipBoard;
import org.shared.Move;
import org.shared.Ship;
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
        // ShipBoard.fromArray(new Ship[] {
        // new Ship(0, 0, 1, Ship.Orientation.Horizontal),
        // new Ship(0, 1, 2, Ship.Orientation.Horizontal),
        // new Ship(0, 2, 3, Ship.Orientation.Horizontal),
        // new Ship(0, 3, 4, Ship.Orientation.Horizontal),
        // new Ship(0, 4, 5, Ship.Orientation.Horizontal),
        // });

        connectToServer(board);
        System.out.println("Waiting for server to start the game.");
        gameLoop();
        disconnectFromServer();
    }

    private void gameLoop() throws ClientException {
        System.out.println("Waiting for server..");

        while (true) {
            var instruction = readLineOrThrow();
            if (instruction == null) {
                printError("Lost connection to server..");
                return;
            }
            switch (instruction) {
                case "SEND MOVE" -> handleSendMove();
                case "DEFEAT" -> {
                    handleDefeat();
                    return;
                }
                case "WIN" -> {
                    handleWin();
                    return;
                }
                case "DRAW" -> {
                    handleDraw();
                    return;
                }
                default -> {
                    printError("Invalid instruction received: '%s'".formatted(instruction));
                    throw ClientException.of(new ClientError.UnknownInstructionError(instruction));
                }
            }
        }
    }

    private void handleDraw() {
        printColored("It waws a draw.", Color.RED);
    }

    private void handleDefeat() {
        printColored("You have lost..", Color.RED);
    }

    private void handleWin() {
        printColored("You have won!!!", Color.GREEN);
        System.out.println("");
    }

    private void handleSendMove() throws ClientException {
        var move = promptMove();
        writer.println(move.encode());
        MarkerBoard.Marker marker;

        System.out.println("Waiting for a server response.");

        if (receiveHasHit()) {
            printColored("You hit!!", Color.GREEN);
            marker = MarkerBoard.Marker.Hit;
        } else {
            printColored("You missed..", Color.RED);
            marker = MarkerBoard.Marker.Miss;
        }

        showBoardUpdate();

        markerBoard.placeMarker(move.x(), move.y(), marker);
    }

    private void showBoardUpdate() throws ClientException {
        if (!readLineOrThrow().equals("UPDATE"))
            System.exit(1);
        var boardStr = new StringBuilder();
        String line;
        while (!(line = readLineOrThrow()).equals("END"))
            boardStr.append(line + "\n");

        printColoredInformation("Updated board:", Color.GREEN);
        System.out.println(boardStr.toString());

        printColoredInformation("Press enter to continue.", Color.GREEN);
        scanner.nextLine();
    }

    private boolean receiveHasHit() throws ClientException {
        var line = readLineOrThrow();
        if (line.equals("HIT"))
            return true;
        else if (line.equals("MISS"))
            return false;

        System.err.println("WTF: " + line);
        System.exit(1);
        return false;
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
        doHandshake();

        LOG.debug(Level.INFO, "Exchanging game information.");
        writer.println(name);

        writer.println(board.encode());
        writer.println("END");

    }

    private void doHandshake() throws ClientException {
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
