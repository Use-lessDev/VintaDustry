package vintadustry;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import vintadustry.DonaTree;
import vintadustry.PlankerSaw;
import vintadustry.TaskyFx;

public class TreeFx {
    static final Color wood = Color.valueOf((String)"7a4a2a");
    public static Effect snap;
    public static Effect shake;
    public static Effect fall;
    public static Effect land;
    public static Effect chips;
    public static Effect breakDrive;
    public static Effect kick;
    public static Effect topple;
    public static Effect impact;
    public static final float toppleHit = 30.0f;
    public static final float toppleDist = 42.0f;

    public static void load() {
        TaskyFx.load();
        PlankerSaw.create();
        snap = new Effect(46.0f, 80.0f, effectContainer -> {
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Lines.stroke((float)(effectContainer.fout() * 2.0f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(3.0f + effectContainer.finpow() * 12.0f));
            Draw.color((Color)wood);
            Angles.randLenVectors((long)effectContainer.id, (int)9, (float)(3.0f + 20.0f * effectContainer.finpow()), (float)effectContainer.rotation, (float)70.0f, (f, f2) -> {
                float f3 = 1.0f + effectContainer.fout() * 2.0f;
                Fill.rect((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 6.0f * effectContainer.fin() * effectContainer.fin()), (float)(f3 * 1.6f), (float)(f3 * 0.8f), (float)(effectContainer.fin() * 540.0f + Mathf.randomSeed((long)((long)(f * 100.0f)), (float)360.0f)));
            });
            Draw.color((Color)effectContainer.color);
            Angles.randLenVectors((long)(effectContainer.id + 7), (int)8, (float)(4.0f + 18.0f * effectContainer.finpow()), (f, f2) -> {
                float f3 = 22.0f * effectContainer.fin() * effectContainer.fin();
                float f4 = Mathf.sin((float)(effectContainer.time / 5.0f + f)) * 3.0f * effectContainer.fin();
                Fill.rect((float)(effectContainer.x + f + f4), (float)(effectContainer.y + f2 - f3), (float)(2.4f * effectContainer.fout() + 0.4f), (float)(2.4f * effectContainer.fout() + 0.4f), (float)(45.0f + effectContainer.time * 6.0f));
            });
        });
        shake = new Effect(2.0f, effectContainer -> {
            Building building = Vars.world.buildWorld(effectContainer.x, effectContainer.y);
            if (building instanceof DonaTree.DonaTreeBuild) {
                DonaTree.DonaTreeBuild donaTreeBuild = (DonaTree.DonaTreeBuild)building;
                donaTreeBuild.shake = Math.max(donaTreeBuild.shake, effectContainer.rotation <= 0.0f ? 1.0f : effectContainer.rotation);
            }
        });
        fall = new Effect(90.0f, 160.0f, effectContainer -> {
            String[] stringArray;
            float f = effectContainer.fin();
            if (effectContainer.data instanceof String) {
                stringArray = ((String)effectContainer.data).split(";", -1);
            } else {
                String[] stringArray2 = new String[2];
                stringArray2[0] = "";
                stringArray = stringArray2;
                stringArray2[1] = "";
            }
            String[] stringArray3 = stringArray;
            TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find(stringArray3[0]);
            TextureAtlas.AtlasRegion atlasRegion2 = stringArray3.length > 1 && !stringArray3[1].isEmpty() ? Core.atlas.find(stringArray3[1]) : null;
            float f4 = effectContainer.id % 2 == 0 ? 1.0f : -1.0f;
            float f5 = 1.0f - Interp.pow3In.apply(f);
            Draw.z((float)30.2f);
            Draw.alpha((float)(1.0f - f * f));
            if (atlasRegion.found()) {
                Draw.rect((TextureRegion)atlasRegion, (float)(effectContainer.x + Mathf.sin((float)(f * 40.0f), (float)1.0f, (float)1.5f) * (1.0f - f)), (float)effectContainer.y, (float)(64.0f * f5), (float)(64.0f * f5), (float)(f4 * Interp.pow2In.apply(f) * 25.0f));
            }
            if (atlasRegion2 != null && atlasRegion2.found()) {
                Draw.z((float)115.5f);
                float f6 = Interp.pow2In.apply(f);
                Draw.alpha((float)(1.0f - f6));
                Draw.rect((TextureRegion)atlasRegion2, (float)(effectContainer.x + f4 * f6 * 26.0f), (float)(effectContainer.y - f6 * 10.0f), (float)(64.0f * (1.0f - f6 * 0.5f)), (float)(64.0f * (1.0f - f6 * 0.5f)), (float)(f4 * f6 * 80.0f));
            }
            Draw.z((float)110.0f);
            Draw.color((Color)Color.gray, (float)(0.5f * effectContainer.fout()));
            Lines.stroke((float)(3.0f * effectContainer.fout()));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(6.0f + effectContainer.finpow() * 34.0f));
            Draw.color((Color)effectContainer.color);
            Angles.randLenVectors((long)effectContainer.id, (int)22, (float)(6.0f + 46.0f * effectContainer.finpow()), (f2, f3) -> {
                float f4 = 28.0f * f * f;
                Fill.rect((float)(effectContainer.x + f2), (float)(effectContainer.y + f3 - f4), (float)(3.0f * effectContainer.fout() + 0.5f), (float)(3.0f * effectContainer.fout() + 0.5f), (float)(45.0f + effectContainer.time * 5.0f));
            });
            Draw.color((Color)wood);
            Angles.randLenVectors((long)(effectContainer.id + 3), (int)12, (float)(4.0f + 30.0f * effectContainer.finpow()), (f2, f3) -> Fill.rect((float)(effectContainer.x + f2), (float)(effectContainer.y + f3 - 8.0f * f * f), (float)(3.0f * effectContainer.fout() + 0.5f), (float)(1.6f * effectContainer.fout() + 0.4f), (float)(f * 600.0f)));
            Draw.reset();
        });
        breakDrive = new Effect(124.0f, 80.0f, effectContainer -> {
            Building building = Vars.world.buildWorld(effectContainer.x, effectContainer.y);
            if (building instanceof DonaTree.DonaTreeBuild) {
                DonaTree.DonaTreeBuild donaTreeBuild = (DonaTree.DonaTreeBuild)building;
                donaTreeBuild.breakTime = effectContainer.time;
                donaTreeBuild.breakStamp = Time.time;
                donaTreeBuild.breakDir = effectContainer.rotation;
                float f = Mathf.clamp((float)(effectContainer.time / 88.0f));
                donaTreeBuild.shake = Math.max(donaTreeBuild.shake, 0.5f + 1.9f * f * f);
            }
        });
        kick = new Effect(2.0f, effectContainer -> {
            Building building = Vars.world.buildWorld(effectContainer.x, effectContainer.y);
            if (building instanceof DonaTree.DonaTreeBuild) {
                DonaTree.DonaTreeBuild donaTreeBuild = (DonaTree.DonaTreeBuild)building;
                if (donaTreeBuild.lastKick != effectContainer.id) {
                    donaTreeBuild.lastKick = effectContainer.id;
                    donaTreeBuild.kick(effectContainer.rotation, effectContainer.data instanceof Float ? ((Float)effectContainer.data).floatValue() : 1.0f);
                }
            }
        });
        topple = new Effect(125.0f, 200.0f, effectContainer -> {
            float f;
            float f2;
            if (!(effectContainer.data instanceof String)) {
                return;
            }
            String[] stringArray = ((String)effectContainer.data).split(";", -1);
            TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find(stringArray[0]);
            TextureAtlas.AtlasRegion atlasRegion2 = stringArray.length > 1 ? Core.atlas.find(stringArray[1]) : null;
            float f3 = effectContainer.time;
            float f6 = Mathf.cosDeg((float)effectContainer.rotation);
            float f7 = Mathf.sinDeg((float)effectContainer.rotation);
            float f8 = 30.0f;
            float f9 = f3 < f8 ? f3 / f8 * (f3 / f8) : (f3 < f8 + 12.0f ? 1.0f - 0.11f * Mathf.sin((float)((float)Math.PI * (f3 - f8) / 12.0f)) : 1.0f);
            float f10 = Mathf.clamp((float)((effectContainer.lifetime - f3) / 45.0f));
            float f11 = f2 = f6 >= 0.0f ? -1.0f : 1.0f;
            if (atlasRegion.found()) {
                f = f3 < 14.0f ? 1.0f - f3 / 14.0f : 0.0f;
                float f12 = 64.0f * (1.09f + 0.12f * f);
                Draw.z((float)31.0f);
                Draw.color((float)1.0f, (float)1.0f, (float)1.0f, (float)f10);
                Draw.rect((TextureRegion)atlasRegion, (float)(effectContainer.x - f6 * 4.0f * f9), (float)(effectContainer.y - f7 * 4.0f * f9 + 8.0f * f), (float)f12, (float)f12, (float)(f2 * 24.0f * f9));
            }
            if (atlasRegion2 != null && atlasRegion2.found()) {
                f = 64.0f * (1.09f - 0.14f * f9);
                Draw.z((float)(f3 < f8 ? 115.5f : 59.0f));
                Draw.color((float)1.0f, (float)1.0f, (float)1.0f, (float)f10);
                Draw.rect((TextureRegion)atlasRegion2, (float)(effectContainer.x + f6 * 42.0f * f9), (float)(effectContainer.y + f7 * 42.0f * f9 + 7.0f * Mathf.sin((float)((float)Math.PI * Mathf.clamp((float)f9)))), (float)f, (float)f, (float)(f2 * 78.0f * f9));
            }
            if (f3 < 90.0f) {
                f = f3 / 90.0f;
                Draw.z((float)116.0f);
                Draw.color((Color)effectContainer.color);
                Draw.alpha((float)(1.0f - f));
                Angles.randLenVectors((long)effectContainer.id, (int)14, (float)(6.0f + f * 52.0f), (f4, f5) -> {
                    float f6 = Mathf.sin((float)(f * 14.0f + f4)) * 3.0f;
                    float f7 = Mathf.cosDeg((float)effectContainer.rotation) * 42.0f * 0.7f * Mathf.clamp((float)(f3 / f8));
                    float f8 = Mathf.sinDeg((float)effectContainer.rotation) * 42.0f * 0.7f * Mathf.clamp((float)(f3 / f8));
                    Fill.rect((float)(effectContainer.x + f4 + f7 + f6), (float)(effectContainer.y + f5 + f8 - 18.0f * f * f), (float)(2.6f * (1.0f - f) + 0.5f), (float)(2.0f * (1.0f - f) + 0.4f), (float)(f * 400.0f + f4 * 20.0f));
                });
            }
            Draw.reset();
        });
        impact = new Effect(48.0f, 140.0f, effectContainer -> {
            if (effectContainer.time < 2.0f) {
                Effect.shake((float)3.0f, (float)10.0f, (float)effectContainer.x, (float)effectContainer.y);
            }
            Draw.color((Color)Color.valueOf((String)"b59a74"), (float)(effectContainer.fout() * 0.55f));
            Lines.stroke((float)(effectContainer.fout() * 5.0f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.finpow() * 36.0f));
            Draw.color((Color)Color.valueOf((String)"6b4a2c"));
            Angles.randLenVectors((long)effectContainer.id, (int)14, (float)(6.0f + effectContainer.finpow() * 40.0f), (f, f2) -> Fill.rect((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 6.0f * effectContainer.fin() * effectContainer.fin()), (float)(effectContainer.fout() * 3.0f + 0.5f), (float)(effectContainer.fout() * 2.4f + 0.4f), (float)(Mathf.angle((float)f, (float)f2) + effectContainer.fin() * 200.0f)));
            Draw.color((Color)effectContainer.color);
            Angles.randLenVectors((long)(effectContainer.id + 3), (int)16, (float)(8.0f + effectContainer.finpow() * 50.0f), (f, f2) -> Fill.rect((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 8.0f * effectContainer.fin() * effectContainer.fin()), (float)(effectContainer.fout() * 2.6f + 0.4f), (float)(effectContainer.fout() * 2.0f + 0.3f), (float)(effectContainer.fin() * 300.0f + f * 30.0f)));
            Draw.reset();
        });
        chips = new Effect(34.0f, 70.0f, effectContainer -> {
            Draw.color((Color)wood, (Color)Color.valueOf((String)"3b2412"), (float)effectContainer.fin());
            Angles.randLenVectors((long)effectContainer.id, (int)6, (float)(3.0f + effectContainer.finpow() * 22.0f), (f, f2) -> {
                Lines.stroke((float)(effectContainer.fout() * 1.4f + 0.4f));
                Lines.lineAngle((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 5.0f * effectContainer.fin() * effectContainer.fin()), (float)(Mathf.angle((float)f, (float)f2) + effectContainer.fin() * 120.0f), (float)(1.5f + effectContainer.fout() * 3.5f));
            });
            Draw.color((Color)effectContainer.color);
            Angles.randLenVectors((long)(effectContainer.id + 1), (int)6, (float)(2.0f + effectContainer.finpow() * 26.0f), (f, f2) -> Fill.rect((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 8.0f * effectContainer.fin() * effectContainer.fin()), (float)(effectContainer.fout() * 2.4f + 0.4f), (float)(effectContainer.fout() * 1.8f + 0.3f), (float)(effectContainer.fin() * 260.0f)));
            Draw.reset();
        });
        land = new Effect(26.0f, 40.0f, effectContainer -> {
            float f = Math.max(0.4f, effectContainer.rotation);
            Draw.color((Color)Color.valueOf((String)"b8a98a"), (float)(0.5f * effectContainer.fout()));
            Angles.randLenVectors((long)effectContainer.id, (int)5, (float)((2.0f + 7.0f * effectContainer.finpow()) * f), (f2, f3) -> Fill.circle((float)(effectContainer.x + f2), (float)(effectContainer.y + f3), (float)(1.6f * effectContainer.fout() * f + 0.3f)));
            Draw.color((Color)wood);
            Angles.randLenVectors((long)(effectContainer.id + 5), (int)3, (float)((2.0f + 8.0f * effectContainer.finpow()) * f), (f2, f3) -> Fill.rect((float)(effectContainer.x + f2), (float)(effectContainer.y + f3 + 3.0f * Mathf.sin((float)(effectContainer.fin() * (float)Math.PI)) * f), (float)(1.8f * effectContainer.fout() + 0.3f), (float)1.0f, (float)(effectContainer.fin() * 360.0f)));
            Draw.reset();
        });
    }
}
