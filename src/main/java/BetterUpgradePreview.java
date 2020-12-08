import basemod.BaseMod;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.UIStrings;

@SuppressWarnings("unused")
@SpireInitializer
public class BetterUpgradePreview implements
        EditStringsSubscriber,
        PostInitializeSubscriber {
    public static final String modId = "BetterUpgradePreview";

    public BetterUpgradePreview() {
        BaseMod.subscribe(this);
    }

    public static String makeID(String idText) {
        return modId + ":" + idText;
    }

    public static void initialize() {
        BetterUpgradePreview betterUpgradePreview = new BetterUpgradePreview();
    }

    @Override
    public void receivePostInitialize() {
        try {
            UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ModBadge"));
            Texture modBadge = new Texture(modId + "Resources/badge.png");
            BaseMod.registerModBadge(modBadge,
                    uiStrings.TEXT_DICT.get("MOD_NAME"),
                    uiStrings.TEXT_DICT.get("MOD_AUTHOR"),
                    uiStrings.TEXT_DICT.get("MOD_DESC"),
                    null);
        } catch (GdxRuntimeException err) {
            err.printStackTrace();
        }
    }

    @Override
    public void receiveEditStrings() {
        String lang = "eng";

        if (Settings.language == Settings.GameLanguage.ENG) {
            lang = "eng";
        }

        BaseMod.loadCustomStringsFile(UIStrings.class, modId + "Resources/localization/" + lang + "/uiStrings.json");
    }

}
