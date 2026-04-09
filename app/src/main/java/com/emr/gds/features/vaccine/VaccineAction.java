package com.emr.gds.features.vaccine;

import com.emr.gds.infrastructure.service.EmrBridgeService;
import com.emr.gds.features.vaccine.VaccineController;
import com.emr.gds.features.vaccine.VaccineService;

/**
 * A JavaFX tool window for quickly logging vaccine administrations.
 * This window appears as a bottom-right overlay and provides buttons for common vaccines.
 */
public class VaccineAction {

    private static VaccineController controller;

    /**
     * Opens the vaccine logging window. If the window is already open, it brings it to the front.
     */
    public static void open() {
        if (controller == null) {
            controller = new VaccineController(new VaccineService(new EmrBridgeService()));
        }
        controller.show();
    }
}
