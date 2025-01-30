package its.madruga.warevamp.broadcast;

import its.madruga.warevamp.broadcast.senders.ModuleSender;
import its.madruga.warevamp.broadcast.senders.WhatsAppSender;

public class Senders {
    public static void initialize() {
        new WhatsAppSender();
        new ModuleSender();
    }
}
