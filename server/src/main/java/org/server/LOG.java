package org.server;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LOG {
    private static final Logger LOG = Logger.getLogger("gameclient");
    private static boolean debugEnabled = false;

    public static void setDebug(boolean enable) {
        debugEnabled = enable;
    }

    public static void debug(Level level, String msg) {
        if(debugEnabled) {
            LOG.log(level, msg, "");
        }
    }
}
