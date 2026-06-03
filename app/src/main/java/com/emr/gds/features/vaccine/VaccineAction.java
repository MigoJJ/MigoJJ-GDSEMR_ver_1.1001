package com.emr.gds.features.vaccine;

import com.emr.gds.features.vaccine.service.VaccineService;
import com.emr.gds.features.vaccine.view.VaccineController;
import com.emr.gds.infrastructure.service.EmrBridgeService;

/**
 * Entry point for the Vaccine module.
 * External callers use VaccineAction.open() — the internal package structure is hidden.
 */
public class VaccineAction {

    private static VaccineController controller;

    public static void open() {
        if (controller == null) {
            controller = new VaccineController(new VaccineService(new EmrBridgeService()));
        }
        controller.show();
    }
}
