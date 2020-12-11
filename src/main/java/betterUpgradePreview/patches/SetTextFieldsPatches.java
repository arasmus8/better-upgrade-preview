package betterUpgradePreview.patches;

import basemod.BaseMod;
import betterUpgradePreview.ModSettings;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SetTextFieldsPatches {
    @SpirePatch(
            clz = AbstractCard.class,
            method = "initializeDescription"
    )
    public static class AbstractCardInitializeDescriptionPatch {
        @SpirePostfixPatch
        public static void defaultAndUpgradedText(AbstractCard _instance) {
            if (_instance.upgraded /*&& "".equals(CardTextFields.upgradedText.get(_instance))*/) {
                CardTextFields.upgradedText.set(_instance, _instance.rawDescription);
            } else { // if ("".equals(CardTextFields.defaultText.get(_instance))) {
                CardTextFields.defaultText.set(_instance, _instance.rawDescription);
            }
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "displayUpgrades"
    )
    public static class AbstractCardDisplayUpgradesPatch {
        @SpirePostfixPatch
        public static void diffText(AbstractCard _instance) {
            String defaultText = CardTextFields.defaultText.get(_instance);
            String upgradedText = CardTextFields.upgradedText.get(_instance);
            if ("".equals(CardTextFields.diffText.get(_instance)) && !defaultText.equals(upgradedText) && !"".equals(upgradedText)) {
                String diffText = calculateTextDiff(defaultText, upgradedText, _instance);
                CardTextFields.diffText.set(_instance, diffText);
            }
            if (!"".equals(CardTextFields.diffText.get(_instance))) {
                _instance.rawDescription = CardTextFields.diffText.get(_instance);
                _instance.initializeDescription();
            }
        }
    }

    private static boolean checkPattern(String original, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(original);
        return matcher.find();
    }

    private static boolean checkForEnergy(String original) {
        return checkPattern(original, "(\\[[RGBPE]])");
    }

    private static boolean checkForDynVar(String original) {
        return checkPattern(original, "(![a-zA-Z:]+?!)");
    }

    private static boolean checkForColor(String original) {
        return checkPattern(original, "(\\[#\\p{XDigit}{6}].*\\[])");
    }

    private static boolean checkForNewline(String original) {
        return checkPattern(original, "^(NL)$");
    }

    private static boolean checkForWhitespace(String original) {
        return checkPattern(original, "\\s+");
    }

    private static String checkForCustomKeyword(String word, AbstractCard card) {
        String modified = word;
        if (modified.length() > 0 && modified.charAt(modified.length() - 1) != ']' && !Character.isLetterOrDigit(modified.charAt(modified.length() - 1))) {
            modified = word.substring(0, word.length() - 1);
        }
        modified = modified.toLowerCase();
        String parentKeyword = GameDictionary.parentWord.get(modified);
        if (parentKeyword != null) {
            modified = parentKeyword;
            if (GameDictionary.keywords.containsKey(modified)) {
                if (BaseMod.keywordIsUnique(modified)) {
                    ArrayList<String> diffKeywords = CardTextFields.diffedKeywords.get(card);
                    if (diffKeywords == null) {
                        diffKeywords = new ArrayList<>();
                    }
                    diffKeywords.add(modified);
                    CardTextFields.diffedKeywords.set(card, diffKeywords);
                    String prefix = BaseMod.getKeywordPrefix(modified);
                    return word.replaceFirst(prefix, "");
                }
            }
        }
        return null;
    }

    private static String calculateTextDiff(String original, String upgraded, AbstractCard card) {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .lineNormalizer((s -> s))
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(start -> start ? "@!@OLDSTART@!@" : "@!@OLDEND@!@")
                .newTag(start -> start ? "@!@DIFFSTART@!@" : "@!@DIFFEND@!@")
                .build();
        try {
            List<DiffRow> rows = generator.generateDiffRows(Collections.singletonList(original), Collections.singletonList(upgraded));
            String diffStr = rows.get(0).getOldLine();
            diffStr = diffStr.replaceAll("[*]@!@DIFFSTART@!@(?=[A-Za-z])", "@!@DIFFSTART@!@*");
            diffStr = diffStr.replaceAll("[*]@!@OLDSTART@!@(?=[A-Za-z])", "@!@OLDSTART@!@*");
            String[] diffTokenized = diffStr.split("(?=@!@OLDSTART@!@)|(?<=@!@OLDSTART@!@)|(?<=@!@OLDEND@!@)|(?=@!@OLDEND@!@)|(?=@!@DIFFSTART@!@)|(?<=@!@DIFFSTART@!@)|(?<=@!@DIFFEND@!@)|(?=@!@DIFFEND@!@)", -1);
            ArrayList<DiffToken> ret = new ArrayList<>();
            boolean inDiff = false;
            boolean inOldDiff = false;
            for (String diffTok : diffTokenized) {
                switch (diffTok) {
                    case "@!@DIFFSTART@!@":
                        inDiff = true;
                        continue;
                    case "@!@DIFFEND@!@":
                        inDiff = false;
                        continue;
                    case "@!@OLDSTART@!@":
                        inOldDiff = true;
                        continue;
                    case "@!@OLDEND@!@":
                        inOldDiff = false;
                        continue;
                }
                String[] words = diffTok.split("(?= )|(?<= )", -1);
                for (String word : words) {
                    if (checkForEnergy(word)) {
                        ret.add(new DiffToken(inOldDiff, inDiff, true, word));
                        continue;
                    }
                    if (checkForDynVar(word)) {
                        ret.add(new DiffToken(inOldDiff, inDiff, true, word));
                        continue;
                    }
                    if (checkForColor(word)) {
                        ret.add(new DiffToken(inOldDiff, inDiff, true, word));
                        continue;
                    }
                    if (checkForNewline(word)) {
                        ret.add(new DiffToken(inOldDiff, inDiff, true, word));
                        continue;
                    }
                    if (checkForWhitespace(word)) {
                        ret.add(new DiffToken(inOldDiff, inDiff, true, word));
                        continue;
                    }
                    if (inDiff || inOldDiff) {
                        String prefixRemoved = checkForCustomKeyword(word, card);
                        if (prefixRemoved != null) {
                            ret.add(new DiffToken(inOldDiff, inDiff, false, prefixRemoved));
                            continue;
                        }
                    }

                    String[] withoutPunctuation = word.split("(?!^)\\b", -1);
                    for (String tok : withoutPunctuation) {
                        if (tok.length() == 0) {
                            continue;
                        }
                        ret.add(new DiffToken(inOldDiff, inDiff, false, tok));
                    }
                }
            }
            if (ret.stream().anyMatch(t -> t.isNew)) {
                StringBuilder sb = new StringBuilder();
                List<DiffToken> filtered = ret.stream().filter(t -> !t.isOld).collect(Collectors.toList());
                boolean newWord = true;
                boolean inColor = false;
                boolean lastTokWasNL = false;
                for (DiffToken token : filtered) {
                    if (newWord && token.isNew && !token.isSpecial) {
                        sb.append("[#").append(ModSettings.addColor).append("]");
                        sb.append(token.toString());
                        inColor = true;
                        newWord = false;
                    } else if (token.isWhitespace()) {
                        if (inColor) {
                            sb.append("[]");
                        }
                        inColor = false;
                        sb.append(token.toString());
                        newWord = true;
                    } else if (token.value.equals("NL")) {
                        if (!newWord) {
                            sb.append(" ");
                        }
                        sb.append(token.toString());
                        lastTokWasNL = true;
                        continue;
                    } else {
                        if (lastTokWasNL && !newWord) {
                            sb.append(" ");
                        }
                        sb.append(token.toString());
                        newWord = false;
                    }
                    lastTokWasNL = false;
                }
                return sb.toString();
            } else if (ret.stream().anyMatch(t -> t.isOld)) {
                StringBuilder sb = new StringBuilder();
                boolean newWord = true;
                boolean inColor = false;
                for (DiffToken token : ret) {
                    if (newWord && token.isOld && !token.isSpecial) {
                        sb.append("[#").append(ModSettings.removeColor).append("]");
                        sb.append(token.toString());
                        inColor = true;
                        newWord = false;
                    } else if (token.isWhitespace()) {
                        if (inColor) {
                            sb.append("[]");
                        }
                        inColor = false;
                        sb.append(token.toString());
                        newWord = true;
                    } else {
                        sb.append(token.toString());
                    }
                }
                return sb.toString();
            } else {
                return original;
            }
        } catch (DiffException e) {
            e.printStackTrace();
            return "";
        }
    }
}
