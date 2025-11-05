package org.server;

import org.shared.Move;

import org.server.errorhandling.ServerError;

public sealed interface Event permits
        Event.PlayerSentMove,
        Event.PlayerConnectionErrored,
        Event.PlayerSentInvalidEvent {

    public record PlayerSentMove(Move m) implements Event {
    }

    public record PlayerConnectionErrored(ServerError error) implements Event {
    }

    public record PlayerSentInvalidEvent(String msg) implements Event {
    }

    public static Event parseEvent(String line) {
        if (line.startsWith("MOVE")) {
            try {
                return new Event.PlayerSentMove(Move.parse(line));
            } catch (IllegalArgumentException e) {
                return new Event.PlayerSentInvalidEvent(e.getMessage());
            }
        }
        return new Event.PlayerSentInvalidEvent(
                "Received unknown event `%s`".formatted(line));
    }

}
