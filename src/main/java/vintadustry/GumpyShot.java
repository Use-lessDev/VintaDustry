package vintadustry;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import vintadustry.DualUnit;
import vintadustry.TaskyFx;
import vintadustry.TaskyNest;
import vintadustry.TestMod;

public class GumpyShot
extends BasicBulletType {
    public final boolean visual;
    static TextureRegion gumpyRegion;
    static TextureRegion shadowRegion;

    public GumpyShot(boolean bl) {
        super(6.0f, bl ? 0.0f : 350.0f, "vintadustry-gumpy");
        this.visual = bl;
        this.width = 18.0f;
        this.height = 18.0f;
        this.lifetime = 25.0f;
        this.pierce = !bl;
        this.pierceBuilding = false;
        this.pierceCap = 5;
        this.collides = !bl;
        this.collidesAir = false;
        this.hittable = false;
        this.absorbable = false;
        this.reflectable = false;
        this.shrinkY = 0.0f;
        this.shrinkX = 0.0f;
        this.despawnEffect = Fx.none;
        this.hitEffect = Fx.none;
        this.trailLength = 18;
        this.trailWidth = 4.5f;
        this.trailColor = Pal.accent;
        this.trailInterp = Interp.linear;
    }

    boolean fx() {
        return !Vars.headless && this.visual == Vars.net.active();
    }

    static boolean blocked(float f, float f2) {
        Tile tile = Vars.world.tileWorld(f, f2);
        return tile == null || tile.solid();
    }

    boolean stopAtWall(Bullet bullet) {
        if (!GumpyShot.blocked(bullet.x, bullet.y)) {
            return false;
        }
        float f = bullet.rotation();
        float f2 = bullet.x;
        float f3 = bullet.y;
        for (int i = 0; i < 6 && GumpyShot.blocked(f2, f3); ++i) {
            f2 -= Angles.trnsx((float)f, (float)2.0f);
            f3 -= Angles.trnsy((float)f, (float)2.0f);
        }
        bullet.set(f2, f3);
        if (this.fx()) {
            TaskyFx.land.at(f2, f3, f, bullet.team.color);
        }
        bullet.remove();
        return true;
    }

    public void update(Bullet bullet) {
        super.update(bullet);
        if (this.stopAtWall(bullet)) {
            return;
        }
        if (this.fx() && bullet.timer(1, 2.2f)) {
            TaskyFx.puff.at(bullet.x, bullet.y, bullet.rotation(), bullet.team.color);
        }
    }

    public void draw(Bullet bullet) {
        float f;
        if (!this.visual && Vars.net.client()) {
            return;
        }
        if (gumpyRegion == null) {
            gumpyRegion = Core.atlas.find("vintadustry-gumpy");
            shadowRegion = Core.atlas.find("circle-shadow");
        }
        Color color = bullet.team.color;
        float f2 = bullet.fin();
        float f3 = Mathf.sin((float)(f2 * (float)Math.PI)) * 11.0f;
        if (bullet.trail != null) {
            f = Draw.z();
            Draw.z((float)99.999f);
            bullet.trail.draw(color, this.trailWidth);
            Draw.z((float)f);
        }
        Draw.z((float)99.99f);
        Draw.color((float)0.0f, (float)0.0f, (float)0.0f, (float)(0.28f * (1.0f - f3 / 22.0f)));
        Draw.rect((TextureRegion)shadowRegion, (float)bullet.x, (float)bullet.y, (float)(17.0f * (1.0f - f3 / 40.0f)), (float)(9.0f * (1.0f - f3 / 40.0f)));
        Draw.color();
        Draw.z((float)100.0f);
        Draw.blend((Blending)Blending.additive);
        Draw.color((Color)color, (float)0.35f);
        Fill.circle((float)bullet.x, (float)(bullet.y + f3), (float)(8.5f + Mathf.absin((float)bullet.time, (float)3.0f, (float)1.2f)));
        Draw.blend();
        f = 1.0f + 0.12f * (f3 / 11.0f);
        float f4 = Mathf.sin((float)(bullet.time * 0.9f)) * 9.0f;
        Draw.color();
        Draw.rect((TextureRegion)gumpyRegion, (float)bullet.x, (float)(bullet.y + f3), (float)(this.width * f), (float)(this.height * f), (float)(bullet.rotation() - 90.0f + f4));
        Draw.reset();
    }

    public void hit(Bullet bullet, float f, float f2) {
        super.hit(bullet, f, f2);
        if (this.fx()) {
            TaskyFx.impact.at(f, f2, bullet.rotation(), bullet.team.color);
        }
    }

    public void despawned(Bullet bullet) {
        super.despawned(bullet);
        if (this.fx()) {
            TaskyFx.land.at(bullet.x, bullet.y, bullet.rotation(), bullet.team.color);
        }
    }

    public void removed(Bullet bullet) {
        super.removed(bullet);
        if (this.visual || Vars.net.client()) {
            return;
        }
        Object object = bullet.data;
        if (!(object instanceof Player)) {
            return;
        }
        Player player = (Player)object;
        bullet.data = null;
        DualUnit.inFlight.remove((Object)player);
        if (!player.isAdded()) {
            return;
        }
        float f = bullet.x - Angles.trnsx((float)bullet.rotation(), (float)6.0f);
        float f2 = bullet.y - Angles.trnsy((float)bullet.rotation(), (float)6.0f);
        for (int i = 0; i < 6 && GumpyShot.blocked(f, f2); ++i) {
            f -= Angles.trnsx((float)bullet.rotation(), (float)3.0f);
            f2 -= Angles.trnsy((float)bullet.rotation(), (float)3.0f);
        }
        Unit unit = TestMod.gumpy.create(bullet.team);
        unit.set(f, f2);
        unit.rotation = bullet.rotation();
        unit.health = Math.max(unit.maxHealth * 0.5f, 1.0f);
        unit.add();
        TaskyNest.waiting.remove((Object)player);
        Call.unitControl((Player)player, (Unit)unit);
    }
}
