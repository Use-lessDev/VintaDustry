package vintadustry;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Unit;
import vintadustry.GumpyUnit;
import vintadustry.PartMap;
import vintadustry.TaskyFx;

public class PartHealth {
    public static final int MID = 0;
    public static final float[] share = new float[]{0.4f, 0.15f, 0.15f, 0.15f, 0.15f};
    static final String[] keys = new String[]{"mid", "tl", "tr", "l", "r"};
    public final float[] hp = new float[5];
    final float[] flash = new float[5];
    boolean inited;
    float pending;
    float hitX;
    float hitY;
    float hitStamp = -1.0f;
    float smokeT;
    static final Set tasky = new Set("tasky", PartMap.TASKY, 42, PartMap.TASKY_C);
    static final Set gumpy = new Set("gumpy", PartMap.GUMPY, 16, PartMap.GUMPY_C);
    static TextureRegion blank;

    static Set setFor(Unit unit) {
        return unit instanceof GumpyUnit ? gumpy : tasky;
    }

    static TextureRegion blank() {
        if (blank == null) {
            blank = Core.atlas.find("vintadustry-blank");
        }
        return blank;
    }

    public boolean broken(int n) {
        return this.inited && this.hp[n] <= 1.0E-4f;
    }

    public float sum() {
        float f = 0.0f;
        for (float f2 : this.hp) {
            f += f2;
        }
        return f;
    }

    public void init(Unit unit) {
        if (this.inited) {
            return;
        }
        this.inited = true;
        float f = Mathf.clamp((float)(unit.health / Math.max(unit.maxHealth, 1.0f)));
        for (int i = 0; i < 5; ++i) {
            this.hp[i] = share[i] * unit.maxHealth * f;
        }
    }

    public void write(Writes writes) {
        for (float f : this.hp) {
            writes.f(f);
        }
    }

    public void read(Reads reads, Unit unit, boolean bl) {
        try {
            float[] fArray = new float[5];
            for (int i = 0; i < 5; ++i) {
                fArray[i] = reads.f();
            }
            this.apply(fArray, unit);
        }
        catch (RuntimeException runtimeException) {
            if (bl) {
                throw runtimeException;
            }
            this.inited = false;
        }
    }

    void apply(float[] fArray, Unit unit) {
        for (int i = 0; i < 5; ++i) {
            if (this.inited) {
                if (fArray[i] < this.hp[i] - 0.01f) {
                    this.flash[i] = 1.0f;
                }
                if (fArray[i] <= 1.0E-4f && this.hp[i] > 1.0E-4f && !Vars.headless) {
                    this.breakFx(unit, i);
                }
            }
            this.hp[i] = fArray[i];
        }
        this.inited = true;
    }

    public void damaged(Unit unit, float f) {
        this.pending += f;
        if (this.hitStamp == Time.time) {
            this.resolve(unit, true);
        }
    }

    public void hitAt(Unit unit, float f, float f2) {
        this.hitX = f;
        this.hitY = f2;
        this.hitStamp = Time.time;
        if (this.pending > 0.0f) {
            this.resolve(unit, true);
        }
    }

    void resolve(Unit unit, boolean bl) {
        if (this.pending <= 0.0f) {
            return;
        }
        this.init(unit);
        int n = bl ? this.partAt(unit, this.hitX, this.hitY) : this.randomPart();
        float f = this.pending;
        this.pending = 0.0f;
        this.hit(unit, n, f);
    }

    int randomPart() {
        float f = 0.0f;
        for (int i = 0; i < 5; ++i) {
            if (!(this.hp[i] > 1.0E-4f)) continue;
            f += share[i];
        }
        float f2 = Mathf.random((float)Math.max(f, 1.0E-4f));
        for (int i = 0; i < 5; ++i) {
            if (this.hp[i] <= 1.0E-4f || !((f2 -= share[i]) <= 0.0f)) continue;
            return i;
        }
        return 0;
    }

    int partAt(Unit unit, float f, float f2) {
        Set set = PartHealth.setFor(unit);
        float f3 = (unit.rotation - 90.0f) * ((float)Math.PI / 180);
        float f4 = f - unit.x;
        float f5 = f2 - unit.y;
        float f6 = f4 * Mathf.cos((float)f3) + f5 * Mathf.sin((float)f3);
        float f7 = -f4 * Mathf.sin((float)f3) + f5 * Mathf.cos((float)f3);
        int n = Mathf.clamp((int)((int)Math.floor(f6 + (float)set.n / 2.0f)), (int)0, (int)(set.n - 1));
        int n2 = Mathf.clamp((int)((int)Math.floor((float)set.n / 2.0f - f7)), (int)0, (int)(set.n - 1));
        return set.grid[n2].charAt(n) - 48;
    }

