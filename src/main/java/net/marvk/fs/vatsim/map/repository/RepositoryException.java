package net.marvk.fs.vatsim.map.repository;

import java.io.Serial;

public class RepositoryException extends Exception {
    @Serial
    private static final long serialVersionUID = -3182316927356828235L;

    public RepositoryException() {
    }

    public RepositoryException(final String message) {
        super(message);
    }

    public RepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(final Throwable cause) {
        super(cause);
    }

    public RepositoryException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
