package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileTypeValidatorTest {

    private FileTypeValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();

        ValidFileType annotation = mock(ValidFileType.class);
        when(annotation.allowedTypes()).thenReturn(new String[]{"image/jpeg", "image/png", "image/gif"});
        when(annotation.message()).thenReturn("File type not allowed");
        validator.initialize(annotation);

        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    void isValid_whenFileIsNull_thenReturnFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", null, new byte[]{});
        assertFalse(validator.isValid(file, context));
    }

    @Test
    void isValid_whenContentTypeNotAllowed_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        assertFalse(validator.isValid(file, context));
    }

    @Test
    void isValid_whenValidPngImage_thenReturnTrue() throws IOException {
        byte[] imageBytes = createValidImage("png");
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", imageBytes);
        assertTrue(validator.isValid(file, context));
    }

    @Test
    void isValid_whenValidJpegImage_thenReturnTrue() throws IOException {
        byte[] imageBytes = createValidImage("jpg");
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);
        assertTrue(validator.isValid(file, context));
    }

    @Test
    void isValid_whenValidGifImage_thenReturnTrue() throws IOException {
        byte[] imageBytes = createValidImage("gif");
        MultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", imageBytes);
        assertTrue(validator.isValid(file, context));
    }

    @Test
    void isValid_whenAllowedTypeButNotRealImage_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not-an-image".getBytes());
        assertFalse(validator.isValid(file, context));
    }

    @Test
    void isValid_whenAllowedTypeButIOException_thenReturnFalse() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenThrow(new IOException("read error"));

        assertFalse(validator.isValid(file, context));
    }

    private byte[] createValidImage(String format) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}
