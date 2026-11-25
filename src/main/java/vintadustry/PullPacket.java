/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.util.io.Reads
 *  arc.util.io.Writes
 *  mindustry.Vars
 *  mindustry.gen.Building
 *  mindustry.gen.Player
 *  mindustry.gen.Unit
 *  mindustry.net.NetConnection
 *  mindustry.net.Packet
 */
package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.DonaTree;

public class PullPacket
extends Packet {
    public int pos;
    public byte slot;

    public void write(Writes writes) {
        writes.i(this.pos);
        writes.b((int)this.slot);
    }

    public void read(Reads reads) {
        this.pos = reads.i();
        this.slot = reads.b();
    }

    public void handleServer(NetConnection netConnection) {
        Player player = netConnection.player;
        if (player == null || player.unit() == null || player.unit().dead) {
            return;
        }
        Unit unit = player.unit();
        Building building = Vars.world.build(this.pos);
        if (building instanceof DonaTree.DonaTreeBuild) {
            DonaTree.DonaTreeBuild donaTreeBuild = (DonaTree.DonaTreeBuild)building;
            donaTreeBuild.rip(this.slot, unit);
        }
    }
}
