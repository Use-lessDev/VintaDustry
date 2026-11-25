package vintadustry;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Lines;
import arc.math.Mat;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.util.Log;
import arc.util.Time;
import mindustry.ui.Fonts;
import vintadustry.MenuLobby;

public class LobbyWarning {
    static boolean done;
    static final Color yellow;
    static final Color grayTop;
    static final Color grayBottom;
    static final int pips = 7;
    static int level;

    static boolean on() {
        return Core.settings.getBool("vd-lobby", true);
    }

    public static void setup() {
        if (done) {
            return;
        }
        Group group = MenuLobby.group;
        if (group == null) {
            return;
        }
        done = true;
        Element element = new Element(){
            {
                this.setFillParent(true);
                this.touchable = Touchable.disabled;
            }

            public void draw() {
                if (!LobbyWarning.on()) {
                    return;
                }
                float f = 0.0f;
                float f2 = 0.0f;
                float f3 = Core.graphics.getWidth();
                float f4 = Core.graphics.getHeight();
                Fill.quad((float)f, (float)f2, (float)grayBottom.toFloatBits(), (float)(f + f3), (float)f2, (float)grayBottom.toFloatBits(), (float)(f + f3), (float)(f2 + f4), (float)grayTop.toFloatBits(), (float)f, (float)(f2 + f4), (float)grayTop.toFloatBits());
                float f5 = Time.time / 3.0f;
                Draw.color((float)0.0f, (float)0.0f, (float)0.0f, (float)0.07f);
                for (float f6 = f2 - f5 % 8.0f; f6 < f2 + f4; f6 += 8.0f) {
                    Fill.rect((float)(f + f3 / 2.0f), (float)f6, (float)f3, (float)2.0f);
                }
                Draw.reset();
            }
        };
        element.visible(() -> LobbyWarning.on());
        Element element2 = new Element(){
            final Mat tmp;
            final Mat saved;
            String error;
            {
                this.setFillParent(true);
                this.touchable = Touchable.disabled;
                this.tmp = new Mat();
                this.saved = new Mat();
            }

            boolean inside(float f, float f2, float f3, float f4, float f5, float f6) {
                return f >= f3 && f <= f3 + f5 && f2 >= f4 && f2 <= f4 + f6;
            }

            void corner(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, String string) {
                float f12;
                Draw.color((float)(LobbyWarning.yellow.r * 0.32f), (float)(LobbyWarning.yellow.g * 0.32f), (float)(LobbyWarning.yellow.b * 0.32f), (float)1.0f);
                Fill.tri((float)f, (float)f2, (float)(f + f3 * f5), (float)f2, (float)f, (float)(f2 + f4 * f5));
                Draw.color((float)LobbyWarning.yellow.r, (float)LobbyWarning.yellow.g, (float)LobbyWarning.yellow.b, (float)0.9f);
                Lines.stroke((float)(1.5f * f11));
                Lines.line((float)(f + f3 * f5), (float)f2, (float)f, (float)(f2 + f4 * f5));
                Lines.line((float)(f + f3 * f5 * 0.82f), (float)(f2 + f4 * f5 * 0.0f), (float)(f + f3 * f5 * 0.82f), (float)(f2 + f4 * f5 * 0.0f));
                float f13 = f5 * 0.12f;
                Lines.line((float)(f + f3 * f13), (float)(f2 + f4 * f13), (float)(f + f3 * (f5 - f13 * 2.4f)), (float)(f2 + f4 * f13));
                Lines.line((float)(f + f3 * f13), (float)(f2 + f4 * f13), (float)(f + f3 * f13), (float)(f2 + f4 * (f5 - f13 * 2.4f)));
                Lines.line((float)(f + f3 * (f5 - f13 * 2.4f)), (float)(f2 + f4 * f13), (float)(f + f3 * f13), (float)(f2 + f4 * (f5 - f13 * 2.4f)));
                float f14 = -f3 * f5;
                float f15 = f4 * f5;
                float f16 = Mathf.len((float)f14, (float)f15);
                float f17 = f14 / f16;
                float f18 = f15 / f16;
                float f19 = f3 / 1.4142f;
                float f20 = f4 / 1.4142f;
                float f21 = f + f3 * f5;
                float f22 = f2;
                float f23 = 7.0f * f11;
                float f24 = 8.0f * f11;
                float f25 = 11.0f * f11;
                float f26 = 6.0f * f11;
                float f27 = f16 + 52.0f * f11;
                float f28 = f10 * 0.35f % (f23 + f24);
                Draw.color((float)LobbyWarning.yellow.r, (float)LobbyWarning.yellow.g, (float)LobbyWarning.yellow.b, (float)0.95f);
                for (float f29 = -26.0f * f11 - (f23 + f24) + f28; f29 < f16 + 26.0f * f11; f29 += f23 + f24) {
                    f12 = f29;
                    float f30 = f29 + f23;
                    float f31 = f21 + f17 * f12 + f19 * f26;
                    float f32 = f22 + f18 * f12 + f20 * f26;
                    float f33 = f21 + f17 * f30 + f19 * f26;
                    float f34 = f22 + f18 * f30 + f20 * f26;
                    float f35 = f21 + f17 * f30 + f19 * (f26 + f25) + f17 * f25 * 0.6f;
                    float f36 = f22 + f18 * f30 + f20 * (f26 + f25) + f18 * f25 * 0.6f;
                    float f37 = f21 + f17 * f12 + f19 * (f26 + f25) + f17 * f25 * 0.6f;
                    float f38 = f22 + f18 * f12 + f20 * (f26 + f25) + f18 * f25 * 0.6f;
                    if (!this.inside(f31, f32, f6, f7, f8, f9) || !this.inside(f33, f34, f6, f7, f8, f9) || !this.inside(f35, f36, f6, f7, f8, f9) || !this.inside(f37, f38, f6, f7, f8, f9)) continue;
                    Fill.quad((float)f31, (float)f32, (float)f33, (float)f34, (float)f35, (float)f36, (float)f37, (float)f38);
                }
                if (string != null) {
                    Font font = Fonts.outline;
                    f12 = font.getData().scaleX;
                    font.getData().setScale(Math.max(0.55f * f11 * (f5 / (60.0f * f11)), 0.05f));
                    font.setColor(yellow);
                    font.draw((CharSequence)string, f + f3 * f5 * 0.3f, f2 + f4 * f5 * 0.62f, 1);
                    font.getData().setScale(f12 > 0.001f ? f12 : 1.0f);
                    font.setColor(Color.white);
                }
            }

            void rotText(String string, float f, float f2, float f3, float f4, Color color) {
                Font font = Fonts.outline;
                Draw.flush();
                this.saved.set(Draw.trans());
                this.tmp.setToTranslation(f, f2).rotate(f3);
                Draw.trans((Mat)this.tmp);
                float f5 = font.getData().scaleX;
                font.getData().setScale(f4);
                font.setColor(color);
                font.draw((CharSequence)string, 0.0f, 0.0f, 1);
                font.getData().setScale(f5 > 0.001f ? f5 : 1.0f);
                font.setColor(Color.white);
                Draw.flush();
                Draw.trans((Mat)this.saved);
            }

            void bang(float f, float f2, float f3, float f4, float f5, boolean bl) {
                float f6 = f3 * 0.2f;
                float f7 = f3 * 0.07f;
                float f8 = f3 * 0.7f;
                float f9 = f;
                float f10 = f2 - f3 / 2.0f;
                float f11 = f10 + f3 - f8;
                float f12 = f4 * f3;
                float[] fArray = new float[]{f9 - f6 / 2.0f + f12, f10 + f3, f9 + f6 / 2.0f + f12, f10 + f3, f9 + f7 / 2.0f + f12 * 0.5f, f11, f9 - f7 / 2.0f + f12 * 0.5f, f11};
                if (bl) {
                    Draw.color((float)LobbyWarning.yellow.r, (float)LobbyWarning.yellow.g, (float)LobbyWarning.yellow.b, (float)f5);
                    Lines.stroke((float)2.0f);
                    Lines.line((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
                    Lines.line((float)fArray[2], (float)fArray[3], (float)fArray[4], (float)fArray[5]);
                    Lines.line((float)fArray[4], (float)fArray[5], (float)fArray[6], (float)fArray[7]);
                    Lines.line((float)fArray[6], (float)fArray[7], (float)fArray[0], (float)fArray[1]);
                    Lines.circle((float)(f9 + f12 * 0.2f), (float)(f10 + f3 * 0.07f), (float)(f3 * 0.06f));
                } else {
                    Draw.color((float)LobbyWarning.yellow.r, (float)LobbyWarning.yellow.g, (float)LobbyWarning.yellow.b, (float)f5);
                    Fill.quad((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3], (float)fArray[4], (float)fArray[5], (float)fArray[6], (float)fArray[7]);
                    Fill.circle((float)(f9 + f12 * 0.2f), (float)(f10 + f3 * 0.07f), (float)(f3 * 0.06f));
                }
            }

            public void draw() {
                try {
                    if (this.error == null) {
                        this.drawPanel();
                    }
                }
                catch (Throwable throwable) {
                    StackTraceElement[] stackTraceElementArray = throwable.getStackTrace();
                    this.error = String.valueOf(throwable) + (String)(stackTraceElementArray.length > 0 ? " @ " + String.valueOf(stackTraceElementArray[0]) : "");
                    Draw.reset();
                    Log.err((String)"Lobby warning panel failed", (Throwable)throwable);
                }
                if (this.error != null && LobbyWarning.on()) {
                    Fonts.outline.getData().setScale(0.6f);
                    Fonts.outline.setColor(yellow);
                    Fonts.outline.draw((CharSequence)("TRUMPET PANEL ERROR: " + this.error), 20.0f, (float)Core.graphics.getHeight() - 20.0f, 8);
                    Fonts.outline.getData().setScale(1.0f);
                    Fonts.outline.setColor(Color.white);
                }
            }

            void drawPanel() {
                if (!LobbyWarning.on()) {
                    return;
                }
                float f = 0.0f;
                float f2 = 0.0f;
                float f3 = Core.graphics.getWidth();
                float f4 = Core.graphics.getHeight();
                float f5 = Math.max(Scl.scl((float)1.0f) * 0.8f, 0.6f);
                float f6 = 14.0f * f5;
                float f7 = f + f6;
                float f8 = f2 + f6;
                float f9 = f3 - 2.0f * f6;
                float f10 = f4 - 2.0f * f6;
                float f11 = Time.time;
                float f12 = 0.5f + 0.5f * Mathf.absin((float)f11, (float)70.0f, (float)1.0f);
                float f13 = 0.55f + 0.45f * Mathf.absin((float)f11, (float)20.0f, (float)1.0f);
                Draw.color((float)LobbyWarning.yellow.r, (float)LobbyWarning.yellow.g, (float)LobbyWarning.yellow.b, (float)(0.75f + 0.25f * f12));
                Lines.stroke((float)(1.5f * f5));
                Lines.rect((float)f7, (float)f8, (float)f9, (float)f10);
                float f14 = Math.min(f9, f10);
                float f15 = f14 * 0.3f;
                float f16 = f14 * 0.11f;
                this.corner(f7 + f9, f8 + f10, -1.0f, -1.0f, f15, f7, f8, f9, f10, f11, f5, null);
                this.corner(f7, f8 + f10, 1.0f, -1.0f, f16, f7, f8, f9, f10, f11, f5, "!");
                this.corner(f7, f8, 1.0f, 1.0f, f16, f7, f8, f9, f10, f11, f5, "!");
                this.corner(f7 + f9, f8, -1.0f, 1.0f, f16, f7, f8, f9, f10, f11, f5, "!");
                float f17 = f7 + f9 - f15 * 0.36f;
                float f18 = f8 + f10 - f15 * 0.36f;
                float f19 = Math.max(f15 * 0.0062f, 0.4f);
                float f20 = Fonts.outline.getData().lineHeight * f19 * 0.62f;
                float f21 = 0.7071f * f20;
                float f22 = 0.7071f * f20;
                this.rotText("LEVEL " + level, f17 + f21, f18 + f22, -45.0f, f19, yellow);
                this.rotText("TRUMPET", f17 - f21, f18 - f22, -45.0f, f19, yellow);
                float f23 = f14 * 0.36f;
                float f24 = f7 + f9 / 2.0f;
                float f25 = f8 + f10 / 2.0f;
                if (level <= 0) {
                    this.bang(f24, f25, f23 * (1.0f + 0.04f * f12), 0.0f, 0.35f + 0.65f * f12, true);
                } else {
                    int n = Math.min(level, 3);
                    for (int i = 0; i < n; ++i) {
                        float f26 = ((float)i - (float)(n - 1) / 2.0f) * f23 * 0.38f;
                        float f27 = n == 1 ? 0.0f : ((float)i - (float)(n - 1) / 2.0f) * 0.12f;
                        this.bang(f24 + f26, f25 + (i == (n - 1) / 2 ? f23 * 0.06f : -f23 * 0.02f), f23 * (0.85f + 0.15f * f13), f27, 0.8f + 0.2f * f13, false);
                    }
                }
                Draw.reset();
            }
        };
        element2.visible(() -> LobbyWarning.on());
        if (group.getChildren().size > 0) {
            group.addChildAt(1, element);
        } else {
            group.addChild(element);
        }
        if (group.getChildren().size > 2) {
            group.addChildAt(2, element2);
        } else {
            group.addChild(element2);
        }
    }

    static {
        yellow = Color.valueOf((String)"ffd400");
        grayTop = Color.valueOf((String)"4a4a4a");
        grayBottom = Color.valueOf((String)"262626");
        level = 0;
    }
}
