package betterUpgradePreview.patches;

import basemod.ReflectionHacks;
import betterUpgradePreview.ModSettings;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import javassist.CtBehavior;

public class CardTextRenderPatches {
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
                    original = ReflectionHacks.getPrivate(_instance, AbstractCard.class, "textColor");
                    ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", ModSettings.addColor);
                } else if (tmp[0].equals("[DiffAddE] ")) {
                    tmp[0] = "";
                    if (original == null) {
                        System.out.println("ERROR! Diff end without start!!");
                    } else {
                        ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", original);
                    }
                } else if (tmp[0].equals("[DiffRmvS] ")) {
                    tmp[0] = "";
                    original = ReflectionHacks.getPrivate(_instance, AbstractCard.class, "textColor");
                    ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", ModSettings.removeColor);
                } else if (tmp[0].equals("[DiffRmvE] ")) {
                    tmp[0] = "";
                    if (original == null) {
                        System.out.println("ERROR! Diff end without start!!");
                    } else {
                        ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", original);
                    }
                }
            }
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

    @SpirePatch(
            clz = SingleCardViewPopup.class,
            method = "renderDescription"
    )
    public static class AlterBigDescriptionRenderingPatch {
        private static Color original = null;

        /* I'm going to have to instrument patch this - replace the Settings.CREAM_COLOR with a method call */
        @SpireInsertPatch(
                locator = AlterBigDescriptionRenderingPatchLocator.class,
                localvars = {"tmp"}
        )
        public static void Insert(SingleCardViewPopup _instance, SpriteBatch sb, @ByRef String[] tmp) {
            if (tmp[0].length() > 0 && tmp[0].charAt(0) == '[') {
                if (tmp[0].equals("[DiffAddS] ")) {
                    tmp[0] = "";
                    original = ReflectionHacks.getPrivate(_instance, AbstractCard.class, "textColor");
                    ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", ModSettings.addColor);
                } else if (tmp[0].equals("[DiffAddE] ")) {
                    tmp[0] = "";
                    if (original == null) {
                        System.out.println("ERROR! Diff end without start!!");
                    } else {
                        ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", original);
                    }
                } else if (tmp[0].equals("[DiffRmvS] ")) {
                    tmp[0] = "";
                    original = ReflectionHacks.getPrivate(_instance, AbstractCard.class, "textColor");
                    ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", ModSettings.removeColor);
                } else if (tmp[0].equals("[DiffRmvE] ")) {
                    tmp[0] = "";
                    if (original == null) {
                        System.out.println("ERROR! Diff end without start!!");
                    } else {
                        ReflectionHacks.setPrivate(_instance, AbstractCard.class, "textColor", original);
                    }
                }
            }
        }
    }

    public static class AlterBigDescriptionRenderingPatchLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(GlyphLayout.class, "setText");
            int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
            return new int[]{lines[lines.length-1]}; // Only last occurrence
        }
    }
}
