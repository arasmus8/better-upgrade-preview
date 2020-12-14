package betterUpgradePreview;

import basemod.BaseMod;
import basemod.ModColorDisplay;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private final ArrayList<ModColorDisplay> removeColorButtons = new ArrayList<>();
    private final ArrayList<ModColorDisplay> addColorButtons = new ArrayList<>();

    @Override
    public void receivePostInitialize() {
        try {
            UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("ModBadge"));
            Texture modBadge = new Texture(modId + "Resources/badge.png");

            ModPanel settingsPanel = new ModPanel(p -> {
                Texture colorButton = new Texture(modId + "Resources/colorButton.png");
                Texture colorButtonOutline = new Texture(modId + "Resources/colorButtonOutline.png");
                Consumer<ModColorDisplay> handleRemoveClick = modColorDisplay -> {
                    removeColorButtons.forEach(m -> {
                        m.rOutline = Color.DARK_GRAY.r;
                        m.gOutline = Color.DARK_GRAY.g;
                        m.bOutline = Color.DARK_GRAY.b;
                    });
                    modColorDisplay.rOutline = Color.GOLDENROD.r;
                    modColorDisplay.gOutline = Color.GOLDENROD.g;
                    modColorDisplay.bOutline = Color.GOLDENROD.b;
                    ModSettings.removeColor = new Color(modColorDisplay.r, modColorDisplay.g, modColorDisplay.b, 1.0f);
                    ModSettings.saveSettings();
                };
                Consumer<ModColorDisplay> handleAddClick = modColorDisplay -> {
                    addColorButtons.forEach(m -> {
                        m.rOutline = Color.DARK_GRAY.r;
                        m.gOutline = Color.DARK_GRAY.g;
                        m.bOutline = Color.DARK_GRAY.b;
                    });
                    modColorDisplay.rOutline = Color.GOLDENROD.r;
                    modColorDisplay.gOutline = Color.GOLDENROD.g;
                    modColorDisplay.bOutline = Color.GOLDENROD.b;
                    ModSettings.addColor = new Color(modColorDisplay.r, modColorDisplay.g, modColorDisplay.b, 1.0f);
                    ModSettings.saveSettings();
                };
                //load settings
                ModSettings.loadSettings();

                p.addUIElement(new ModLabel(uiStrings.TEXT_DICT.get("REMOVE_LABEL"),
                        380f,
                        750f,
                        p,
                        (modLabel -> { })));
                List<Color> removeColors = new ArrayList<>();
                removeColors.add(new Color(0xFF6563FF));
                removeColors.add(new Color(0x666666FF));
                removeColors.add(new Color(0x5c1500FF));
                removeColors.add(new Color(0x5c3500FF));
                removeColors.add(new Color(0x003673FF));
                for (int i = 0; i < removeColors.size(); i++) {
                    ModColorDisplay modColorDisplay = new ModColorDisplay(380f + i * 96f,
                            680f,
                            0f,
                            colorButton,
                            colorButtonOutline,
                            handleRemoveClick);
                    Color color = removeColors.get(i);
                    modColorDisplay.r = color.r;
                    modColorDisplay.g = color.g;
                    modColorDisplay.b = color.b;
                    if (ModSettings.colorMatches(color, ModSettings.removeColor)) {
                        modColorDisplay.rOutline = Color.GOLDENROD.r;
                        modColorDisplay.gOutline = Color.GOLDENROD.g;
                        modColorDisplay.bOutline = Color.GOLDENROD.b;
                    } else {
                        modColorDisplay.rOutline = Color.DARK_GRAY.r;
                        modColorDisplay.gOutline = Color.DARK_GRAY.g;
                        modColorDisplay.bOutline = Color.DARK_GRAY.b;
                    }
                    removeColorButtons.add(modColorDisplay);
                    p.addUIElement(modColorDisplay);
                }

                p.addUIElement(new ModLabel(uiStrings.TEXT_DICT.get("ADD_LABEL"),
                        380f,
                        610f,
                        p,
                        (modLabel -> { })));
                List<Color> addColors = new ArrayList<>();
                addColors.add(new Color(0x7FFF00FF));
                addColors.add(new Color(0xE1FF00FF));
                addColors.add(new Color(0x00FFF7FF));
                addColors.add(new Color(0x0095FFFF));
                addColors.add(new Color(0xc300FFFF));
                for (int i = 0; i < addColors.size(); i++) {
                    ModColorDisplay modColorDisplay = new ModColorDisplay(380f + i * 96f,
                            550f,
                            0f,
                            colorButton,
                            colorButtonOutline,
                            handleAddClick);
                    Color color = addColors.get(i);
                    modColorDisplay.r = color.r;
                    modColorDisplay.g = color.g;
                    modColorDisplay.b = color.b;
                    if (ModSettings.colorMatches(color, ModSettings.addColor)) {
                        modColorDisplay.rOutline = Color.GOLDENROD.r;
                        modColorDisplay.gOutline = Color.GOLDENROD.g;
                        modColorDisplay.bOutline = Color.GOLDENROD.b;
                    } else {
                        modColorDisplay.rOutline = Color.DARK_GRAY.r;
                        modColorDisplay.gOutline = Color.DARK_GRAY.g;
                        modColorDisplay.bOutline = Color.DARK_GRAY.b;
                    }
                    addColorButtons.add(modColorDisplay);
                    p.addUIElement(modColorDisplay);
                }
            });

            BaseMod.registerModBadge(modBadge,
                    uiStrings.TEXT_DICT.get("MOD_NAME"),
                    uiStrings.TEXT_DICT.get("MOD_AUTHOR"),
                    uiStrings.TEXT_DICT.get("MOD_DESC"),
                    settingsPanel);
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
