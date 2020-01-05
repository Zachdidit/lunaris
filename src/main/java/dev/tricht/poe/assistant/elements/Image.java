package dev.tricht.poe.assistant.elements;

import dev.tricht.poe.assistant.item.Item;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Image implements Element {

    private String url;

    public static final double PADDING = UIWrap.PADDING;

    public Image(String url) {
        this.url = url;
    }

    @Override
    public Node build() {

        Node node;
        double width = 0;
        double height = 0;

        try {
            javafx.scene.image.Image image = new javafx.scene.image.Image(this.url, true);
            ImageView view = new ImageView(image);
            view.setFitWidth(316);
            view.setFitHeight(164);

            node = view;
        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            node = new Rectangle(0, 0, Color.rgb(33, 33, 33, 1));
        }

        return new UIWrap(node,316, 164).build();
    }
}
