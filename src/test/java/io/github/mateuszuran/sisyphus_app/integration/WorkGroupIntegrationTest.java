package io.github.mateuszuran.sisyphus_app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.AbstractIntegrationTest;
import io.github.mateuszuran.sisyphus_app.SisyphusAppApplication;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import org.bson.types.Binary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SisyphusAppApplication.class)
@AutoConfigureMockMvc
public class WorkGroupIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkGroupRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        repository.deleteAll();
    }

    @Test
    void givenFile_whenCreate_thenReturnStatus200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "cv",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "sample content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/group/create")
                        .file(file))
                .andExpect(status().isOk());
        var groups = repository.findAll();

        Assertions.assertNotNull(groups);
        Assertions.assertEquals(groups.get(0).getSend(), 0);
    }

    @Test
    void givenNothing_whenGetAllGroups_thenExpectListOfObjects() throws Exception {
        var pdf = new Binary(fakePdf());

        repository.save(WorkGroup.builder().cvData(pdf).creationTime("today").build());

        mockMvc.perform(get("/group/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].creationTime").value("today"));
    }

    @Test
    void givenWorKGroup_whenGet_thenReturnSingleObject() throws Exception {
        var pdf = new Binary(fakePdf());

        WorkGroup group = WorkGroup.builder().cvData(pdf).creationTime("tomorrow").send(15).denied(4).inProgress(12).build();
        var savedWorkGroup = repository.save(group);

        mockMvc.perform(get("/group/single/" + savedWorkGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedWorkGroup.getId()))
                .andExpect(jsonPath("$.creationTime").value("tomorrow"));
    }

    @Test
    void givenWorkGroup_thenDelete_thenReturnStatusOk() throws Exception {
        WorkGroup group = WorkGroup.builder().creationTime("tomorrow").send(15).denied(4).inProgress(12).build();
        var savedWorkGroup = repository.save(group);

        mockMvc.perform(delete("/group/delete/" + savedWorkGroup.getId()))
                .andExpect(status().isOk());

        Assertions.assertTrue(repository.findAll().isEmpty());
        Assertions.assertTrue(repository.findById(savedWorkGroup.getId()).isEmpty());
    }

    private byte[] fakePdf() throws IOException {
        String pdfFilePath = "src/test/resources/LoremIpsum.pdf";
        return  Files.readAllBytes(Paths.get(pdfFilePath));
    }

}
