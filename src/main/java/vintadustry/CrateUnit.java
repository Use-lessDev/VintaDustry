package vintadustry;

import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.EntityCollisions;
import mindustry.entities.Units;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.type.ItemStack;
import vintadustry.DualUnit;
import vintadustry.Rope;

public class CrateUnit
extends UnitEntity {
    public static int classId = -1;
    public ItemStack[] stacks = new ItemStack[]{new ItemStack(), new ItemStack()};
    public Unit leader;
    public DualUnit owner;
    public DualUnit producer;

    public int classId() {
        return classId;
    }

    public void update() {
        if (!Vars.net.client() && !this.dead) {
            Rope.freeUp((Unit)this);
        }
        super.update();
        this.collideOthers();
        if (!(this.owner == null || !this.owner.dead && this.owner.isAdded() && this.owner.crates.contains((Object)this))) {
            this.owner = null;
            this.leader = null;
        }
        if (this.owner == null || this.leader == null || this.leader.dead || !this.leader.isAdded()) {
            if (this.owner == null) {
                this.leader = null;
            }
            return;
        }
        if (!Vars.net.client()) {
            Rope.pull((Unit)this, this.leader, this.ropeLength(), 0.06f, 12.0f);
            this.vel.limit(Math.max(this.leader.speed() * 1.25f, 0.5f));
        }
        if (this.vel.len() > 0.15f) {
            this.rotation = Mathf.slerpDelta((float)this.rotation, (float)this.vel.angle(), (float)0.12f);
        }
    }

    public float ropeLength() {
        return (this.leader.hitSize + this.hitSize) / 2.0f + 8.0f;
    }

    protected DualUnit group() {
        return this.owner;
    }

    protected void collideOthers() {
        if (this.isFlying() || this.dead) {
            return;
        }
        Units.nearby((float)(this.x - this.hitSize), (float)(this.y - this.hitSize), (float)(this.hitSize * 2.0f), (float)(this.hitSize * 2.0f), unit -> {
            CrateUnit crateUnit;
            if (unit == this || unit.dead || unit.isFlying()) {
                return;
            }
            DualUnit dualUnit = this.group();
            if (dualUnit != null && (unit == dualUnit || unit instanceof CrateUnit && (crateUnit = (CrateUnit)((Object)unit)).group() == dualUnit)) {
                return;
            }
            float f = this.x - unit.x;
            float f2 = this.y - unit.y;
            float f3 = f * f + f2 * f2;
            float f4 = (this.hitSize + unit.hitSize) / 2.0f;
            if (f3 >= f4 * f4 || f3 < 1.0E-4f) {
                return;
            }
            float f5 = Mathf.sqrt((float)f3);
            float f6 = (f4 - f5) * 0.25f;
            this.move((f /= f5) * f6, (f2 /= f5) * f6);
            unit.move(-f * f6, -f2 * f6);
        });
    }

    public EntityCollisions.SolidPred solidity() {
        return EntityCollisions::solid;
    }

    public void write(Writes writes) {
        super.write(writes);
        this.writeStacks(writes);
    }

    public void read(Reads reads) {
        super.read(reads);
        this.readStacks(reads);
    }

    public void writeSync(Writes writes) {
        super.writeSync(writes);
        this.writeStacks(writes);
        writes.i(this.owner == null ? -1 : this.owner.id);
        writes.i(this.leader == null ? -1 : this.leader.id);
    }

    public void readSync(Reads reads) {
        DualUnit dualUnit;
        super.readSync(reads);
        this.readStacks(reads);
        int n = reads.i();
        int n2 = reads.i();
        Unit unit = n < 0 ? null : (Unit)Groups.unit.getByID(n);
        Unit unit2 = n2 < 0 ? null : (Unit)Groups.unit.getByID(n2);
        this.owner = unit instanceof DualUnit ? (dualUnit = (DualUnit)unit) : null;
        this.leader = this.owner == null ? null : unit2;
        Unit unit3 = this.leader;
        if (this.owner != null && !this.owner.crates.contains((Object)this)) {
            this.owner.crates.add((Object)this);
        }
    }

    protected void writeStacks(Writes writes) {
        for (ItemStack itemStack : this.stacks) {
            writes.s(itemStack.item == null ? -1 : (int)itemStack.item.id);
            writes.i(itemStack.amount);
        }
    }

    protected void readStacks(Reads reads) {
        for (ItemStack itemStack : this.stacks) {
            short s = reads.s();
            int n = reads.i();
            itemStack.item = s < 0 ? null : Vars.content.item((int)s);
            itemStack.amount = itemStack.item == null ? 0 : n;
        }
    }
}
