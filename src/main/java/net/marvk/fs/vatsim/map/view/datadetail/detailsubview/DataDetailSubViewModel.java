package net.marvk.fs.vatsim.map.view.datadetail.detailsubview;

import net.marvk.fs.vatsim.map.data.Data;

public class DataDetailSubViewModel<D extends Data> extends DetailSubViewModel<D> {
    public void goTo() {
        goTo(data.get());
    }
}
