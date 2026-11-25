/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.util.io.Reads
 *  arc.util.io.Writes
 *  mindustry.gen.Groups
 *  mindustry.gen.Player
 *  mindustry.gen.Unit
 *  mindustry.net.NetConnection
 *  mindustry.net.Packet
 */
package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.BranchUnit;

public class DragPacket
extends Packet {
    public int id;
    public float x;
    public float y;
    public float rot;

    public void write(Writes writes) {
        writes.i(this.id);
        writes.f(this.x);
        writes.f(this.y);
        writes.f(this.rot);
    }

    public void read(Reads reads) {
        this.id = reads.i();
        this.x = reads.f();
        this.y = reads.f();
        this.rot = reads.f();
    }

    public void handleServer(NetConnection netConnection) {
        Player player = netConnection.player;
        if (player == null || player.unit() == null || player.unit().dead) {
            return;
        }
        Unit unit = player.unit();
        Unit unit2 = (Unit)Groups.unit.getByID(this.id);
        if (unit2 instanceof BranchUnit && unit2.team == unit.team && unit.within(this.x, this.y, 240.0f)) {
            ((BranchUnit)unit2).cancelFlight();
            unit2.set(this.x, this.y);
            unit2.rotation = this.rot;
        }
    }
}