    void hit(Unit unit, int n, float f) {
        if (this.hp[n] <= 1.0E-4f) {
            n = 0;
        }
        this.flash[n] = 1.0f;
        float f2 = Math.min(this.hp[n], f);
        int n2 = n;
        this.hp[n2] = this.hp[n2] - f2;
        f -= f2;
        if (this.hp[n] <= 1.0E-4f) {
            this.hp[n] = 0.0f;
            this.breakFx(unit, n);
        }
        if (f > 0.0f && n != 0) {
            this.flash[0] = 1.0f;
            float f3 = Math.min(this.hp[0], f);
            this.hp[0] = this.hp[0] - f3;
            if (this.hp[0] <= 1.0E-4f) {
                this.hp[0] = 0.0f;
                this.breakFx(unit, 0);
            }
        }
        this.reconcile(unit);
    }

    void reconcile(Unit unit) {
        float f = this.sum();
        if (unit.health > f) {
            unit.health = f;
        }
        if (this.hp[0] <= 1.0E-4f && !unit.dead && !Vars.net.client()) {
            unit.health = 0.0f;
            unit.kill();
        }
    }

    void breakFx(Unit unit, int n) {
        if (Vars.headless || n == 0) {
            return;
        }
        TaskyFx.partBurst(unit, n);
    }

    public void update(Unit unit) {
        float f;
        this.init(unit);
        if (!Vars.net.client()) {
            this.resolve(unit, false);
            float f2 = this.sum();
            if (unit.health > f2 + 0.01f) {
                f = unit.health - f2;
                float f3 = 0.0f;
                for (int i = 0; i < 5; ++i) {
                    if (!(this.hp[i] > 1.0E-4f)) continue;
                    f3 += share[i] * unit.maxHealth - this.hp[i];
                }
                if (f3 > 0.0f) {
                    float f4 = Math.min(1.0f, f / f3);
                    for (int i = 0; i < 5; ++i) {
                        if (!(this.hp[i] > 1.0E-4f)) continue;
                        int n = i;
                        this.hp[n] = this.hp[n] + (share[i] * unit.maxHealth - this.hp[i]) * f4;
                    }
                }
                unit.health = Math.min(unit.health, this.sum());
            }
        }
        for (int i = 0; i < 5; ++i) {
            if (!(this.flash[i] > 0.0f)) continue;
            this.flash[i] = Math.max(0.0f, this.flash[i] - Time.delta * 0.08f);
        }
        if (!Vars.headless) {
            this.smokeT += Time.delta;
            if (this.smokeT >= 6.0f) {
                this.smokeT = 0.0f;
                Set set = PartHealth.setFor(unit);
                f = (unit.rotation - 90.0f) * ((float)Math.PI / 180);
                for (int i = 0; i < 5; ++i) {
                    if (!this.broken(i) || Mathf.chance((double)0.6)) continue;
                    float f5 = set.cent[i][0] + Mathf.range((float)2.0f);
                    float f6 = set.cent[i][1] + Mathf.range((float)2.0f);
                    TaskyFx.smoke.at(unit.x + f5 * Mathf.cos((float)f) - f6 * Mathf.sin((float)f), unit.y + f5 * Mathf.sin((float)f) + f6 * Mathf.cos((float)f));
                }
            }
        }
    }

    public void draw(Unit unit, Set set) {
        float f = unit.rotation - 90.0f;
        float f2 = unit.type.groundLayer + Mathf.clamp((float)(unit.type.hitSize / 4000.0f), (float)0.0f, (float)0.01f);
        for (int i = 0; i < 5; ++i) {
            if (this.broken(i)) continue;
            Draw.z((float)(f2 - 4.0E-4f));
            Draw.color((Color)Color.white);
            if (this.flash[i] > 0.0f) {
                Draw.mixcol((Color)Color.white, (float)(Mathf.clamp((float)this.flash[i]) * 0.85f));
            }
            Draw.rect((TextureRegion)set.part[i], (float)unit.x, (float)unit.y, (float)f);
            Draw.mixcol();
        }
        float f3 = 1.0f;
        for (int i = 0; i < 5; ++i) {
            if (!this.broken(i)) continue;
            Draw.z((float)(f2 + 8.0E-4f));
            Draw.color((float)1.0f, (float)1.0f, (float)1.0f, (float)f3);
            Draw.rect((TextureRegion)set.smoke[i], (float)unit.x, (float)unit.y, (float)f);
        }
        Draw.reset();
    }

    static class Set {
        final String unit;
        final TextureRegion[] part = new TextureRegion[5];
        final TextureRegion[] smoke = new TextureRegion[5];
        final String[] grid;
        final int n;
        final float[][] cent;
        boolean loaded;
        boolean ok;

        Set(String string, String[] stringArray, int n, float[][] fArray) {
            this.unit = string;
            this.grid = stringArray;
            this.n = n;
            this.cent = fArray;
        }

        boolean ready() {
            if (!this.loaded) {
                this.loaded = true;
                this.ok = true;
                for (int i = 0; i < 5; ++i) {
                    this.part[i] = Core.atlas.find("vintadustry-" + this.unit + "-part-" + keys[i]);
                    this.smoke[i] = Core.atlas.find("vintadustry-" + this.unit + "-smoke-" + keys[i]);
                    if (this.part[i].found() && this.smoke[i].found()) continue;
                    this.ok = false;
                }
            }
            return this.ok;
        }
    }
}
