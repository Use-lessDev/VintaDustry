/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.math.Mathf
 *  arc.math.geom.Position
 *  arc.util.Tmp
 *  mindustry.gen.Unit
 */
package vintadustry;

import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Tmp;
import mindustry.gen.Unit;
import vintadustry.DualUnit;

public class Follow {
    public static void step(Unit unit, Unit unit2) {
        float f;
        if (unit2 instanceof DualUnit) {
            DualUnit dualUnit = (DualUnit)unit2;
            f = dualUnit.trailAngle;
        } else {
            f = unit2.rotation;
        }
        float f2 = f;
        float f3 = (unit2.hitSize + unit.hitSize) / 2.0f + (unit2 instanceof DualUnit ? 6.0f : 2.0f);
        Tmp.v1.trns(f2 + 180.0f, f3).add((Position)unit2);
        Tmp.v2.set(Tmp.v1).sub(unit.x, unit.y);
        float f4 = Tmp.v2.len();
        if (f4 > 250.0f) {
            unit.set(Tmp.v1.x, Tmp.v1.y);
            unit.vel.setZero();
            return;
        }
        float f5 = Math.max(f4 - 1.5f, 0.0f);
        Tmp.v3.set(unit2.vel).scl(1.05f);
        Tmp.v2.setLength(f5 * 0.18f).add(Tmp.v3).limit(unit.type.speed + unit2.speed());
        unit.vel.lerpDelta((Position)Tmp.v2, 0.3f);
        if (unit.vel.len() > 0.15f) {
            unit.rotation = Mathf.slerpDelta((float)unit.rotation, (float)unit.vel.angle(), (float)0.12f);
        }
    }
}
