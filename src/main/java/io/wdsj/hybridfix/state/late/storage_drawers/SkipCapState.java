package io.wdsj.hybridfix.state.late.storage_drawers;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class SkipCapState {
    public static ThreadLocal<Boolean> SKIP_CAP_INIT = ThreadLocal.withInitial(() -> false);
}
