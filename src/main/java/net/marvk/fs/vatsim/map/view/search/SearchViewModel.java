package net.marvk.fs.vatsim.map.view.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.StatusScope;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SearchViewModel implements ViewModel {
    private final StringProperty query = new SimpleStringProperty();
    private final ReadOnlyObjectWrapper<ObservableList<Data>> results = new ReadOnlyObjectWrapper<>();
    private final DelegateCommand searchCommand;

    @InjectScope
    private StatusScope statusScope;

    @Inject
    public SearchViewModel(final Provider<SearchActionSupplier> searchActionProvider) {
        final SearchActionSupplier supplier = searchActionProvider.get();
        this.searchCommand = new DelegateCommand(() -> supplier.createAction(query.get(), results), query.isNotEmpty(), true);
    }

    public void initialize() {
        System.out.println("statusScope = " + statusScope);

        results.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                statusScope.getSearchedData().clear();
            } else {
                statusScope.getSearchedData().setAll(newValue);
            }

            Notifications.REPAINT.publish();
        });
    }

    public void search() {
        if (searchCommand.isExecutable() && searchCommand.isNotRunning()) {
            searchCommand.execute();
        }
    }

    public void clear() {
        query.set("");
        results.set(null);
    }

    public String getQuery() {
        return query.get();
    }

    public StringProperty queryProperty() {
        return query;
    }

    public void setQuery(final String query) {
        this.query.set(query);
    }

    public List<? extends Data> getResults() {
        return results.get();
    }

    public ReadOnlyObjectProperty<ObservableList<Data>> resultsProperty() {
        return results.getReadOnlyProperty();
    }

    public void setDataDetail(final Data data) {
        if (data != null) {
            Notifications.SET_DATA_DETAIL.publish(data);
        }
    }

    public DelegateCommand getSearchCommand() {
        return searchCommand;
    }

    private static class SearchActionSupplier {
        private final ClientRepository clientRepository;

        @Inject
        public SearchActionSupplier(final ClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        public Action createAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
            return new SearchAction(query, result);
        }

        private class SearchAction extends Action {
            private final String query;
            private final ObjectProperty<ObservableList<Data>> result;

            public SearchAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
                this.query = query;
                this.result = result;
            }

            @Override
            protected void action() throws Exception {
                final FilteredList<? extends Data> filtered = clientRepository
                        .list()
                        .filtered(e -> StringUtils.containsIgnoreCase(e.getCallsign(), query));

                result.set((ObservableList<Data>) filtered);
            }
        }
    }
}
