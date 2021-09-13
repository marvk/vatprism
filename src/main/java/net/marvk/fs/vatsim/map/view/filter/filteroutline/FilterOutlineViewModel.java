package net.marvk.fs.vatsim.map.view.filter.filteroutline;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.data.FilterRepository;
import net.marvk.fs.vatsim.map.data.RepositoryException;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.filter.FilterListViewModel;
import net.marvk.fs.vatsim.map.view.filter.FilterScope;

import java.util.Optional;

@Log4j2
public class FilterOutlineViewModel implements ViewModel {
    private final ListProperty<FilterListViewModel> filters = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final FilterRepository filterRepository;

    @InjectScope
    private FilterScope filterScope;

    @Inject
    public FilterOutlineViewModel(final FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
        filterRepository.list().stream().map(FilterListViewModel::new).forEach(filters::add);
    }

    public void initialize() {
        filterScope.filterProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            final Optional<FilterListViewModel> existing = filters
                    .stream()
                    .filter(e -> e.getUuid().equals(newValue.getUuid()))
                    .findFirst();

            if (existing.isPresent()) {
                update(existing.get(), newValue);
            } else {
                create(newValue);
            }
        });
    }

    private void create(final Filter filter) {
        filters.add(new FilterListViewModel(filter));
        try {
            filterRepository.create(filter);
        } catch (final RepositoryException e) {
            log.error("Failed to create filter file", e);
        } finally {
            Notifications.REPAINT.publish();
        }
    }

    private void update(final FilterListViewModel viewModel, final Filter filter) {
        viewModel.setFilter(filter);
        try {
            filterRepository.update(filter);
        } catch (final RepositoryException e) {
            log.error("Failed to update filter file", e);
        } finally {
            Notifications.REPAINT.publish();
        }
    }

    public void delete(final FilterListViewModel viewModel) {
        filters.remove(viewModel);

        if (viewModel == null || viewModel.getFilter() == null) {
            return;
        }

        if (filterScope.getFilter() != null && filterScope.getFilter().getUuid().equals(viewModel.getUuid())) {
            filterScope.setFilter(null);
        }
        try {
            filterRepository.delete(viewModel.getFilter());
        } catch (final RepositoryException e) {
            log.error("Failed to delete filter file", e);
        } finally {
            Notifications.REPAINT.publish();
        }
    }

    public ObservableList<FilterListViewModel> getFilters() {
        return filters.get();
    }

    public ListProperty<FilterListViewModel> filtersProperty() {
        return filters;
    }

    public void addNewFilter() {
        setActive(new Filter());
    }

    public void setActive(final Filter filter) {
        filterScope.setFilter(filter);
    }

    public void setActive(final FilterListViewModel selectedItem) {
        if (selectedItem == null) {
            setActive((Filter) null);
        } else {
            setActive(selectedItem.getFilter());
        }
    }
}
