package betterUpgradePreview.patches;

import basemod.ReflectionHacks;
import betterUpgradePreview.ModSettings;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class CardTextRenderPatches {
    @SuppressWarnings("LibGDXStaticResource")
    public static final Texture whitePixel;
    public static final TextureRegion strikethrough;

    static {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pm.setColor(0xffffffff);
        pm.drawPixel(0, 0);
        whitePixel = new Texture(pm);
        whitePixel.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        strikethrough = new TextureRegion(whitePixel);
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "initializeDescription"
    )
    public static class FixDiffMarkerSizePatch {
        @SpireInsertPatch(
                locator = FixDiffMarkerSizePatchLocator.class,
                localvars = {"gl", "word"}
        )
        public static void Insert(AbstractCard _instance, @ByRef GlyphLayout[] gl, String word) {
            if (word.length() > 0 && word.charAt(0) == '[') {
                if (word.equals("[DiffAddS]") ||
                        word.equals("[DiffAddE]") ||
                        word.equals("[DiffRmvS]") ||
                        word.equals("[DiffRmvE]")
                ) {
                    gl[0].setText(FontHelper.cardDescFont_N, "");
                    gl[0].width = 0;
                }
            }
        }
    }

    public static class FixDiffMarkerSizePatchLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "DESC_BOX_WIDTH");
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }

    public static class AlterDescriptionRenderingPatchLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(GlyphLayout.class, "setText");
            int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
            return new int[]{lines[lines.length - 1]}; // Only last occurrence
        }
    }

    public static class AlterDescriptionRenderingPatchLocator2 extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(GlyphLayout.class, "width");
            int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
            return new int[]{lines[1], lines[lines.length - 1]}; // Only 2nd and last occurrence
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "renderDescription"
    )
    public static class AlterDescriptionRenderingPatch {
        private static Color original = null;

        @SpireInsertPatch(
                locator = AlterDescriptionRenderingPatchLocator.class,
                localvars = {"tmp"}
        )
        public static void Insert(AbstractCard _instance, SpriteBatch sb, @ByRef String[] tmp) {
            if (tmp[0].length() > 0 && tmp[0].charAt(0) == '[') {
                if (tmp[0].equals("[DiffAddS] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffAdd.set(_instance, true);
                } else if (tmp[0].equals("[DiffAddE] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffAdd.set(_instance, false);
                } else if (tmp[0].equals("[DiffRmvS] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffRmv.set(_instance, true);
                } else if (tmp[0].equals("[DiffRmvE] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffRmv.set(_instance, false);
                }
            }
        }

        @SpireInsertPatch(
                locator = AlterDescriptionRenderingPatchLocator2.class,
                localvars = {"font", "tmp", "i", "start_x", "gl", "draw_y", "spacing"}
        )
        public static void Strikethrough(AbstractCard _instance,
                                         SpriteBatch sb,
                                         BitmapFont font,
                                         String tmp,
                                         int i,
                                         float start_x,
                                         GlyphLayout gl,
                                         float draw_y,
                                         float spacing) {
            if (AbstractCardFields.isInDiffRmv.get(_instance)) {
                float w = gl.width;
                Color original = sb.getColor();
                sb.setColor(ModSettings.removeColor);
                sb.draw(strikethrough,
                        start_x,
                        draw_y - i * font.getCapHeight() * 1.45f - 9f * _instance.drawScale * Settings.scale,
                        w,
                        2f * _instance.drawScale * Settings.scale
                );
                sb.setColor(original);
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) &&
                            m.getMethodName().equals("renderRotatedText")) {
                        m.replace("{ $10 = " +
                                CardTextRenderPatches.class.getName() +
                                ".GetColor(this); $_ = $proceed($$); }");
                    }
                }
            };
        }
    }

    public static Color GetColor(AbstractCard _instance) {
        if (AbstractCardFields.isInDiffAdd.get(_instance)) {
            return ModSettings.addColor;
        } else if (AbstractCardFields.isInDiffRmv.get(_instance)) {
            return ModSettings.removeColor;
        } else {
            return ReflectionHacks.getPrivate(_instance, AbstractCard.class, "textColor");
        }
    }

    public static Color GetColorSCVP(SingleCardViewPopup _instance) {
        AbstractCard card = ReflectionHacks.getPrivate(_instance, SingleCardViewPopup.class, "card");
        if (AbstractCardFields.isInDiffAdd.get(card)) {
            return ModSettings.addColor;
        } else if (AbstractCardFields.isInDiffRmv.get(card)) {
            return ModSettings.removeColor;
        } else {
            return Settings.CREAM_COLOR;
        }
    }

    @SpirePatch(
            clz = SingleCardViewPopup.class,
            method = "renderDescription"
    )
    public static class AlterBigDescriptionRenderingPatch {

        @SpireInstrumentPatch
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) &&
                            m.getMethodName().equals("renderRotatedText")) {
                        m.replace("{ $10 = " +
                                CardTextRenderPatches.class.getName() +
                                ".GetColorSCVP(this); $_ = $proceed($$); }");
                    }
                }
            };
        }

        @SpireInsertPatch(
                locator = AlterBigDescriptionRenderingPatchLocator2.class,
                localvars = {"font", "tmp", "i", "start_x", "gl", "draw_y"}
        )
        public static void Strikethrough(SingleCardViewPopup _instance,
                                         SpriteBatch sb,
                                         float ___current_x,
                                         float ___current_y,
                                         AbstractCard ___card,
                                         BitmapFont font,
                                         String tmp,
                                         int i,
                                         float start_x,
                                         GlyphLayout gl,
                                         float draw_y) {
            if (AbstractCardFields.isInDiffRmv.get(___card)) {
                float w = gl.width;
                Color original = sb.getColor();
                sb.setColor(ModSettings.removeColor);
                sb.draw(strikethrough,
                        start_x,
                        draw_y - i * font.getCapHeight() * 1.53f - 24f * ___card.drawScale * Settings.scale,
                        w,
                        4f * ___card.drawScale * Settings.scale
                );
                sb.setColor(original);
            }
        }

        @SpireInsertPatch(
                locator = AlterBigDescriptionRenderingPatchLocator.class,
                localvars = {"tmp"}
        )
        public static void Insert(SingleCardViewPopup _instance, SpriteBatch sb, @ByRef String[] tmp) {
            AbstractCard card = ReflectionHacks.getPrivate(_instance, SingleCardViewPopup.class, "card");
            if (tmp[0].length() > 0 && tmp[0].charAt(0) == '[') {
                if (tmp[0].equals("[DiffAddS] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffAdd.set(card, true);
                } else if (tmp[0].equals("[DiffAddE] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffAdd.set(card, false);
                } else if (tmp[0].equals("[DiffRmvS] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffRmv.set(card, true);
                } else if (tmp[0].equals("[DiffRmvE] ")) {
                    tmp[0] = "";
                    AbstractCardFields.isInDiffRmv.set(card, false);
                }
            }
        }
    }

    public static class AlterBigDescriptionRenderingPatchLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(GlyphLayout.class, "setText");
            int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
            return new int[]{lines[lines.length - 1]}; // Only last occurrence
        }
    }

    public static class AlterBigDescriptionRenderingPatchLocator2 extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.FieldAccessMatcher(GlyphLayout.class, "width");
            int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
            return new int[]{lines[1], lines[lines.length - 1]}; // Only 2nd and last occurrence
        }
    }

}
