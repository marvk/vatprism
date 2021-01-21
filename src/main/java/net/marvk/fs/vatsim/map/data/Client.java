package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimServer;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Client implements Settable<VatsimClient>, Data {
    private static final Pattern URL = Pattern.compile("(?:(?:https?[: ]\\/\\/)?(?<content>(www)?(?:[a-z0-9-]{1,256}\\.)+(?:[a-z]{2,})(?:\\/[a-z0-9-_]+)*\\/?))", Pattern.CASE_INSENSITIVE);
    private final IntegerProperty cid = new SimpleIntegerProperty();
    private final StringProperty cidString = new SimpleStringProperty();
    private final StringProperty callsign = new SimpleStringProperty();
    private final StringProperty realName = new SimpleStringProperty();
    private final StringProperty server = new SimpleStringProperty();

    private final ObjectProperty<ZonedDateTime> logonTime = new SimpleObjectProperty<>();
    private final ObjectProperty<ZonedDateTime> lastUpdatedTime = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<String> urls = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    public Client() {
        cidString.bind(cid.asString());
    }

    @Override
    public void setFromModel(final VatsimClient client) {
        Objects.requireNonNull(client);

        cid.set(Integer.parseInt(client.getCid()));
        callsign.set(client.getCallsign());
        realName.set(client.getName());
        final VatsimServer server = client.getServer();
        if (server != null) {
            this.server.set(server.getName());
        }
        logonTime.set(client.getLogon());
        lastUpdatedTime.set(client.getLastUpdate());
    }

    public int getCid() {
        return cid.get();
    }

    public ReadOnlyIntegerProperty cidProperty() {
        return cid;
    }

    public String getCidString() {
        return cidString.get();
    }

    public ReadOnlyStringProperty cidStringProperty() {
        return cidString;
    }

    public String getCallsign() {
        return callsign.get();
    }

    public ReadOnlyStringProperty callsignProperty() {
        return callsign;
    }

    public String getRealName() {
        return realName.get();
    }

    public ReadOnlyStringProperty realNameProperty() {
        return realName;
    }

    public String getServer() {
        return server.get();
    }

    public ReadOnlyStringProperty serverProperty() {
        return server;
    }

    public ZonedDateTime getLogonTime() {
        return logonTime.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> logonTimeProperty() {
        return logonTime;
    }

    public ZonedDateTime getLastUpdatedTime() {
        return lastUpdatedTime.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> lastUpdatedTimeProperty() {
        return lastUpdatedTime;
    }

    public abstract ClientType clientType();

    protected SimpleListProperty<String> getUrlsWritable() {
        return urls;
    }

    public ReadOnlyListProperty<String> getUrls() {
        return urls.getReadOnlyProperty();
    }

    @SneakyThrows
    protected void setUrls(final String s) {
        if (s == null) {
            return;
        }
        this.urls.setAll(parseUrls(s));
    }

    private static List<String> parseUrls(final String s) {
        return URL
                .matcher(s.replaceAll("/./\s+$", ""))
                .results()
                .map(e -> e.group(1))
                .map(e -> e.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }
}
