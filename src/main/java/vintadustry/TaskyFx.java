package vintadustry;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import vintadustry.DualUnit;
import vintadustry.GumpyUnit;
import vintadustry.PartHealth;
import vintadustry.TestMod;

public class TaskyFx {
    public static Effect launch;
    public static Effect puff;
    public static Effect land;
    public static Effect impact;
    public static Effect sparkle;
    public static Effect partBreak;
    public static Effect blast;
    public static Effect flame;
    public static Effect smokeColumn;
    public static Effect shard;
    public static Effect debris;
    public static Sound partSound;
    public static Sound boomSound;
    static float lastPartSound;
    static final Color cHot;
    static final Color cYellow;
    static final Color cOrange;
    static final Color cRed;
    static final Color cAsh;
    static final Color cSmokeA;
    static final Color cSmokeB;
    static final Color cMetalA;
    static final Color cMetalB;
    static final Rand rand;
    static TextureRegion shadowRegion;
    public static Effect smoke;
    static final Color dust;

    static Color fireColor(float f) {
        Color color = Tmp.c1;
        if (f < 0.2f) {
            color.set(cHot).lerp(cYellow, f / 0.2f);
        } else if (f < 0.5f) {
            color.set(cYellow).lerp(cOrange, (f - 0.2f) / 0.3f);
        } else if (f < 0.8f) {
            color.set(cOrange).lerp(cRed, (f - 0.5f) / 0.3f);
        } else {
            color.set(cRed).lerp(cAsh, Mathf.clamp((float)((f - 0.8f) / 0.2f)));
        }
        return color;
    }

    static float hash(float f, float f2) {
        float f3 = Math.abs(Mathf.sin((float)(f * 12.9898f + f2 * 78.233f)) * 43758.547f);
        return f3 - (float)Math.floor(f3);
    }

