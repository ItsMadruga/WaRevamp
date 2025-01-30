package its.madruga.warevamp.broadcast;

import its.madruga.warevamp.broadcast.receivers.ModuleReceiver;
import its.madruga.warevamp.broadcast.receivers.WhatsAppReceiver;

public class Receivers {
    public static void registerReceivers() {
        new ModuleReceiver().registerAllReceivers();
        new WhatsAppReceiver().registerAllReceivers();
    }
}
