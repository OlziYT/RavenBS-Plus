package keystrokesmod.script.packets.clientbound;

public class SPacket {
    public String name;
    public net.minecraft.network.Packet packet;

    public SPacket(net.minecraft.network.Packet packet) {
        this.packet = packet;
        this.name = packet.getClass().getSimpleName();
    }
}
