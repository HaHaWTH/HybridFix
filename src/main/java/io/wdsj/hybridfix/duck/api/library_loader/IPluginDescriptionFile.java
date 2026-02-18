package io.wdsj.hybridfix.duck.api.library_loader;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface IPluginDescriptionFile {
    /**
     * Gets the libraries this plugin requires. This is a preview feature.
     * <ul>
     * <li>Libraries must be GAV specifiers and are loaded from Maven Central.
     * </ul>
     * <p>
     * Example:<blockquote><pre>libraries:
     *     - com.squareup.okhttp3:okhttp:4.9.0</pre></blockquote>
     *
     * @return required libraries
     */
    @NotNull
    default List<String> getLibraries() {
        return Collections.emptyList();
    }
}
