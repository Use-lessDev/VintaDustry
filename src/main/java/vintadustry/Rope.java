/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.math.geom.Position
 *  arc.util.Time
 *  arc.util.Tmp
 *  mindustry.gen.Unit
 */
package vintadustry;

import arc.math.geom.Position;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Unit;
import vintadustry.GumpyShot;

public class Rope {
    public static float stretch(Unit unit, Unit unit2, float f) {
        return Math.max(unit.dst((Position)unit2) - f, 0.0f);
    }

    public static void pull(Unit unit, Unit unit2, float f, float f2, float f3) {
        float f4 = Rope.stretch(unit, unit2, f);
        if (f4 <= 0.0f) {
            return;
        }
        Tmp.v1.set(unit2.x - unit.x, unit2.y - unit.y).nor();
        unit.vel.mulAdd(Tmp.v1, Math.min(f4 * f2, 1.2f) * Time.delta);
        float f5 = -unit.vel.dot(Tmp.v1);
        if (f5 > 0.0f) {
            unit.vel.mulAdd(Tmp.v1, f5 * Math.min(f4 * 0.08f, 0.9f));
        }
        if (f4 > f3) {
            Tmp.v2.set(unit.x - unit2.x, unit.y - unit2.y).setLength(f + f3).add((Position)unit2);
            float f6 = Tmp.v2.x - unit.x;
            float f7 = Tmp.v2.y - unit.y;
            int n = Math.max(1, (int)Math.ceil(Math.sqrt(f6 * f6 + f7 * f7) / 3.0));
            for (int i = 0; i < n; ++i) {
                unit.move(f6 / (float)n, f7 / (float)n);
            }
        }
    }

    public static boolean freeUp(Unit unit) {
        if (!GumpyShot.blocked(unit.x, unit.y)) {
            return true;
        }
        float f = unit.x;
        float f2 = unit.y;
        float f3 = Float.MAX_VALUE;
        boolean bl = false;
        int n = Math.round(unit.x / 8.0f);
        int n2 = Math.round(unit.y / 8.0f);
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                float f4;
                float f5 = (float)(n + i) * 8.0f;
                float f6 = (float)(n2 + j) * 8.0f;
                if (GumpyShot.blocked(f5, f6) || !((f4 = (f5 - unit.x) * (f5 - unit.x) + (f6 - unit.y) * (f6 - unit.y)) < f3)) continue;
                f3 = f4;
                f = f5;
                f2 = f6;
                bl = true;
            }
        }
        if (bl) {
            unit.set(f, f2);
            unit.vel.setZero();
        }
        return bl;
    }
}
