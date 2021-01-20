package net.marvk.fs.vatsim.map.version;

import java.io.Serial;

public class VersionApiException extends Exception {
    @Serial
    private static final long serialVersionUID = 6838213101575454645L;

    public VersionApiException() {
    }

    public VersionApiException(final String message) {
        super(message);
    }

    public VersionApiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public VersionApiException(final Throwable cause) {
        super(cause);
    }

    public VersionApiException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
