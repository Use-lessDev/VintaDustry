/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.math.Angles
 *  arc.math.Mathf
 *  arc.math.geom.Position
 *  arc.util.Time
 *  arc.util.io.Reads
 *  arc.util.io.Writes
 *  mindustry.Vars
 *  mindustry.content.Fx
 *  mindustry.entities.Units
 *  mindustry.game.Team
 *  mindustry.gen.Building
 *  mindustry.gen.Call
 *  mindustry.gen.Groups
 *  mindustry.gen.Player
 *  mindustry.gen.Unit
 *  mindustry.type.Item
 */
package vintadustry;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import java.util.Iterator;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.Item;
import vintadustry.DualUnit;
import vintadustry.Rope;

public class GumpyUnit
extends DualUnit {
    public static int gumpyClassId = -1;
    public DualUnit tower;
    public Unit leader;
    public static final float launchSpeed = 6.0f;
    public static final float launchTime = 25.0f;
    public static final float launchDamage = 350.0f;
    public float flungTime;
    public Player pendingPlayer;
    protected float flungAngle;
    protected float noPlayer;

    @Override
    public int classId() {
        return gumpyClassId;
    }

    @Override
    public int ownSlots() {
        return 1;
    }

    @Override
    public int maxCrates() {
        return 2;
    }

    @Override
    public boolean isTasky() {
        return false;
    }

    public void launch(float f) {
        this.flungAngle = f;
        this.flungTime = 25.0f;
        this.health = Math.max(this.health - this.maxHealth * 0.5f, 1.0f);
        this.rotation = f;
        this.vel.setZero();
    }

    protected void updateLaunch() {
        this.flungTime -= Time.delta;
        float f = 6.0f * Time.delta;
        float f2 = this.x;
        float f3 = this.y;
        this.vel.setZero();
        this.move(Angles.trnsx((float)this.flungAngle, (float)f), Angles.trnsy((float)this.flungAngle, (float)f));
        this.vel.setZero();
        this.rotation = this.flungAngle;
        boolean bl = Mathf.dst((float)f2, (float)f3, (float)this.x, (float)this.y) < f * 0.5f;
        boolean[] blArray = new boolean[]{false};
        if (!Vars.net.client()) {
            Building building;
            Units.nearbyEnemies((Team)this.team, (float)(this.x - this.hitSize), (float)(this.y - this.hitSize), (float)(this.hitSize * 2.0f), (float)(this.hitSize * 2.0f), unit -> {
                if (blArray[0] || unit.dead || unit.isFlying()) {
                    return;
                }
                if (this.within((Position)unit, (this.hitSize + unit.hitSize) / 2.0f)) {
                    unit.damage(350.0f);
                    blArray[0] = true;
                }
            });
            if (!blArray[0] && (building = Vars.world.buildWorld(this.x, this.y)) != null && building.team != this.team) {
                building.damage(this.team, 350.0f);
                blArray[0] = true;
            }
        }
        if (blArray[0] || bl || this.flungTime <= 0.0f) {
            this.flungTime = 0.0f;
            Fx.explosion.at(this.x, this.y);
            if (this.pendingPlayer != null) {
                if (this.pendingPlayer.isAdded()) {
                    Call.unitControl((Player)this.pendingPlayer, (Unit)this);
                }
                this.pendingPlayer = null;
            }
        }
    }

    @Override
    public void writeSync(Writes writes) {
        super.writeSync(writes);
        writes.i(this.tower == null ? -1 : this.tower.id);
        writes.f(this.flungTime);
        writes.f(this.flungAngle);
    }

    @Override
    public void readSync(Reads reads) {
        DualUnit dualUnit;
        super.readSync(reads);
        int n = reads.i();
        Unit unit = n < 0 ? null : (Unit)Groups.unit.getByID(n);
        this.tower = unit instanceof DualUnit ? (dualUnit = (DualUnit)unit) : null;
        this.leader = this.tower;
        if (this.tower != null && !this.tower.towed.contains((Object)this)) {
            this.tower.towed.add((Object)this);
        }
        this.flungTime = reads.f();
        this.flungAngle = reads.f();
    }

    @Override
    public void update() {
        if (this.flungTime > 0.0f) {
            this.vel.setZero();
        }
        super.update();
        if (!Vars.net.client() && this.stack.amount > 0 && this.stack.item != null && !this.acceptsItem(this.stack.item)) {
            int n;
            DualUnit dualUnit = null;
            Iterator iterator = Groups.unit.iterator();
            while (iterator.hasNext()) {
                DualUnit dualUnit2;
                Unit unit = (Unit)iterator.next();
                if (!(unit instanceof DualUnit) || !(dualUnit2 = (DualUnit)unit).isTasky() || dualUnit2.team != this.team || dualUnit2.dead || !dualUnit2.within((Position)this, 220.0f)) continue;
                dualUnit = dualUnit2;
                break;
            }
            if (dualUnit != null && (n = Math.min(this.stack.amount, dualUnit.maxAccepted((Item)(iterator = this.stack.item)))) > 0) {
                dualUnit.addItem((Item)iterator, n);
                this.stack.amount -= n;
                if (this.stack.amount <= 0) {
                    this.stack.item = null;
                }
                Fx.mine.at(this.x, this.y, 0.0f, ((Item)iterator).color);
            }
        }
        if (!Vars.net.client()) {
            if (this.pendingPlayer != null && !this.pendingPlayer.isAdded()) {
                this.pendingPlayer = null;
            }
            if (this.isPlayer() || this.pendingPlayer != null) {
                this.noPlayer = 0.0f;
            } else {
                float f;
                this.noPlayer += Time.delta;
                if (f > 180.0f) {
                    Call.unitDespawn((Unit)this);
                    return;
                }
            }
        }
        if (this.flungTime > 0.0f && !Vars.net.client()) {
            this.updateLaunch();
        }
        if (this.tower != null) {
            if (this.tower.dead || !this.tower.isAdded() || !this.tower.towed.contains((Object)this)) {
                this.tower = null;
                this.leader = null;
            } else if (this.leader != null && !this.leader.dead && this.leader.isAdded() && (!Vars.net.client() || this.isLocal())) {
                this.pullByRope();
            }
        }
    }

    public float ropeLength() {
        return (this.leader.hitSize + this.hitSize) / 2.0f + 36.0f;
    }

    public float stretch() {
        if (this.leader == null) {
            return 0.0f;
        }
        return Rope.stretch((Unit)this, this.leader, this.ropeLength());
    }

    protected void pullByRope() {
        Rope.pull((Unit)this, this.leader, this.ropeLength(), 0.03f, 24.0f);
    }
}
