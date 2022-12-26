package net.marvk.fs.vatsim.map.view.datatable;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.ImmutableObjectProperty;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2
public class ColumnBuilderFactory<Model extends Data> {
    private final SimpleTableViewModel<Model> viewModel;
    private final TextFlowHighlighter textFlowHighlighter;
    private final Consumer<TableColumn<Model, ?>> columnConsumer;

    public ColumnBuilderFactory(
            final SimpleTableViewModel<Model> viewModel,
            final TextFlowHighlighter textFlowHighlighter,
            final Consumer<TableColumn<Model, ?>> columnConsumer
    ) {
        this.viewModel = viewModel;
        this.textFlowHighlighter = textFlowHighlighter;
        this.columnConsumer = columnConsumer;
    }

    public <CellValue> TitleStep<Model, CellValue> newBuilder() {
        return new Builder<>();
    }

    public final class Builder<CellValue> implements
            TitleStep<Model, CellValue>,
            ComparableStep<CellValue>,
            StringMapperStep<Model, CellValue>,
            ValueStep<Model, CellValue>,
            WidthStep {

        private final TableColumn<Model, CellValue> result;
        private boolean mono;
        private BiFunction<CellValue, String, String> stringMapper;
        private BiFunction<Model, ObservableStringValue, ObservableValue<CellValue>> valueFactory;
        private boolean valueNullable;
        private double widthFactor = 1.0;

        private Builder() {
            this.result = new TableColumn<>();
            this.result.setSortable(false);
        }

        @Override
        public ValueStep<Model, CellValue> title(final String title) {
            this.result.setText(title);
            return this;
        }

        @Override
        public StringMapperStep<Model, CellValue> objectObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableValue<CellValue>> valueFactory) {
            this.valueFactory = valueFactory;
            return this;
        }

        @Override
        public ComparableStep<CellValue> toStringMapper(final BiFunction<CellValue, String, String> stringMapper, final boolean nullable) {
            this.valueNullable = nullable;
            this.stringMapper = stringMapper;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ComparableStep<String> stringObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableStringValue> valueFactory) {
            this.valueFactory = (model, query) -> (ObservableValue<CellValue>) valueFactory.apply(model, query);
            return (ComparableStep<String>) toStringMapper(e -> (String) e);
        }

        @Override
        public MonoStep sortable() {
            this.result.setSortable(true);
            return this;
        }

        @Override
        public MonoStep sortable(final Comparator<CellValue> comparator) {
            this.result.setComparator(comparator);
            this.result.setSortable(true);
            return this;
        }

        @Override
        public WidthStep mono(final boolean mono) {
            this.mono = mono;
            return this;
        }

        @Override
        public BuildStep widthFactor(final double factor) {
            if (factor <= 0) {
                throw new IllegalArgumentException("Factor can not be equal or less than zero, was %s".formatted(factor));
            }
            this.widthFactor = factor;
            return this;
        }

        @Override
        public void build() {
            result.setCellValueFactory(this::valueOrNullPropertyProperty);
            result.setCellFactory(param -> new DataTableCell<>((cellValue, query) -> textFlowAdjusted(stringMapper.apply(cellValue, query), mono), valueNullable));
            result.setReorderable(false);
            result.prefWidthProperty()
                  .bind(viewModel.fontSizeProperty().divide(12.0).multiply(100.0).multiply(widthFactor));
            columnConsumer.accept(result);
        }

        /*
         * TODO Band aid fix for bad api data
         */
        private ObservableValue<CellValue> valueOrNullPropertyProperty(final TableColumn.CellDataFeatures<Model, CellValue> param) {
            try {
                return valueFactory.apply(param.getValue(), viewModel.queryProperty());
            } catch (final Exception e) {
                log.error("Unexpected error while calculating cell value", e);
                return new ImmutableObjectProperty<>(null);
            }
        }

        private Node textFlowAdjusted(final String cellValue, final boolean mono) {
            // TODO find cause of nullable cellValue
            if (cellValue == null) {
                return null;
            }

            final TextFlow textFlow = textFlow(cellValue, mono);
            textFlow.setMinHeight(0);
            textFlow.setPrefHeight(Region.USE_PREF_SIZE);
            textFlow.setMinWidth(Region.USE_PREF_SIZE);
            textFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
            textFlow.setPadding(new Insets(3, 0, 0, 4));
            return textFlow;
        }

        private TextFlow textFlow(final String cellValue, final boolean mono) {
            if (viewModel.getQuery() == null || viewModel.getQuery().isBlank()) {
                return textFlowHighlighter.createSimpleTextFlow(cellValue, mono);
            }
            return textFlowHighlighter.createHighlightedTextFlow(cellValue, viewModel.getPattern(), mono);
        }
    }

    public interface TitleStep<Model extends Data, CellValue> {
        ValueStep<Model, CellValue> title(final String s);
    }

    public interface ValueStep<Model extends Data, CellValue> {
        default StringMapperStep<Model, CellValue> objectObservableValueFactory(final Function<Model, ObservableValue<CellValue>> valueFactory) {
            return objectObservableValueFactory((model, ignored) -> valueFactory.apply(model));
        }

        StringMapperStep<Model, CellValue> objectObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableValue<CellValue>> valueFactory);

        default ComparableStep<String> stringObservableValueFactory(final Function<Model, ObservableStringValue> valueFactory) {
            return stringObservableValueFactory((model, ignored) -> valueFactory.apply(model));
        }

        ComparableStep<String> stringObservableValueFactory(final BiFunction<Model, ObservableStringValue, ObservableStringValue> valueFactory);
    }

    public interface StringMapperStep<Model extends Data, CellValue> {

        default ComparableStep<CellValue> toStringMapper(final Function<CellValue, String> stringMapper) {
            return toStringMapper((cellValue, ignored) -> stringMapper.apply(cellValue));
        }

        default ComparableStep<CellValue> toStringMapper(final Function<CellValue, String> stringMapper, final boolean nullable) {
            return toStringMapper((cellValue, ignored) -> stringMapper.apply(cellValue), nullable);
        }

        default ComparableStep<CellValue> toStringMapper(final BiFunction<CellValue, String, String> stringMapper) {
            return toStringMapper(stringMapper, false);
        }

        ComparableStep<CellValue> toStringMapper(final BiFunction<CellValue, String, String> stringMapper, final boolean nullable);
    }

    public interface ComparableStep<CellValue> extends MonoStep, WidthStep, BuildStep {
        MonoStep sortable();

        MonoStep sortable(final Comparator<CellValue> comparator);
    }

    public interface MonoStep extends WidthStep, BuildStep {
        WidthStep mono(final boolean mono);
    }

    public interface WidthStep extends BuildStep {
        BuildStep widthFactor(final double factor);
    }

    public interface BuildStep {
        void build();
    }

    private class DataTableCell<CellValue> extends TableCell<Model, CellValue> {
        private final BiFunction<CellValue, String, Node> nodeSupplier;
        private final boolean valueNullable;

        public DataTableCell(final BiFunction<CellValue, String, Node> nodeSupplier, final boolean valueNullable) {
            this.nodeSupplier = nodeSupplier;
            this.valueNullable = valueNullable;
        }

        @Override
        protected void updateItem(final CellValue item, final boolean empty) {
            super.updateItem(item, empty);

            if (empty || (item == null && !valueNullable)) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(nodeSupplier.apply(item, viewModel.getQuery()));
            }
        }
    }
}
