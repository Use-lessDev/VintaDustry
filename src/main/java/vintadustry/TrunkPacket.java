package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.DonaTree;

public class TrunkPacket
extends Packet {
    public int pos;

    public void write(Writes writes) {
        writes.i(this.pos);
    }

    public void read(Reads reads) {
        this.pos = reads.i();
    }

    public void handleServer(NetConnection netConnection) {
        Player player = netConnection.player;
        if (player == null || player.unit() == null || player.unit().dead) {
            return;
        }
        Building building = Vars.world.build(this.pos);
        if (building instanceof DonaTree.DonaTreeBuild) {
            DonaTree.DonaTreeBuild donaTreeBuild = (DonaTree.DonaTreeBuild)building;
            donaTreeBuild.ripTrunk(player.unit());
        }
    }
}
