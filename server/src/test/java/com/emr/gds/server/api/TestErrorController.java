package com.emr.gds.server.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
class TestErrorController {

    @PostMapping("/api/test/body")
    String body(@Valid @RequestBody BodyPayload payload) {
        return "ok";
    }

    @GetMapping("/api/test/param")
    String param(@Size(min = 2, message = "param too short") @RequestParam String param) {
        return "ok";
    }

    @GetMapping("/api/test/boom")
    String boom() {
        throw new RuntimeException("kaboom");
    }

    record BodyPayload(@NotBlank(message = "name required") String name) {
    }
}
