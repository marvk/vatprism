package net.marvk.fs.vatsim.map.api;

import com.github.zafarkhaja.semver.Version;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDay;

import java.util.List;

public interface VatprismApi {
    VersionResponse checkVersion(final UpdateChannel channel) throws VatprismApiException;

    void submitThemeChoice(final String themeName) throws VatprismApiException;

    List<MessageOfTheDay> messagesOfTheDay(final Version version, final Double focusedHours, final Double totalHours, final boolean unfiltered) throws VatprismApiException;
}
