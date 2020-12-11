package betterUpgradePreview.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

import java.util.ArrayList;

@SpirePatch(
        clz = AbstractCard.class,
        method = SpirePatch.CLASS
)
public class CardTextFields {
    public static SpireField<String> defaultText = new SpireField<String>(() -> "");
    public static SpireField<String> upgradedText = new SpireField<String>(() -> "");
    public static SpireField<String> diffText = new SpireField<String>(() -> "");
    public static SpireField<ArrayList<String>> diffedKeywords = new SpireField<ArrayList<String>>(() -> null);
}
