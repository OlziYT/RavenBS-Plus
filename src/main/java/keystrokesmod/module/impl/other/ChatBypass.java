package keystrokesmod.module.impl.other;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class ChatBypass extends Module {
    private ButtonSetting filterKnownWords;
    private List<String> filteredWords = Arrays.asList(
            // blocked words
            "kill", "retard", "anal", "beaner", "bestiality", "blowjob", "cameltoe", "chink", "clit", "cock", "coon", "cunnilingus", "cunt", "dick", "dildo", "dilf", "dyke", "ejaculate", "ejaculati" /*ing & ion*/,
            "fag", "foreskin", "gilf", "hentai", "jerkoff", "jizz", "kike", "kill yourself", "kill urself", "kys", "loli", "masturbate", "masturbati" /*ing & ion*/, "milf", "nazi", "nigga", "nigger",
            "orgy", "pedo", "penis", "porn", "pussy", "rape", "raping", "redtube", "retard", "schlong", "shemale", "sex", "swastika", "tits", "titties", "trannie", "tranny", "vagina", "whore", "xhamster",
            "xvideos", "end",
            // censored words
            "arse", "ass", "bastard", "bitch", "boob", "douche", "fuck", "hitler", "shit", "twat", "wank"
    );

    private List<String> allowedCommands = Arrays.asList(
            "ac", "achat", "pc", "pchat", "gc", "gchat", "shout", "msg", "message", "r", "reply", "t", "tell", "w", "whisper"
    );

    private String replace_a = "\u00E1", replace_e = "\u00E9", replace_i = "\u00A1", replace_o = "\u00F3", replace_u = "\u00FA", replace_y = "\u00FF", replace_A = "\u00C1", replace_E = "\u00C9", replace_I = replace_i, replace_O = "\u00D3", replace_U = "\u00DA", replace_Y = replace_y;

    public ChatBypass() {
        super("Chat Bypass", category.other);
        this.registerSetting(filterKnownWords = new ButtonSetting("Only filter known words", true));
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (!(e.getPacket() instanceof C01PacketChatMessage)) return;
        C01PacketChatMessage c01 = (C01PacketChatMessage) e.getPacket();
        String msg = c01.getMessage();
        String[] split = splitCommand(msg);

        if (split == null || split[1].isEmpty()) {
            return; // filter commands
        }

        msg = split[1]; // set msg to just message to ignore command

        if (filterKnownWords.isToggled()) {
            StringBuilder newMsg = new StringBuilder();
            String[] words = msg.split(" ");
            for (String word : words) {
                String lowerCaseWord = word.toLowerCase();
                for (String filteredWord : filteredWords) {
                    int index = lowerCaseWord.indexOf(filteredWord.toLowerCase());
                    if (index != -1) {
                        String matched = word.substring(index, index + filteredWord.length()),
                                replaced = doReplace(matched);
                        word = word.substring(0, index) + replaced + word.substring(index + filteredWord.length());
                    }
                }
                newMsg.append(word).append(" ");
            }
            msg = newMsg.toString().trim();
        }
        else {
            msg = doReplace(msg);
        }

        if (split[0] != null) { // if command existed, re-add
            msg = split[0] + " " + msg;
        }

        PacketUtils.sendPacketNoEvent(new C01PacketChatMessage(msg));
        e.setCanceled(true); // cancel original packet
    }

    private String[] splitCommand(String msg) {
        if (msg.startsWith("/")) {
            if (!isValidCommand(msg)) {
                return null;
            }
            int spaceIndex = msg.indexOf(" ");
            if (spaceIndex != -1) { // command arguments found
                return new String[]{
                        msg.substring(0, spaceIndex), // command
                        msg.substring(spaceIndex + 1) // args
                };
            }
        }
        return new String[]{null, msg};
    }

    private String doReplace(String text) {
        return text
                .replace("a", replace_a).replace("e", replace_e).replace("i", replace_i)
                .replace("o", replace_o).replace("u", replace_u).replace("y", replace_y)
                .replace("A", replace_A).replace("E", replace_E).replace("I", replace_I)
                .replace("O", replace_O).replace("U", replace_U).replace("Y", replace_Y);
    }

    // assumes its already a command (starts with /)
    private boolean isValidCommand(String msg) {
        for (String cmd : allowedCommands) {
            String _cmd = "/" + cmd + " ";
            if (msg.startsWith(_cmd)) {
                return true;
            }
        }
        return false;
    }
}
