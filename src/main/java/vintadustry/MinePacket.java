package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.MineGame;

public class MinePacket
extends Packet {
    public byte type;
    public int pos;

    public void write(Writes writes) {
        writes.b((int)this.type);
        writes.i(this.pos);
    }

    public void read(Reads reads) {
        this.type = reads.b();
        this.pos = reads.i();
    }

    public void handleServer(NetConnection netConnection) {
        if (netConnection.player != null) {
            MineGame.serverEvent(netConnection.player, this.type, this.pos);
        }
    }
}
