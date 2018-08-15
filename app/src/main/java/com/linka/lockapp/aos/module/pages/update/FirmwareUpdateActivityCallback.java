package com.linka.lockapp.aos.module.pages.update;

import com.linka.lockapp.aos.module.pages.dfu.DfuManager;

public interface FirmwareUpdateActivityCallback {
    void changeCurrentPage(int page);
    void changeTitle(String text);
    void setBackButtonVisibility(int visibility);
    DfuManager getDfuManager();
}
