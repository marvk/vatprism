package net.marvk.fs.vatsim.map.view.icons;

import org.kordamp.ikonli.AbstractIkonHandler;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.jipsy.ServiceProviderFor;

import java.io.InputStream;
import java.net.URL;

@ServiceProviderFor(IkonHandler.class)
public class VatprismIkonHandler extends AbstractIkonHandler {
    private static final String FONT_RESOURCE = "/net/marvk/fs/vatsim/map/vatprism.ttf";

    @Override
    public boolean supports(final String description) {
        return description != null && description.startsWith("vatprism-");
    }

    @Override
    public Ikon resolve(final String description) {
        return VatprismIcon.findByDescription(description);
    }

    @Override
    public URL getFontResource() {
        return getClass().getResource(FONT_RESOURCE);
    }

    @Override
    public InputStream getFontResourceAsStream() {
        return getClass().getResourceAsStream(FONT_RESOURCE);
    }

    @Override
    public String getFontFamily() {
        return "vatprism";
    }
}
