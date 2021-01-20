package net.marvk.fs.vatsim.map.view.icons;

import org.kordamp.ikonli.IkonProvider;
import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor(IkonProvider.class)
public class VatprismIkonProvider implements IkonProvider<VatprismIcon> {
    @Override
    public Class<VatprismIcon> getIkon() {
        return VatprismIcon.class;
    }
}
