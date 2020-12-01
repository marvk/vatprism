package net.marvk.fs.vatsim.map.view.table;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Repository;

public abstract class SimpleTableViewModel<ViewModel> extends AbstractTableViewModel<ViewModel> {
    private final Repository<ViewModel> clientRepository;

    public SimpleTableViewModel(final Repository<ViewModel> clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public ObservableList<ViewModel> items() {
        return clientRepository.list();
    }
}
