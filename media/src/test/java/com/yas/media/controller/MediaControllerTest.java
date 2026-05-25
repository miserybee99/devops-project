package com.yas.media.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaVm;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// add cmt

@WebMvcTest(controllers = MediaController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Test
    void create_whenValidInput_thenReturn200() throws Exception {
        Media media = new Media();
        media.setId(1L);
        media.setCaption("test caption");
        media.setFileName("test.png");
        media.setMediaType("image/png");

        when(mediaService.saveMedia(any())).thenReturn(media);

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);

        MockMultipartFile file = new MockMultipartFile(
            "multipartFile", "test.png", "image/png", baos.toByteArray()
        );

        mockMvc.perform(multipart("/medias")
                .file(file)
                .param("caption", "test caption")
                .param("fileNameOverride", "test.png"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.caption").value("test caption"))
            .andExpect(jsonPath("$.fileName").value("test.png"))
            .andExpect(jsonPath("$.mediaType").value("image/png"));
    }

    @Test
    void delete_whenValidId_thenReturn204() throws Exception {
        doNothing().when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMediaNotFound_thenThrowNotFoundException() throws Exception {
        doThrow(new NotFoundException("Media 1 is not found"))
            .when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void get_whenMediaExists_thenReturn200() throws Exception {
        MediaVm mediaVm = new MediaVm(1L, "caption", "file.png", "image/png", "/media/medias/1/file/file.png");
        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        mockMvc.perform(get("/medias/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.caption").value("caption"))
            .andExpect(jsonPath("$.fileName").value("file.png"));
    }

    @Test
    void get_whenMediaNotFound_thenReturn404() throws Exception {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        mockMvc.perform(get("/medias/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getByIds_whenMediasExist_thenReturn200() throws Exception {
        MediaVm vm1 = new MediaVm(1L, "cap1", "f1.png", "image/png", "/url1");
        MediaVm vm2 = new MediaVm(2L, "cap2", "f2.png", "image/png", "/url2");
        when(mediaService.getMediaByIds(List.of(1L, 2L))).thenReturn(List.of(vm1, vm2));

        mockMvc.perform(get("/medias").param("ids", "1", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getByIds_whenNoMediasFound_thenReturn404() throws Exception {
        when(mediaService.getMediaByIds(List.of(99L))).thenReturn(List.of());

        mockMvc.perform(get("/medias").param("ids", "99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getFile_whenFileExists_thenReturnFile() throws Exception {
        byte[] content = "file-content".getBytes();
        MediaDto mediaDto = MediaDto.builder()
            .content(new ByteArrayInputStream(content))
            .mediaType(MediaType.IMAGE_PNG)
            .build();

        when(mediaService.getFile(eq(1L), eq("test.png"))).thenReturn(mediaDto);

        mockMvc.perform(get("/medias/1/file/test.png"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.png\""));
    }
}
