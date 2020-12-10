package patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;

import java.util.ArrayList;

@SpirePatch(
        clz = SingleCardViewPopup.class,
        method = SpirePatch.CLASS
)
public class SingleCardViewPopupFields {
    public static SpireField<ArrayList<AbstractCard>> unupgradedCardRewards = new SpireField<>(() -> null);
}
