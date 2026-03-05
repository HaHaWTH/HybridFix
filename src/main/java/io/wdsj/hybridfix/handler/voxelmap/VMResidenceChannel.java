package io.wdsj.hybridfix.handler.voxelmap;

public final class VMResidenceChannel {
    private VMResidenceChannel() {
    }

    public static final String CHANNEL = "hybridfix:vm2res";
    public static final String BATCH_UPDATE = "vm_batch_update";
    public static final String CLEAR = "vm_clear";
    public static final String SINGLE_UPDATE = "vm_single_update";
    public static final String SINGLE_REMOVE = "vm_single_remove";
}
