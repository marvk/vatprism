package net.marvk.fs.vatsim.map;

import com.sun.jna.platform.win32.*;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsException;
import net.harawata.appdirs.impl.MacOSXAppDirs;
import net.harawata.appdirs.impl.UnixAppDirs;
import net.harawata.appdirs.impl.WindowsAppDirs;
import net.harawata.appdirs.impl.WindowsFolderResolver;

public final class CustomAppDirsFactory {
    public static AppDirs createInstance() {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("mac os x")) {
            return new MacOSXAppDirs();
        } else if (os.startsWith("windows")) {
            final WindowsFolderResolver folderResolver = new CustomShellFolderResolver();
            return new WindowsAppDirs(folderResolver);
        } else {
            return new UnixAppDirs();
        }
    }

    private static class CustomShellFolderResolver implements WindowsFolderResolver {
        @Override
        public String resolveFolder(final WindowsAppDirs.FolderId folderId) {
            try {
                return Shell32Util.getKnownFolderPath(convertFolderIdToGuid(folderId));
            } catch (final Win32Exception e) {
                throw new AppDirsException(
                        "SHGetKnownFolderPath returns an error: " + e.getErrorCode());
            } catch (final UnsatisfiedLinkError e) {
                // Fallback for pre-vista OSes. #5
                try {
                    return Shell32Util.getFolderPath(convertFolderIdToCsidl(folderId));
                } catch (final Win32Exception e2) {
                    throw new AppDirsException(
                            "SHGetFolderPath returns an error: " + e2.getErrorCode());
                }
            }
        }

        private Guid.GUID convertFolderIdToGuid(final WindowsAppDirs.FolderId folderId) {
            return switch (folderId) {
                case APPDATA -> KnownFolders.FOLDERID_RoamingAppData;
                case LOCAL_APPDATA -> KnownFolders.FOLDERID_LocalAppData;
                case COMMON_APPDATA -> KnownFolders.FOLDERID_ProgramData;
            };
        }

        protected int convertFolderIdToCsidl(WindowsAppDirs.FolderId folderId) {
            return switch (folderId) {
                case APPDATA -> ShlObj.CSIDL_APPDATA;
                case LOCAL_APPDATA -> ShlObj.CSIDL_LOCAL_APPDATA;
                case COMMON_APPDATA -> ShlObj.CSIDL_COMMON_APPDATA;
            };
        }
    }
}
