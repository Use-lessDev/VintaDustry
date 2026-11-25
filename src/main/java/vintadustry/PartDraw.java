/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.math.Mathf
 *  arc.math.geom.Vec2
 *  mindustry.gen.Mechc
 *  mindustry.gen.Unit
 *  mindustry.type.UnitType
 */
package vintadustry;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.gen.Mechc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import vintadustry.PartHealth;

public class PartDraw {
    static final Vec2 off = new Vec2();
    static final Vec2 tmp = new Vec2();
    static final float[] swayOut = new float[2];

    public static float[] sway(Unit unit) {
        PartDraw.swayOut[0] = 0.0f;
        PartDraw.swayOut[1] = 0.0f;
        if (unit instanceof Mechc) {
            Mechc mechc = (Mechc)unit;
            UnitType unitType = unit.type;
            float f = unit.elevation;
            float f2 = mechc.baseRotation();
            float f3 = Mathf.lerp((float)(Mathf.sin((float)mechc.walkExtend(true), (float)0.63661975f, (float)1.0f) * unitType.mechSideSway), (float)0.0f, (float)f);
            float f4 = Mathf.lerp((float)(Mathf.sin((float)mechc.walkExtend(true), (float)0.31830987f, (float)1.0f) * unitType.mechFrontSway), (float)0.0f, (float)f);
            off.trns(f2, 0.0f, f3).add(tmp.trns(f2 + 90.0f, 0.0f, f4));
            PartDraw.swayOut[0] = PartDraw.off.x;
            PartDraw.swayOut[1] = PartDraw.off.y;
        }
        return swayOut;
    }

    public static void draw(PartHealth partHealth, Unit unit, PartHealth.Set set) {
        float f = 0.0f;
        float f2 = 0.0f;
        if (unit instanceof Mechc) {
            Mechc mechc = (Mechc)unit;
            UnitType unitType = unit.type;
            float f3 = unit.elevation;
            float f4 = mechc.baseRotation();
            float f5 = Mathf.lerp((float)(Mathf.sin((float)mechc.walkExtend(true), (float)0.63661975f, (float)1.0f) * unitType.mechSideSway), (float)0.0f, (float)f3);
            float f6 = Mathf.lerp((float)(Mathf.sin((float)mechc.walkExtend(true), (float)0.31830987f, (float)1.0f) * unitType.mechFrontSway), (float)0.0f, (float)f3);
            off.trns(f4, 0.0f, f5).add(tmp.trns(f4 + 90.0f, 0.0f, f6));
            f = PartDraw.off.x;
            f2 = PartDraw.off.y;
        }
        unit.trns(f, f2);
        partHealth.draw(unit, set);
        unit.trns(-f, -f2);
    }
}