    public static void load() {
        if (!Vars.headless) {
            partSound = Vars.tree.loadSound("vd-part-break");
            boomSound = Vars.tree.loadSound("vd-unit-explode");
        }
        blast = new Effect(68.0f, 460.0f, effectContainer -> {
            float f;
            float f2 = Math.max(effectContainer.rotation, 0.2f);
            float f7 = effectContainer.fin();
            if (effectContainer.time < 1.5f) {
                Effect.shake((float)(2.5f + 5.0f * f2), (float)(12.0f + 14.0f * f2), (float)effectContainer.x, (float)effectContainer.y);
            }
            Drawf.light((float)effectContainer.x, (float)effectContainer.y, (float)((28.0f + 90.0f * f2) * (1.0f - f7 * f7)), (Color)TaskyFx.fireColor(f7 * 0.7f), (float)(0.9f * (1.0f - f7)));
            if (f7 > 0.28f) {
                f = Mathf.clamp((float)((f7 - 0.28f) / 0.72f));
                Angles.randLenVectors((long)(effectContainer.id + 11), (int)8, (float)((14.0f + 40.0f * Interp.pow2Out.apply(f)) * f2), (f3, f4) -> {
                    float f5 = TaskyFx.hash(f3, f4);
                    Draw.color((Color)cSmokeA, (Color)cSmokeB, (float)f);
                    Draw.alpha((float)(0.55f * (1.0f - f) * Math.min(1.0f, f * 6.0f)));
                    Fill.circle((float)(effectContainer.x + f3), (float)(effectContainer.y + f4 + 30.0f * f * f2), (float)((8.0f + 8.0f * f5) * f2 * (0.7f + 0.9f * f)));
                });
            }
            if (f7 < 0.2f) {
                f = f7 / 0.2f;
                Draw.color((Color)Color.white, (Color)cHot, (float)f);
                Draw.alpha((float)(1.0f - 0.5f * f));
                Fill.circle((float)effectContainer.x, (float)effectContainer.y, (float)((7.0f + 22.0f * Interp.pow2Out.apply(f)) * f2));
            }
            Draw.blend((Blending)Blending.additive);
            f = Interp.pow2Out.apply(Mathf.clamp((float)(f7 * 3.2f)));
            float f8 = 1.0f - Interp.pow2In.apply(Mathf.clamp((float)((f7 - 0.32f) / 0.68f)));
            Angles.randLenVectors((long)effectContainer.id, (int)11, (float)((6.0f + 22.0f * Interp.pow3Out.apply(f7)) * f2), (f5, f6) -> {
                float f7 = TaskyFx.hash(f5, f6);
                float f8 = (6.0f + 6.0f * f7) * f2 * f * f8;
                Draw.color((Color)TaskyFx.fireColor(Mathf.clamp((float)(f7 * 1.1f + f7 * 0.25f + 0.1f))));
                Draw.alpha((float)(0.5f * (1.0f - f7 * f7)));
                Fill.circle((float)(effectContainer.x + f5), (float)(effectContainer.y + f6 + 10.0f * f7 * f2), (float)f8);
                Draw.color((Color)TaskyFx.fireColor(Mathf.clamp((float)(f7 * 1.1f + f7 * 0.25f - 0.1f))));
                Draw.alpha((float)(0.8f * (1.0f - f7 * f7)));
                Fill.circle((float)(effectContainer.x + f5), (float)(effectContainer.y + f6 + 10.0f * f7 * f2), (float)(f8 * 0.62f));
            });
            Draw.blend();
            Draw.color((Color)Color.white, (Color)cOrange, (float)f7);
            Draw.alpha((float)(0.55f * (1.0f - f7)));
            Lines.stroke((float)(effectContainer.fout() * effectContainer.fout() * 6.0f * f2 + 0.3f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(Interp.pow3Out.apply(f7) * 56.0f * f2));
            Draw.alpha((float)(0.25f * (1.0f - f7)));
            Lines.stroke((float)(effectContainer.fout() * 2.0f * f2));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(Interp.pow3Out.apply(Mathf.clamp((float)(f7 * 1.2f))) * 40.0f * f2));
            Angles.randLenVectors((long)(effectContainer.id + 21), (int)((int)(16.0f * f2 + 6.0f)), (float)(4.0f + 64.0f * f2 * Interp.pow3Out.apply(f7)), (f3, f4) -> {
                Draw.color((Color)TaskyFx.fireColor(Mathf.clamp((float)(f7 * 1.3f))));
                Lines.stroke((float)(effectContainer.fout() * 2.2f * f2 + 0.3f));
                Lines.lineAngle((float)(effectContainer.x + f3), (float)(effectContainer.y + f4 - 16.0f * f7 * f7 * f2), (float)Mathf.angle((float)f3, (float)f4), (float)(3.0f + effectContainer.fout() * 12.0f * f2));
            });
            Angles.randLenVectors((long)(effectContainer.id + 31), (int)((int)(12.0f * f2 + 4.0f)), (float)(8.0f + 46.0f * f2 * Interp.pow2Out.apply(f7)), (f3, f4) -> {
                float f5 = TaskyFx.hash(f3, f4);
                Draw.color((Color)TaskyFx.fireColor(Mathf.clamp((float)(f7 * 1.2f + f5 * 0.2f))));
                Draw.alpha((float)((0.5f + 0.5f * Mathf.sin((float)(effectContainer.time * 0.9f + f5 * 9.0f))) * effectContainer.fout()));
                float f6 = (1.3f + 1.4f * f5) * f2 * effectContainer.fout() + 0.4f;
                Fill.rect((float)(effectContainer.x + f3), (float)(effectContainer.y + f4 - 30.0f * f7 * f7 * f2), (float)f6, (float)f6, (float)(f7 * 360.0f * f5));
            });
            Draw.reset();
        });
        flame = new Effect(150.0f, 130.0f, effectContainer -> {
            float f = Math.max(effectContainer.rotation, 0.2f);
            float f2 = Mathf.clamp((float)(effectContainer.fout() * 3.0f));
            Draw.blend((Blending)Blending.additive);
            Angles.randLenVectors((long)effectContainer.id, (int)7, (float)(16.0f * f), (f3, f4) -> {
                float f5 = TaskyFx.hash(f3, f4);
                float f6 = (effectContainer.time * 0.06f + f5 * 3.0f) % 1.0f;
                Draw.color((Color)TaskyFx.fireColor(f6 * 0.75f));
                Draw.alpha((float)(0.75f * f2));
                Fill.circle((float)(effectContainer.x + f3 + Mathf.sin((float)(effectContainer.time * 0.2f + f5 * 6.0f)) * 1.5f * f), (float)(effectContainer.y + f4 + f6 * 16.0f * f), (float)((3.5f + 3.0f * f5) * f * (1.0f - f6) * f2 + 0.2f));
            });
            Draw.blend();
            Drawf.light((float)effectContainer.x, (float)effectContainer.y, (float)(30.0f * f), (Color)TaskyFx.fireColor(0.3f), (float)(0.35f * effectContainer.fout()));
            Draw.reset();
        });
        smokeColumn = new Effect(200.0f, 200.0f, effectContainer -> {
            float f = Math.max(effectContainer.rotation, 0.2f);
            Angles.randLenVectors((long)effectContainer.id, (int)6, (float)(10.0f * f), (f2, f3) -> {
                float f4 = TaskyFx.hash(f2, f3);
                float f5 = (effectContainer.time * 0.011f + f4) % 1.0f;
                Draw.color((Color)cSmokeA, (Color)cSmokeB, (float)f5);
                Draw.alpha((float)(0.5f * (1.0f - f5) * Mathf.clamp((float)(effectContainer.fout() * 3.0f)) * Math.min(1.0f, f5 * 8.0f)));
                Fill.circle((float)(effectContainer.x + f2 + f5 * 16.0f * f), (float)(effectContainer.y + f3 + f5 * 70.0f * f), (float)((5.0f + 11.0f * f5) * f));
            });
            Draw.reset();
        });
        shard = new Effect(72.0f, 200.0f, effectContainer -> {
            float f = Math.max(effectContainer.rotation, 0.2f);
            float f2 = effectContainer.fin();
            Angles.randLenVectors((long)effectContainer.id, (int)((int)(9.0f * f + 4.0f)), (float)((14.0f + 90.0f * f) * Interp.pow2Out.apply(f2)), (f3, f4) -> {
                float f5 = TaskyFx.hash(f3, f4);
                float f6 = Mathf.angle((float)f3, (float)f4) + f2 * (260.0f + 200.0f * f5) * (f5 > 0.5f ? 1.0f : -1.0f);
                Draw.color((Color)cHot, (Color)cOrange, (float)Mathf.clamp((float)(f2 * 4.0f)));
                if (f2 > 0.25f) {
                    Draw.color((Color)cOrange, (Color)(f5 > 0.5f ? cMetalA : cMetalB), (float)Mathf.clamp((float)((f2 - 0.25f) * 3.0f)));
                }
                Draw.alpha((float)Mathf.clamp((float)(effectContainer.fout() * 2.5f)));
                Fill.poly((float)(effectContainer.x + f3), (float)(effectContainer.y + f4 - 22.0f * f2 * f2), (int)3, (float)((1.6f + 2.2f * f5) * f * Math.max(0.35f, effectContainer.fout())), (float)f6);
            });
            Draw.reset();
        });
        debris = new Effect(115.0f, 300.0f, effectContainer -> {
            if (!(effectContainer.data instanceof DebrisData)) {
                return;
            }
            DebrisData debrisData = (DebrisData)effectContainer.data;
            PartHealth.Set set = debrisData.kind == 0 ? PartHealth.tasky : PartHealth.gumpy;
            TextureRegion textureRegion = set.part[debrisData.part];
            if (textureRegion == null || !textureRegion.found()) {
                return;
            }
            if (shadowRegion == null) {
                shadowRegion = Core.atlas.find("circle-shadow");
            }
            float f = effectContainer.time;
            float f2 = 0.045f;
            float f3 = (1.0f - (float)Math.exp(-f2 * f)) / f2;
            float f4 = debrisData.x0 + debrisData.vx * f3;
            float f5 = debrisData.y0 + debrisData.vy * f3;
            float f6 = TaskyFx.debrisHeight(debrisData, f);
            float f7 = debrisData.rot0 + debrisData.spin * (1.0f - (float)Math.exp(-0.05f * f));
            float f8 = f7 * ((float)Math.PI / 180);
            float f9 = debrisData.cx * Mathf.cos((float)f8) - debrisData.cy * Mathf.sin((float)f8);
            float f10 = debrisData.cx * Mathf.sin((float)f8) + debrisData.cy * Mathf.cos((float)f8);
            float f11 = Mathf.clamp((float)(effectContainer.fout() * 3.2f));
            float f12 = (float)textureRegion.width / 4.0f;
            float f13 = (float)textureRegion.height / 4.0f;
            for (int i = 1; i <= 5; ++i) {
                float f14 = f - (float)i * 3.0f;
                if (f14 < 0.0f || f14 > 60.0f) continue;
                float f15 = (1.0f - (float)Math.exp(-f2 * f14)) / f2;
                Draw.z((float)109.98f);
                Draw.color((Color)cSmokeA, (Color)cSmokeB, (float)((float)i / 5.0f));
                Draw.alpha((float)(0.4f * (1.0f - (float)i / 6.0f) * f11));
                Fill.circle((float)(debrisData.x0 + debrisData.vx * f15), (float)(debrisData.y0 + debrisData.vy * f15 + TaskyFx.debrisHeight(debrisData, f14)), (float)(2.2f + (float)i * 0.7f));
            }
            Draw.z((float)109.97f);
            Draw.color((float)0.0f, (float)0.0f, (float)0.0f, (float)(0.3f * f11 * (1.0f - Mathf.clamp((float)(f6 / 70.0f)) * 0.6f)));
            Draw.rect((TextureRegion)shadowRegion, (float)f4, (float)f5, (float)(Math.min(f12, f13) * 0.55f), (float)(Math.min(f12, f13) * 0.32f));
            float f16 = 1.0f + Mathf.clamp((float)(f6 / 80.0f)) * 0.25f;
            Draw.z((float)(f6 > 2.0f ? 115.0f : 60.5f));
            Draw.color((Color)Color.white, (Color)Color.valueOf((String)"5c5c66"), (float)Mathf.clamp((float)(f / 55.0f)));
            Draw.alpha((float)f11);
            Draw.mixcol((Color)cOrange, (float)(Mathf.clamp((float)(1.0f - f / 22.0f)) * 0.8f));
            Draw.rect((TextureRegion)textureRegion, (float)(f4 - f9), (float)(f5 + f6 - f10), (float)(f12 * f16), (float)(f13 * f16), (float)f7);
            Draw.mixcol();
            Draw.reset();
        });
        launch = new Effect(36.0f, 170.0f, effectContainer -> {
            if (effectContainer.time < 2.0f) {
                Effect.shake((float)1.2f, (float)5.0f, (float)effectContainer.x, (float)effectContainer.y);
            }
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Fill.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.fout() * effectContainer.fout() * 7.0f));
            Draw.color((Color)effectContainer.color, (float)effectContainer.fout());
            Lines.stroke((float)(effectContainer.fout() * 4.0f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.finpow() * 20.0f));
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Angles.randLenVectors((long)effectContainer.id, (int)12, (float)(4.0f + effectContainer.finpow() * 60.0f), (float)effectContainer.rotation, (float)26.0f, (f, f2) -> {
                Lines.stroke((float)(effectContainer.fout() * 2.0f + 0.3f));
                Lines.lineAngle((float)(effectContainer.x + f), (float)(effectContainer.y + f2), (float)Mathf.angle((float)f, (float)f2), (float)(effectContainer.fout() * 7.0f + 1.0f));
            });
            Draw.color((Color)Color.gray, (float)(0.45f * effectContainer.fout()));
            Angles.randLenVectors((long)(effectContainer.id + 1), (int)6, (float)(4.0f + effectContainer.finpow() * 32.0f), (float)effectContainer.rotation, (float)40.0f, (f, f2) -> Fill.circle((float)(effectContainer.x + f), (float)(effectContainer.y + f2), (float)(effectContainer.fout() * 4.0f + 0.5f)));
            Draw.reset();
        });
        puff = new Effect(24.0f, 40.0f, effectContainer -> {
            Draw.color((Color)effectContainer.color, (Color)Color.white, (float)(0.35f * effectContainer.fout()));
            Draw.alpha((float)(0.55f * effectContainer.fout()));
            Fill.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.fout() * 3.4f + 0.3f));
            Draw.color((Color)Color.white, (float)(effectContainer.fout() * 0.6f));
            Fill.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.fout() * 1.4f));
            Draw.reset();
        });
        land = new Effect(52.0f, 150.0f, effectContainer -> {
            if (effectContainer.time < 2.0f) {
                Effect.shake((float)1.6f, (float)6.0f, (float)effectContainer.x, (float)effectContainer.y);
            }
            Draw.color((Color)dust, (float)(effectContainer.fout() * 0.55f));
            Lines.stroke((float)(effectContainer.fout() * 5.0f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.finpow() * 36.0f));
            Draw.color((Color)effectContainer.color, (float)effectContainer.fout());
            Lines.stroke((float)(effectContainer.fout() * 3.0f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.finpow() * 22.0f));
            Draw.color((Color)dust, (Color)Color.valueOf((String)"6b4a2c"), (float)effectContainer.fin());
            Angles.randLenVectors((long)effectContainer.id, (int)14, (float)(6.0f + effectContainer.finpow() * 42.0f), (f, f2) -> Fill.rect((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 5.0f * effectContainer.fin() * effectContainer.fin()), (float)(effectContainer.fout() * 3.0f + 0.5f), (float)(effectContainer.fout() * 2.4f + 0.4f), (float)(Mathf.angle((float)f, (float)f2) + effectContainer.fin() * 200.0f)));
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Angles.randLenVectors((long)(effectContainer.id + 5), (int)10, (float)(4.0f + effectContainer.finpow() * 46.0f), (f, f2) -> {
                Lines.stroke((float)(effectContainer.fout() * 1.6f + 0.2f));
                Lines.lineAngle((float)(effectContainer.x + f), (float)(effectContainer.y + f2), (float)Mathf.angle((float)f, (float)f2), (float)(effectContainer.fout() * 6.0f + 1.0f));
            });
            Draw.reset();
        });
        impact = new Effect(24.0f, 80.0f, effectContainer -> {
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Fill.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.fout() * 7.0f));
            Lines.stroke((float)(effectContainer.fout() * 2.5f));
            Lines.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.finpow() * 16.0f));
            Angles.randLenVectors((long)effectContainer.id, (int)8, (float)(3.0f + effectContainer.finpow() * 28.0f), (f, f2) -> {
                Lines.stroke((float)(effectContainer.fout() * 1.6f + 0.2f));
                Lines.lineAngle((float)(effectContainer.x + f), (float)(effectContainer.y + f2), (float)Mathf.angle((float)f, (float)f2), (float)(effectContainer.fout() * 5.0f + 1.0f));
            });
            Draw.reset();
        });
        partBreak = new Effect(46.0f, 120.0f, effectContainer -> {
            if (effectContainer.time < 2.0f) {
                Effect.shake((float)2.0f, (float)7.0f, (float)effectContainer.x, (float)effectContainer.y);
            }
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Fill.circle((float)effectContainer.x, (float)effectContainer.y, (float)(effectContainer.fout() * effectContainer.fout() * 8.0f));
            Draw.color((Color)Color.valueOf((String)"5b5f6e"), (Color)Color.valueOf((String)"2a2c36"), (float)effectContainer.fin());
            Angles.randLenVectors((long)effectContainer.id, (int)9, (float)(3.0f + effectContainer.finpow() * 30.0f), (f, f2) -> Fill.rect((float)(effectContainer.x + f), (float)(effectContainer.y + f2 - 4.0f * effectContainer.fin() * effectContainer.fin()), (float)(effectContainer.fout() * 3.2f + 0.5f), (float)(effectContainer.fout() * 2.4f + 0.4f), (float)(Mathf.angle((float)f, (float)f2) + effectContainer.fin() * 240.0f)));
            Draw.color((Color)Color.white, (Color)effectContainer.color, (float)effectContainer.fin());
            Angles.randLenVectors((long)(effectContainer.id + 3), (int)8, (float)(2.0f + effectContainer.finpow() * 34.0f), (f, f2) -> {
                Lines.stroke((float)(effectContainer.fout() * 1.5f + 0.2f));
                Lines.lineAngle((float)(effectContainer.x + f), (float)(effectContainer.y + f2), (float)Mathf.angle((float)f, (float)f2), (float)(effectContainer.fout() * 5.0f + 1.0f));
            });
            Draw.color((Color)Color.gray, (float)(0.5f * effectContainer.fout()));
            Angles.randLenVectors((long)(effectContainer.id + 7), (int)5, (float)(2.0f + effectContainer.finpow() * 16.0f), (f, f2) -> Fill.circle((float)(effectContainer.x + f), (float)(effectContainer.y + f2 + 8.0f * effectContainer.fin()), (float)(effectContainer.fout() * 4.5f + 0.5f)));
            Draw.reset();
        });
        smoke = new Effect(40.0f, 40.0f, effectContainer -> {
            Draw.color((Color)Color.valueOf((String)"30323c"), (Color)Color.valueOf((String)"5b5f6e"), (float)effectContainer.fin());
            Draw.alpha((float)(0.55f * effectContainer.fout()));
            Fill.circle((float)(effectContainer.x + Mathf.sin((float)(effectContainer.fin() * 6.0f + (float)effectContainer.id)) * 1.5f), (float)(effectContainer.y + effectContainer.finpow() * 9.0f), (float)(1.2f + effectContainer.fin() * 3.2f));
            Draw.reset();
        });
        sparkle = new Effect(26.0f, 70.0f, effectContainer -> {
            Draw.color((Color)effectContainer.color, (Color)Color.white, (float)effectContainer.fin());
            Angles.randLenVectors((long)effectContainer.id, (int)3, (float)(3.0f + effectContainer.finpow() * 18.0f), (float)effectContainer.rotation, (float)55.0f, (f, f2) -> {
                Lines.stroke((float)(effectContainer.fout() * 1.5f + 0.2f));
                Lines.lineAngle((float)(effectContainer.x + f), (float)(effectContainer.y + f2), (float)Mathf.angle((float)f, (float)f2), (float)(effectContainer.fout() * 4.0f + 0.5f));
            });
            Draw.reset();
        });
    }

    static float debrisHeight(DebrisData debrisData, float f) {
        float f2 = 0.22f;
        float f3 = 2.0f * debrisData.vz / f2;
        if (f < f3) {
            return debrisData.vz * f - 0.5f * f2 * f * f;
        }
        float f4 = f - f3;
        float f5 = debrisData.vz * 0.38f;
        float f6 = 2.0f * f5 / f2;
        return f4 < f6 ? f5 * f4 - 0.5f * f2 * f4 * f4 : 0.0f;
    }

    static void playPart(float f, float f2) {
        if (partSound == null || Time.time - lastPartSound < 5.0f) {
            return;
        }
        lastPartSound = Time.time;
        partSound.at(f, f2, Mathf.random((float)0.92f, (float)1.08f), 1.0f);
    }

    static void throwPart(Unit unit, int n, float f) {
        PartHealth.Set set = PartHealth.setFor(unit);
        if (set.part[n] == null || !set.part[n].found()) {
            return;
        }
        float f2 = (unit.rotation - 90.0f) * ((float)Math.PI / 180);
        float f3 = set.cent[n][0];
        float f4 = set.cent[n][1];
        DebrisData debrisData = new DebrisData();
        debrisData.kind = unit instanceof GumpyUnit ? 1 : 0;
        debrisData.part = n;
        debrisData.rot0 = unit.rotation - 90.0f;
        debrisData.cx = f3;
        debrisData.cy = f4;
        debrisData.x0 = unit.x + f3 * Mathf.cos((float)f2) - f4 * Mathf.sin((float)f2);
        debrisData.y0 = unit.y + f3 * Mathf.sin((float)f2) + f4 * Mathf.cos((float)f2);
        float f5 = n == 0 ? Mathf.random((float)360.0f) : Mathf.angle((float)(debrisData.x0 - unit.x), (float)(debrisData.y0 - unit.y)) + Mathf.range((float)28.0f);
        float f6 = f * Mathf.random((float)0.8f, (float)1.25f);
        debrisData.vx = Mathf.cosDeg((float)f5) * f6;
        debrisData.vy = Mathf.sinDeg((float)f5) * f6;
        debrisData.vz = Mathf.random((float)2.3f, (float)3.4f);
        debrisData.spin = (Mathf.chance((double)0.5) ? 1.0f : -1.0f) * Mathf.random((float)180.0f, (float)540.0f);
        debris.at(debrisData.x0, debrisData.y0, 0.0f, unit.team.color, (Object)debrisData);
    }

    public static void partBurst(Unit unit, int n) {
        PartHealth.Set set = PartHealth.setFor(unit);
        float f = (unit.rotation - 90.0f) * ((float)Math.PI / 180);
        float f2 = unit.x + set.cent[n][0] * Mathf.cos((float)f) - set.cent[n][1] * Mathf.sin((float)f);
        float f3 = unit.y + set.cent[n][0] * Mathf.sin((float)f) + set.cent[n][1] * Mathf.cos((float)f);
        boolean bl = unit instanceof GumpyUnit;
        blast.at(f2, f3, bl ? 0.5f : 0.8f, unit.team.color);
        shard.at(f2, f3, bl ? 0.45f : 0.7f, unit.team.color);
        TaskyFx.throwPart(unit, n, bl ? 1.0f : 1.6f);
        TaskyFx.playPart(f2, f3);
    }

    public static void unitDeath(DualUnit dualUnit, boolean bl) {
        int n;
        float f = bl ? 0.55f : 1.1f;
        float f2 = Math.max(dualUnit.hitSize * 0.45f, 6.0f);
        float f3 = dualUnit.x;
        float f4 = dualUnit.y;
        Color color = dualUnit.team.color;
        blast.at(f3, f4, f * 0.8f, color);
        int n2 = bl ? 3 : 6;
        for (n = 0; n < n2; ++n) {
            float f5 = f3 + Mathf.range((float)f2);
            float f6 = f4 + Mathf.range((float)f2);
            float f7 = f * Mathf.random((float)0.45f, (float)0.75f);
            Time.run((float)(4.0f + (float)n * 4.5f), () -> blast.at(f5, f6, f7, color));
        }
        Time.run((float)(bl ? 6.0f : 11.0f), () -> {
            blast.at(f3, f4, f * 1.45f, color);
            shard.at(f3, f4, f * 1.3f, color);
            Effect.scorch((float)f3, (float)f4, (int)(bl ? 1 : 3));
        });
        for (n = 0; n < 5; ++n) {
            if (dualUnit.parts.broken(n)) continue;
            int n3 = n;
            Time.run((float)(bl ? 5.0f : 8.0f), () -> TaskyFx.throwPart((Unit)dualUnit, n3, bl ? 1.5f : 2.4f));
        }
        Time.run((float)14.0f, () -> {
            flame.at(f3 + Mathf.range((float)(f2 * 0.5f)), f4 + Mathf.range((float)(f2 * 0.5f)), f * 0.9f, color);
            smokeColumn.at(f3, f4, f, color);
        });
        if (!bl) {
            Time.run((float)20.0f, () -> flame.at(f3 + Mathf.range((float)f2), f4 + Mathf.range((float)f2), f * 0.7f, color));
            Time.run((float)26.0f, () -> smokeColumn.at(f3 + Mathf.range((float)(f2 * 0.5f)), f4, f * 0.8f, color));
        }
        if (!bl && boomSound != null) {
            boomSound.at(f3, f4, 1.0f, 1.0f);
        } else if (bl && partSound != null) {
            partSound.at(f3, f4, 0.95f, 1.0f);
        }
    }

    public static void drawCharge(DualUnit dualUnit) {
        float f;
        float f2;
        float f3;
        float f4 = dualUnit.gumpyProgress;
        if (f4 <= 0.001f) {
            return;
        }
        dualUnit.mouthPos(Tmp.v2);
        float f5 = Tmp.v2.x;
        float f6 = Tmp.v2.y;
        Color color = dualUnit.team.color;
        float f7 = Time.time;
        float f8 = dualUnit.rotation - 90.0f;
        float f9 = f5;
        float f10 = f6;
        Color color2 = color;
        float f11 = f4;
        Draw.draw((float)59.98f, () -> Drawf.construct((float)f9, (float)f10, (TextureRegion)TestMod.gumpy.region, (Color)color2, (float)f8, (float)f11, (float)1.0f, (float)Time.time));
        Draw.z((float)100.0f);
        Draw.blend((Blending)Blending.additive);
        Draw.color((Color)color, (float)(0.06f + 0.1f * f4));
        Fill.circle((float)f5, (float)f6, (float)(5.0f + 5.0f * f4));
        Draw.blend();
        Draw.color((Color)Color.black, (float)0.18f);
        Lines.stroke((float)2.4f);
        Lines.circle((float)f5, (float)f6, (float)11.0f);
        Draw.color((Color)color, (float)0.3f);
        Lines.stroke((float)1.4f);
        Lines.circle((float)f5, (float)f6, (float)11.0f);
        Draw.color((Color)(f4 >= 0.999f ? Color.white : color), (float)0.9f);
        Lines.stroke((float)1.6f);
        Lines.arc((float)f5, (float)f6, (float)11.0f, (float)f4, (float)90.0f);
        if (f4 > 0.3f && f4 < 0.999f) {
            for (int i = 0; i < 3; ++i) {
                f3 = (f7 * 0.02f + (float)i / 3.0f) % 1.0f;
                f2 = 5.0f + 14.0f * (1.0f - f3);
                f = (float)i * 120.0f + f3 * 90.0f;
                Draw.color((Color)color, (float)(0.5f * f3));
                Fill.rect((float)(f5 + Angles.trnsx((float)f, (float)f2)), (float)(f6 + Angles.trnsy((float)f, (float)f2)), (float)1.4f, (float)1.4f, (float)f);
            }
        }
        float f12 = dualUnit.aimAngle();
        if (f4 > 0.5f && f12 >= 0.0f) {
            f3 = 150.0f;
            f2 = 0.3f + 0.4f * f4;
            Draw.color((Color)color, (float)f2);
            for (f = 18.0f; f < f3 - 6.0f; f += 12.0f) {
                Fill.rect((float)(f5 + Angles.trnsx((float)f12, (float)f)), (float)(f6 + Angles.trnsy((float)f12, (float)f)), (float)1.7f, (float)1.7f, (float)(f12 + 45.0f));
            }
            Lines.stroke((float)1.2f);
            Lines.circle((float)(f5 + Angles.trnsx((float)f12, (float)f3)), (float)(f6 + Angles.trnsy((float)f12, (float)f3)), (float)(4.0f + Mathf.absin((float)f7, (float)5.0f, (float)1.2f)));
        }
        Draw.reset();
    }

    static {
        lastPartSound = -999.0f;
        cHot = Color.valueOf((String)"fff3c0");
        cYellow = Color.valueOf((String)"ffd04a");
        cOrange = Color.valueOf((String)"ff8424");
        cRed = Color.valueOf((String)"d23a18");
        cAsh = Color.valueOf((String)"33333a");
        cSmokeA = Color.valueOf((String)"2a2a31");
        cSmokeB = Color.valueOf((String)"63636c");
        cMetalA = Color.valueOf((String)"a9aab6");
        cMetalB = Color.valueOf((String)"4a4b55");
        rand = new Rand();
        dust = Color.valueOf((String)"b59a74");
    }

    public static class DebrisData {
        int kind;
        int part;
        float x0;
        float y0;
        float rot0;
        float cx;
        float cy;
        float vx;
        float vy;
        float vz;
        float spin;
    }
}
