package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.net.Packet;
import vintadustry.DonaTree;

public class TreeSyncPacket
extends Packet {
    public int pos;
    public byte[] g = new byte[8];

    public void write(Writes writes) {
        writes.i(this.pos);
        for (int i = 0; i < 8; ++i) {
            writes.b((int)this.g[i]);
        }
    }

    public void read(Reads reads) {
        this.pos = reads.i();
        for (int i = 0; i < 8; ++i) {
            this.g[i] = reads.b();
        }
    }

    public void handleClient() {
        Building building = Vars.world.build(this.pos);
        if (building instanceof DonaTree.DonaTreeBuild) {
            ((DonaTree.DonaTreeBuild)building).applySync(this.g);
        }
    }
}
