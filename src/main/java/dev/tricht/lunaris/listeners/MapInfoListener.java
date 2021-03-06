package dev.tricht.lunaris.listeners;

import dev.tricht.lunaris.WindowsAPI;
import dev.tricht.lunaris.data.MapInfo;
import dev.tricht.lunaris.data.MapInfoResolver;
import dev.tricht.lunaris.elements.Label;
import dev.tricht.lunaris.item.Item;
import dev.tricht.lunaris.item.ItemGrabber;
import dev.tricht.lunaris.item.types.MapItem;
import dev.tricht.lunaris.tooltip.TooltipCreator;
import dev.tricht.lunaris.elements.Element;
import dev.tricht.lunaris.elements.Icon;
import dev.tricht.lunaris.elements.ItemName;
import lombok.extern.slf4j.Slf4j;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class MapInfoListener implements GameListener {

    private MapInfoResolver mapInfoResolver;

    private Pattern damageAffix = Pattern.compile("(?i:Monsters deal .*% extra damage as .*)");
    private Pattern elementalAffix = Pattern.compile("(?i:Monsters reflect .*% of Elemental Damage)");
    private Pattern physicalAffix = Pattern.compile("(?i:Monsters reflect .*% of Physical Damage)");
    private Pattern recoveryAffix = Pattern.compile("(?i:Players have .*% less Recovery Rate of Life and Energy Shield)");

    public MapInfoListener() {
        this.mapInfoResolver = new MapInfoResolver();
    }

    @Override
    public void onEvent(GameEvent event) {
        try {
                Item item = event.getItem();
                MapInfo mapInfo = this.mapInfoResolver.getMapInfo(item.getBase());

                log.debug("Got a map, creating UI");

                Map<Element, int[]> elements = new LinkedHashMap<>();
                elements.put(new Icon(item, 48), new int[]{0, 0});
                elements.put(new ItemName(item, 48 + Icon.PADDING), new int[]{1, 0});
                List<String> warnings = this.getMapModWarnings(item);
                int row = 1;
                if (!warnings.isEmpty()) {
                    elements.put(new dev.tricht.lunaris.elements.Label("Warning: " + String.join("; ", warnings),
                            new javafx.scene.paint.Color(1, 0.33, 0.33, 1)), new int[]{1, row++});
                }
                if (mapInfo != null) {
                    if (mapInfo.getBosses().size() == 1) {
                        String imageFileName = "/boss_images/" + mapInfo.getBosses().get(0).replace(" ", "_") + ".png";
                        URL imageUrl = MapInfoListener.class.getResource(imageFileName);
                        if (imageUrl != null) {
                            elements.put(new dev.tricht.lunaris.elements.Image(imageFileName), new int[]{1, row++});
                        }
                    }
                    if (mapInfo.getBosses().size() > 0) {
                        elements.put(new dev.tricht.lunaris.elements.Label("Boss(es): " + String.join("; ", mapInfo.getBosses())), new int[]{1, row++});
                    }
                    if (mapInfo.getRegion() != null && !mapInfo.getRegion().isEmpty()) {
                        elements.put(new dev.tricht.lunaris.elements.Label("Region: " + mapInfo.getRegion()), new int[]{1, row++});
                    }
                    if (mapInfo.getPantheon() != null && !mapInfo.getPantheon().isEmpty()) {
                        elements.put(new Label("Pantheon: " + mapInfo.getPantheon()), new int[]{1, row});
                    }
                }

                TooltipCreator.create(event.getMousePos(), elements);
        } catch (Exception e) {
            log.error("Exception while displaying map", e);
        }
    }

    private List<String> getMapModWarnings(Item item) {
        List<String> warnings = new ArrayList<>();
        int damageMods = 0;
        for (String affix : item.getAffixes()) {
            if (damageAffix.matcher(affix).matches()) {
                damageMods++;
            }
            if (elementalAffix.matcher(affix).matches()) {
                warnings.add("Reflects elemental");
            }
            if (physicalAffix.matcher(affix).matches()) {
                warnings.add("Reflects physical");
            }
            if (affix.equals("Players are Cursed with Temporal Chains")) {
                warnings.add("Temporal chains");
            }
            if (affix.equals("Cannot Leech Life from Monsters")) {
                warnings.add("No life leech");
            }
            if (affix.equals("Cannot Leech Life from Monsters")) {
                warnings.add("No life leech");
            }
            if (recoveryAffix.matcher(affix).matches()) {
                warnings.add("Less recovery of Life and ES");
            }
        }
        if (damageMods >= 2) {
            warnings.add(String.format("Multi (%d) extra damage", damageMods));
        }
        return warnings;
    }
}
