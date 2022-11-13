package io.github.weasleyj.satoken.session;

import org.springframework.util.StringUtils;

/**
 * PluginBanner
 *
 * @author weasley
 * @version 1.0.0
 */
public final class PluginBanner {

    private PluginBanner() {
    }

    public static PluginBanner getInstance() {
        return BannerHolder.INSTANCE;
    }

    /**
     * 打印Banner
     */
    public void showBanner() {
        String versionText = PluginVersion.getVersion();
        if (StringUtils.hasText(versionText))
            versionText = "Plugin Version: (v" + versionText + ")";
        String banner = "" +
                "                   _        _                _           _                           _            _                       _             \n" +
                "                  | |      | |              (_)         | |                         | |          | |                     (_)            \n" +
                "  ___  __ _ ______| |_ ___ | | _____ _ __    _ _ __   __| | ___ _ __   ___ _ __   __| | ___ _ __ | |_   ___  ___  ___ ___ _  ___  _ __  \n" +
                " / __|/ _` |______| __/ _ \\| |/ / _ \\ '_ \\  | | '_ \\ / _` |/ _ \\ '_ \\ / _ \\ '_ \\ / _` |/ _ \\ '_ \\| __| / __|/ _ \\/ __/ __| |/ _ \\| '_ \\ \n" +
                " \\__ \\ (_| |      | || (_) |   <  __/ | | | | | | | | (_| |  __/ |_) |  __/ | | | (_| |  __/ | | | |_  \\__ \\  __/\\__ \\__ \\ | (_) | | | |\n" +
                " |___/\\__,_|       \\__\\___/|_|\\_\\___|_| |_| |_|_| |_|\\__,_|\\___| .__/ \\___|_| |_|\\__,_|\\___|_| |_|\\__| |___/\\___||___/___/_|\\___/|_| |_|\n" +
                "                                                               | |                                                                      \n" +
                "                                                               |_|                                                                      \n" +
                "" + versionText + "\n";
        System.out.println(banner);
    }

    private static class BannerHolder {
        private static final PluginBanner INSTANCE = new PluginBanner();
    }
}
