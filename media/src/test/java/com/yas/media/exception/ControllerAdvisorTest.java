package com.yas.media.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.UnsupportedMediaTypeException;
import com.yas.media.viewmodel.ErrorVm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class ControllerAdvisorTest {

    private ControllerAdvisor advisor;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        advisor = new ControllerAdvisor();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getServletPath()).thenReturn("/test");
        webRequest = new ServletWebRequest(servletRequest);
    }

    @Test
    void handleUnsupportedMediaTypeException_thenReturnBadRequest() {
        UnsupportedMediaTypeException ex = new UnsupportedMediaTypeException("unsupported");

        ResponseEntity<ErrorVm> response = advisor.handleUnsupportedMediaTypeException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unsupported media type", response.getBody().title());
    }

    @Test
    void handleNotFoundException_thenReturnNotFound() {
        NotFoundException ex = new NotFoundException("Media 1 is not found");

        ResponseEntity<ErrorVm> response = advisor.handleNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Media 1 is not found", response.getBody().detail());
    }

    @Test
    void handleMethodArgumentNotValid_thenReturnBadRequest() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "caption", "must not be null"));

        MethodParameter methodParameter = new MethodParameter(
            ControllerAdvisorTest.class.getDeclaredMethod("dummyMethod", String.class), 0
        );

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorVm> response = advisor.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().fieldErrors());
        assertEquals(1, response.getBody().fieldErrors().size());
        assertEquals("caption must not be null", response.getBody().fieldErrors().get(0));
    }

    @SuppressWarnings("unused")
    void dummyMethod(String param) {
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_thenReturnBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenReturn((Class) String.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("invalid");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorVm> response = advisor.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleRuntimeException_thenReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("something went wrong");

        ResponseEntity<ErrorVm> response = advisor.handleIoException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("something went wrong", response.getBody().detail());
    }

    @Test
    void handleOtherException_thenReturnInternalServerError() {
        Exception ex = new Exception("unexpected error");

        ResponseEntity<ErrorVm> response = advisor.handleOtherException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("unexpected error", response.getBody().detail());
    }
}
