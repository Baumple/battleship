package org.server;

import org.shared.Move;

import org.server.errorhandling.Result;
import org.server.errorhandling.ServerError;

public sealed interface Event permits
        Event.PlayerSentMove {

    public record PlayerSentMove(Player p, Move m) implements Event {
    }

    public static Result<Event, ServerError> parseEvent(Player p, String line) {
        try {
            return Result.ok(new Event.PlayerSentMove(p, Move.parse(line)));
        } catch (IllegalArgumentException e) {
            return Result.error(new ServerError.InvalidMoveReceived(e, p));
        }
    }

}
