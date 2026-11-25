package vintadustry;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import java.util.Arrays;
import java.util.Iterator;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Hitboxc;
import mindustry.gen.MechUnit;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.world.blocks.storage.CoreBlock;
import vintadustry.BranchUnit;
import vintadustry.CrateUnit;
import vintadustry.GumpyUnit;
import vintadustry.PartDraw;
import vintadustry.PartHealth;
import vintadustry.Rope;
import vintadustry.TaskyFx;
import vintadustry.TaskyNest;
import vintadustry.TestMod;

public class DualUnit
extends MechUnit {
    public static final int slots = 3;
    public static int classId = -1;
    public static final float mouthOffset = 12.0f;
    public static final float gumpyTime = 300.0f;
    public ItemStack[] extra = new ItemStack[]{new ItemStack(), new ItemStack()};
    public Seq<CrateUnit> crates = new Seq();
    public Seq<GumpyUnit> towed = new Seq();
    public float trailAngle = 270.0f;
    public float gumpyProgress;
    public final PartHealth parts = new PartHealth();
    protected CoreBlock.CoreBuild fake;
    protected int[] snapshot = new int[0];
    protected CoreBlock.CoreBuild syncedCore;
    protected float coreSync;
    public boolean aiming;
    public float aimAng;
    protected boolean wasAiming;
    protected float aimHold;
    protected float lastAim;
    public static final float crateTime = 27000.0f;
    public static final int maxProducedCrates = 3;
    public float crateProgress;
    public static final Seq<Player> inFlight = new Seq();
    public final Seq<Player> loaded = new Seq();
    protected float loadedTime;
    protected float shootCharge;

    public int ownSlots() {
        return 3;
    }

    public int maxCrates() {
        return 6;
    }

    public boolean buildsFromCarried() {
        return this.isTasky();
    }

    public boolean isTasky() {
        return true;
    }

    public int slotCount() {
        return this.ownSlots() + this.crates.size * 2;
    }

    public ItemStack slot(int n) {
        if (n == 0) {
            return this.stack;
        }
        if (n < this.ownSlots()) {
            return this.extra[n - 1];
        }
        int n2 = n - this.ownSlots();
        return ((CrateUnit)((Object)this.crates.get((int)(n2 / 2)))).stacks[n2 % 2];
    }

    public int capacityOf(int n) {
        if (n < this.ownSlots()) {
            return this.itemCapacity();
        }
        return ((CrateUnit)((Object)this.crates.get((int)((n - this.ownSlots()) / 2)))).type.itemCapacity;
    }

    public float speed() {
        return super.speed() * Mathf.pow((float)0.85f, (float)this.crates.size);
    }

    protected int slotFor(Item item) {
        return this.slotFor(item, this.slotCount());
    }

    protected int slotFor(Item item, int n) {
        int n2;
        if (item == null) {
            return -1;
        }
        for (n2 = 0; n2 < n; ++n2) {
            ItemStack itemStack = this.slot(n2);
            if (itemStack.amount <= 0 || itemStack.item != item || itemStack.amount >= this.capacityOf(n2)) continue;
            return n2;
        }
        for (n2 = 0; n2 < n; ++n2) {
            if (this.slot((int)n2).amount > 0) continue;
            return n2;
        }
        return -1;
    }

    public int classId() {
        return classId;
    }

    public boolean acceptsItem(Item item) {
        int n = this.slotFor(item);
        return n >= 0 && this.slot((int)n).amount < this.capacityOf(n);
    }

    public int maxAccepted(Item item) {
        int n = this.slotFor(item);
        return n < 0 ? 0 : Math.max(this.capacityOf(n) - this.slot((int)n).amount, 0);
    }

    public void addItem(Item item) {
        this.addItem(item, 1);
    }

    public void addItem(Item item, int n) {
        int n2 = this.slotFor(item);
        if (n2 < 0) {
            return;
        }
        ItemStack itemStack = this.slot(n2);
        itemStack.amount = Math.min((itemStack.item == item ? itemStack.amount : 0) + n, this.capacityOf(n2));
        itemStack.item = item;
    }

    public boolean hasItem() {
        int n = this.slotCount();
        for (int i = 0; i < n; ++i) {
            if (this.slot((int)i).amount <= 0) continue;
            return true;
        }
        return false;
    }

    public void clearItem() {
        Item item = this.stack.item;
        int n = this.stack.amount;
        if (TestMod.galho != null && item == TestMod.galho && n > 0 && !Vars.net.client() && DualUnit.fromDrop()) {
            float f = this.hitSize / 2.0f + 16.0f + 28.0f;
            BranchUnit.scatter(this.team, this.x + Angles.trnsx((float)this.rotation, (float)f), this.y + Angles.trnsy((float)this.rotation, (float)f), -1, n, 90.0f, this.x + Angles.trnsx((float)this.rotation, (float)(this.hitSize / 3.0f)), this.y + Angles.trnsy((float)this.rotation, (float)(this.hitSize / 3.0f)), 12.0f, 1.6f);
        }
        super.clearItem();
    }

    public void dropSlot(int n) {
        if (Vars.net.client() || this.dead || n < 0 || n >= this.ownSlots()) {
            return;
        }
        ItemStack itemStack = this.slot(n);
        if (itemStack.amount <= 0 || itemStack.item == null) {
            return;
        }
        if (TestMod.galho != null && itemStack.item == TestMod.galho) {
            float f = this.hitSize / 2.0f + 16.0f + 28.0f;
            BranchUnit.scatter(this.team, this.x + Angles.trnsx((float)this.rotation, (float)f), this.y + Angles.trnsy((float)this.rotation, (float)f), -1, itemStack.amount, 90.0f, this.x + Angles.trnsx((float)this.rotation, (float)(this.hitSize / 3.0f)), this.y + Angles.trnsy((float)this.rotation, (float)(this.hitSize / 3.0f)), 12.0f, 1.6f);
        }
        itemStack.amount = 0;
        itemStack.item = null;
    }

    protected static boolean fromDrop() {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (!stackTraceElement.getMethodName().contains("dropItem")) continue;
            return true;
        }
        return false;
    }

    protected CoreBlock.CoreBuild carriedCore() {
        CoreBlock.CoreBuild coreBuild = (CoreBlock.CoreBuild)TaskyNest.mobile.get((Object)this.team);
        if (coreBuild != null && Vars.state.teams.cores(this.team).contains((Object)coreBuild)) {
            return coreBuild;
        }
        if (this.fake == null || this.fake.team != this.team) {
            this.fake = (CoreBlock.CoreBuild)Blocks.coreShard.newBuilding().create(Blocks.coreShard, this.team);
            this.fake.set(-100000.0f, -100000.0f);
            this.fake.storageCapacity = 1000000;
        }
        return this.fake;
    }

    public CoreBlock.CoreBuild core() {
        if (!this.buildsFromCarried()) {
            return super.core();
        }
        return this.carriedCore();
    }

    protected void syncFake() {
        int n;
        if (!this.buildsFromCarried()) {
            return;
        }
        CoreBlock.CoreBuild coreBuild = this.carriedCore();
        if (this.snapshot.length != Vars.content.items().size) {
            this.snapshot = new int[Vars.content.items().size];
        }
        if (coreBuild != this.syncedCore) {
            Arrays.fill(this.snapshot, 0);
            coreBuild.items.clear();
            this.syncedCore = coreBuild;
        }
        for (Item item : Vars.content.items()) {
            ItemStack itemStack;
            int n2;
            int n3;
            int n4 = coreBuild.items.get(item) - this.snapshot[item.id];
            if (n4 < 0) {
                n3 = -n4;
                for (n2 = 0; n2 < this.slotCount() && n3 > 0; ++n2) {
                    itemStack = this.slot(n2);
                    if (itemStack.amount <= 0 || itemStack.item != item) continue;
                    n = Math.min(itemStack.amount, n3);
                    itemStack.amount -= n;
                    n3 -= n;
                }
                continue;
            }
            if (n4 <= 0) continue;
            n3 = n4;
            for (n2 = 0; n2 < this.slotCount() && n3 > 0; ++n2) {
                itemStack = this.slot(n2);
                if (itemStack.amount <= 0 || itemStack.item != item) continue;
                n = Math.min(this.capacityOf(n2) - itemStack.amount, n3);
                itemStack.amount += n;
                n3 -= n;
            }
            for (n2 = 0; n2 < this.slotCount() && n3 > 0; ++n2) {
                itemStack = this.slot(n2);
                if (itemStack.amount > 0) continue;
                n = Math.min(this.capacityOf(n2), n3);
                itemStack.set(item, n);
                n3 -= n;
            }
        }
        coreBuild.items.clear();
        Arrays.fill(this.snapshot, 0);
        for (int i = 0; i < this.slotCount(); ++i) {
            Item item;
            item = this.slot(i);
            if (item.amount <= 0 || item.item == null) continue;
            coreBuild.items.add(item.item, item.amount);
            n = item.item.id;
            this.snapshot[n] = this.snapshot[n] + item.amount;
        }
    }

    public void mouthPos(Vec2 vec2) {
        vec2.set(0.0f, 12.0f).rotate(this.rotation - 90.0f).add(this.x, this.y);
    }

    public float aimAngle() {
        if (this.aiming) {
            return this.aimAng;
        }
        if (this.isShooting()) {
            return Angles.angle((float)this.x, (float)this.y, (float)this.aimX, (float)this.aimY);
        }
        if (this.vel.len() > 0.2f) {
            return this.vel.angle();
        }
        return -1.0f;
    }

    public void draw() {
        float f = this.gumpyProgress;
        this.gumpyProgress = 0.0f;
        PartHealth.Set set = PartHealth.setFor((Unit)this);
        boolean bl = set.ready() && PartHealth.blank().found();
        UnitType unitType = this.type;
        TextureRegion textureRegion = unitType.region;
        if (bl) {
            unitType.region = PartHealth.blank();
        }
        super.draw();
        if (bl) {
            unitType.region = textureRegion;
        }
        this.gumpyProgress = f;
        if (bl) {
            PartDraw.draw(this.parts, (Unit)this, set);
        }
        TaskyFx.drawCharge(this);
        this.drawCountsForOthers();
    }

    void drawCountsForOthers() {
        if (!this.isTasky() || this.isLocal() || Vars.renderer.pixelator.enabled() || this.itemTime <= 0.01f) {
            return;
        }
        float[][] fArrayArray = new float[][]{{0.0f, -6.5f}, {-7.0f, -6.5f}, {7.0f, -6.5f}};
        for (int i = 0; i < 3; ++i) {
            ItemStack itemStack = this.slot(i);
            if (itemStack.amount <= 0 || itemStack.item == null) continue;
            Tmp.v1.set(fArrayArray[i][0], fArrayArray[i][1]).rotate(this.rotation - 90.0f).add(this.x, this.y);
            Draw.z((float)100.0f);
            Fonts.outline.draw((CharSequence)("" + itemStack.amount), Tmp.v1.x, Tmp.v1.y - 3.0f, Pal.accent, 0.25f / Scl.scl((float)1.0f), false, 1);
        }
        Draw.reset();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void destroy() {
        if (!this.isAdded()) {
            super.destroy();
            return;
        }
        if (!Vars.headless) {
            TaskyFx.unitDeath(this, this instanceof GumpyUnit);
        }
        UnitType unitType = this.type;
        Effect effect = unitType.deathExplosionEffect;
        Sound sound = unitType.deathSound;
        unitType.deathExplosionEffect = Fx.none;
        unitType.deathSound = new Sound();
        try {
            super.destroy();
        }
        finally {
            unitType.deathExplosionEffect = effect;
            unitType.deathSound = sound;
        }
    }

    public void rawDamage(float f) {
        float f2 = this.health;
        super.rawDamage(f);
        float f3 = f2 - this.health;
        if (f3 > 0.0f && !Vars.net.client()) {
            this.parts.damaged((Unit)this, f3);
        }
    }

    public void collision(Hitboxc hitboxc, float f, float f2) {
        super.collision(hitboxc, f, f2);
        if (hitboxc instanceof Bullet && !Vars.net.client()) {
            this.parts.hitAt((Unit)this, f, f2);
        }
    }

    protected void chargeFx() {
        if (Vars.headless || this.gumpyProgress < 0.5f || this.aimAngle() < 0.0f) {
            return;
        }
        if (Mathf.chance((double)(0.06 * (double)Time.delta * (double)this.gumpyProgress))) {
            Vec2 vec2 = Tmp.v3;
            this.mouthPos(vec2);
            TaskyFx.sparkle.at(vec2.x, vec2.y, this.aimAngle(), this.team.color);
        }
    }

    protected void updateCrates() {
        CrateUnit crateUnit;
        int n = 0;
        for (Unit unit : Groups.unit) {
            if (!(unit instanceof CrateUnit)) continue;
            crateUnit = (CrateUnit)unit;
            if (crateUnit.producer != this || crateUnit.dead) continue;
            ++n;
        }
        if (n >= 3) {
            this.crateProgress = 0.0f;
            return;
        }
        this.crateProgress += Time.delta / 27000.0f;
        if (this.crateProgress >= 1.0f) {
            Unit unit;
            this.crateProgress = 0.0f;
            unit = Tmp.v3.set(0.0f, -12.0f).rotate(this.rotation - 90.0f).add(this.x, this.y);
            Iterator iterator = TestMod.crate.create(this.team);
            if (iterator instanceof CrateUnit) {
                crateUnit = (CrateUnit)((Object)iterator);
                crateUnit.producer = this;
                crateUnit.set(unit.x, unit.y);
                Rope.freeUp((Unit)crateUnit);
                crateUnit.rotation = this.rotation + 180.0f;
                crateUnit.vel.trns(this.rotation + 180.0f, 4.0f);
                crateUnit.add();
                Fx.spawn.at(unit.x, unit.y);
            }
        }
    }

    public void swallow(GumpyUnit gumpyUnit) {
        Player player = gumpyUnit.getPlayer();
        if (player == null || gumpyUnit.dead) {
            return;
        }
        for (CrateUnit crateUnit : new Seq(gumpyUnit.crates)) {
            crateUnit.owner = null;
            crateUnit.leader = null;
        }
        gumpyUnit.crates.clear();
        if (gumpyUnit.tower != null) {
            gumpyUnit.tower.towed.remove((Object)gumpyUnit);
        }
        Call.unitDespawn((Unit)gumpyUnit);
        player.clearUnit();
        TaskyNest.waiting.remove((Object)player);
        if (!this.loaded.contains((Object)player)) {
            this.loaded.add((Object)player);
        }
        this.gumpyProgress = 1.0f;
    }

    protected void updateGumpy() {
        if (!this.isTasky()) {
            return;
        }
        if (!Vars.net.client() && !this.isPlayer()) {
            this.aiming = false;
        }
        if (!Vars.net.client()) {
            this.coreSync += Time.delta;
            boolean bl = this.coreSync >= 15.0f;
            boolean bl2 = bl;
            if (bl) {
                this.coreSync = 0.0f;
            }
            TaskyNest.follow(this, bl);
        }
        if (Vars.net.client()) {
            TaskyNest.follow(this, false);
            this.chargeFx();
            return;
        }
        this.chargeFx();
        this.updateCrates();
        this.loaded.removeAll(player -> player == null || !player.isAdded() || !player.dead() || player.team() != this.team);
        if (!this.loaded.isEmpty()) {
            this.gumpyProgress = 1.0f;
            this.loadedTime += Time.delta;
            if (this.aiming) {
                this.aimHold += Time.delta;
                this.lastAim = this.aimAng;
                this.wasAiming = true;
            } else {
                if (this.wasAiming && this.aimHold >= 8.0f) {
                    this.wasAiming = false;
                    this.aimHold = 0.0f;
                    this.releaseGumpy((Player)this.loaded.first(), this.lastAim);
                    return;
                }
                this.wasAiming = false;
                this.aimHold = 0.0f;
            }
            if (this.loadedTime > 1800.0f) {
                this.releaseGumpy((Player)this.loaded.first(), -1.0f);
            }
            return;
        }
        this.loadedTime = 0.0f;
        this.shootCharge = 0.0f;
        Player player2 = TaskyNest.nextWaiting(this.team);
        if (player2 == null) {
            this.gumpyProgress = 0.0f;
            return;
        }
        this.gumpyProgress += Time.delta / 300.0f;
        if (this.gumpyProgress >= 1.0f) {
            this.releaseGumpy(player2, -1.0f);
        }
    }

    protected void releaseGumpy(Player player, float f) {
        this.gumpyProgress = 0.0f;
        this.loaded.remove((Object)player);
        this.shootCharge = 0.0f;
        this.loadedTime = 0.0f;
        Vec2 vec2 = new Vec2();
        this.mouthPos(vec2);
        float f2 = f >= 0.0f ? f : this.aimAngle();
        TaskyNest.waiting.remove((Object)player);
        if (f2 >= 0.0f) {
            inFlight.add((Object)player);
            TestMod.gumpyShot.create((Entityc)this, this.team, vec2.x, vec2.y, f2, -1.0f, 1.0f, 1.0f, player);
            if (Vars.net.server()) {
                Call.createBullet((BulletType)TestMod.gumpyShotVisual, (Team)this.team, (float)vec2.x, (float)vec2.y, (float)f2, (float)0.0f, (float)1.0f, (float)1.0f);
            }
            if (Vars.net.server()) {
                Call.effect((Effect)TaskyFx.launch, (float)vec2.x, (float)vec2.y, (float)f2, (Color)this.team.color);
            } else {
                TaskyFx.launch.at(vec2.x, vec2.y, f2, this.team.color);
            }
            return;
        }
        Unit unit = TestMod.gumpy.create(this.team);
        unit.set(vec2.x, vec2.y);
        unit.rotation = this.rotation;
        unit.vel.trns(this.rotation, 1.5f);
        unit.add();
        Call.unitControl((Player)player, (Unit)unit);
    }

    public static boolean isLoaded(Player player) {
        for (Unit unit : Groups.unit) {
            DualUnit dualUnit;
            if (unit instanceof DualUnit) {
                dualUnit = (DualUnit)unit;
                if (dualUnit.loaded.contains((Object)player)) {
                    return true;
                }
            }
            if (unit instanceof GumpyUnit) {
                dualUnit = (GumpyUnit)unit;
                if (((GumpyUnit)dualUnit).pendingPlayer == player) {
                    return true;
                }
            }
            if (!inFlight.contains((Object)player)) continue;
            return true;
        }
        return false;
    }

    public void update() {
        ItemStack itemStack;
        int n;
        this.syncFake();
        if (this.vel.len() > 0.1f) {
            this.trailAngle = Mathf.slerpDelta((float)this.trailAngle, (float)this.vel.angle(), (float)0.1f);
        }
        this.crates.removeAll(crateUnit -> crateUnit.dead || !crateUnit.isAdded() || crateUnit.owner != this);
        this.towed.removeAll(gumpyUnit -> gumpyUnit.dead || !gumpyUnit.isAdded() || gumpyUnit.tower != this);
        if (!Vars.net.client()) {
            for (n = 0; n < this.crates.size; ++n) {
                ((CrateUnit)((Object)this.crates.get((int)n))).leader = n == 0 ? this : (Unit)this.crates.get(n - 1);
            }
        }
        for (n = 0; n < this.towed.size; ++n) {
            ((GumpyUnit)((Object)this.towed.get((int)n))).leader = this;
        }
        for (n = 0; n < this.ownSlots() - 1; ++n) {
            ItemStack itemStack2 = this.slot(n);
            itemStack = this.slot(n + 1);
            if (itemStack2.amount > 0 || itemStack.amount <= 0) continue;
            itemStack2.set(itemStack.item, itemStack.amount);
            itemStack.amount = 0;
        }
        int n2 = this.slotCount();
        block3: for (n = this.ownSlots(); n < n2; ++n) {
            itemStack = this.slot(n);
            if (itemStack.amount <= 0 || itemStack.item == null) continue;
            for (int i = 0; i < this.ownSlots(); ++i) {
                ItemStack itemStack3 = this.slot(i);
                if (itemStack3.amount > 0) continue;
                itemStack3.set(itemStack.item, itemStack.amount);
                itemStack.amount = 0;
                continue block3;
            }
        }
        if (this.stack.amount <= 0 || this.stack.item == null) {
            this.stack.item = Items.copper;
        }
        this.updateGumpy();
        super.update();
        this.parts.update((Unit)this);
    }

    public void write(Writes writes) {
        super.write(writes);
        this.writeExtra(writes);
        this.parts.init((Unit)this);
        this.parts.write(writes);
    }

    public void read(Reads reads) {
        super.read(reads);
        this.readExtra(reads);
        this.parts.read(reads, (Unit)this, false);
    }

    public void writeSync(Writes writes) {
        super.writeSync(writes);
        this.writeExtra(writes);
        writes.f(this.gumpyProgress);
        writes.f(this.crateProgress);
        this.parts.init((Unit)this);
        this.parts.write(writes);
    }

    public void readSync(Reads reads) {
        super.readSync(reads);
        this.readExtra(reads);
        this.gumpyProgress = reads.f();
        this.crateProgress = reads.f();
        this.parts.read(reads, (Unit)this, true);
    }

    protected void writeExtra(Writes writes) {
        for (ItemStack itemStack : this.extra) {
            writes.s(itemStack.item == null ? -1 : (int)itemStack.item.id);
            writes.i(itemStack.amount);
        }
    }

    protected void readExtra(Reads reads) {
        for (ItemStack itemStack : this.extra) {
            short s = reads.s();
            int n = reads.i();
            itemStack.item = s < 0 ? null : Vars.content.item((int)s);
            itemStack.amount = itemStack.item == null ? 0 : n;
        }
    }
}
