import java.util.logging.Level;

class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Run program with player name as argument.");
        }

        LOG.setDebug(true);
        LOG.debug(Level.INFO, "Starting client");

        try (var client = new GameClient(args[0])) {
            client.connect();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private static void handleException(Exception e) {
        e.printStackTrace();
    }
}
