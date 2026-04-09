package com.emr.gds.server.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestErrorController.class)
@Import(ApiExceptionHandler.class)
class ApiExceptionHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handlesMethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/api/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/test/body"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name: name required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handlesConstraintViolation() throws Exception {
        mockMvc.perform(get("/api/test/param").param("param", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/test/param"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value(containsString("param too short")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handlesGenericException() throws Exception {
        mockMvc.perform(get("/api/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("internal_error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/api/test/boom"))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.details[0]").value("kaboom"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
