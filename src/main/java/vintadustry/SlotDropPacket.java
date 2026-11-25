package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.DualUnit;

public class SlotDropPacket
extends Packet {
    public byte slot;

    public void write(Writes writes) {
        writes.b((int)this.slot);
    }

    public void read(Reads reads) {
        this.slot = reads.b();
    }

    public void handleServer(NetConnection netConnection) {
        Player player = netConnection.player;
        if (player == null) {
            return;
        }
        Unit unit = player.unit();
        if (unit instanceof DualUnit && !unit.dead) {
            ((DualUnit)unit).dropSlot(this.slot);
        }
    }
}
