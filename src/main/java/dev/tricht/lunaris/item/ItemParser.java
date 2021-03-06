package dev.tricht.lunaris.item;

import dev.tricht.lunaris.item.parser.*;
import dev.tricht.lunaris.item.types.GemItem;
import dev.tricht.lunaris.item.types.ItemType;
import dev.tricht.lunaris.item.types.MapItem;
import dev.tricht.lunaris.item.types.UnknownItem;

import java.util.ArrayList;

public class ItemParser {

    private String[] lines;

    public ItemParser(String[] lines) {
        this.lines = lines;
    }

    public Item parse() {
        ArrayList<ArrayList<String>> parts = getParts();

        if(parts.size() <= 1) {
            return new Item();
        }

        NamePart namePart = new NamePart(parts.get(0));
        StatsPart statsPart = new StatsPart(parts.get(1));

        ItemType itemType = namePart.getItemType();
        if (itemType instanceof UnknownItem) {
            itemType = statsPart.getWeaponType();
        }

        if (itemType instanceof MapItem) {
            ((MapItem) itemType).setTier(statsPart.getMapTier());
        }

        if (itemType instanceof GemItem) {
            ((GemItem) itemType).setLevel(statsPart.getGemLevel());
        }

        //TODO: Prophecy

        ItemProps itemProps = new ItemPropsParts(parts).getProps();

        AffixPart affixPart = new AffixPart(parts.get(new AffixPartIndexCalculator(namePart.getRarity(), itemType, itemProps, parts).getAffixIndex()));

        // TODO: Abyssal sockets

        Item item = new Item();
        item.setType(itemType);
        item.setRarity(namePart.getRarity());
        item.setBase(namePart.getNameWithoutAffixes(affixPart.getAffixes(), itemProps.isIdentified()));
        item.setAffixes(affixPart.getAffixes());
        item.setProps(itemProps);
        item.setName(namePart.getItemName());

        return item;
    }

    public ArrayList<ArrayList<String>> getParts() {
        ArrayList<ArrayList<String>> parts = new ArrayList<ArrayList<String>>();

        ArrayList<String> currentPart = new ArrayList<String>();
        for (String line : lines) {
            if (line.equals("--------")) {
                parts.add(currentPart);
                currentPart = new ArrayList<String>();
                continue;
            }
            currentPart.add(line);
        }
        parts.add(currentPart);

        return parts;
    }
}
