package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

public class JfxModule extends AbstractModule {
    @Provides
    @Singleton
    @Named("open_hand_cursor")
    public Cursor openHand() {
        return createCursor("cursor_open.png");
    }

    @Provides
    @Singleton
    @Named("closed_hand_cursor")
    public Cursor closedHand() {
        return createCursor("cursor_closed.png");
    }

    private static Cursor createCursor(final String fileName) {
        final Image image = new Image(JfxModule.class.getResourceAsStream("/net/marvk/fs/vatsim/map/view/" + fileName));
        return new ImageCursor(image, 10, 9);
    }
}
