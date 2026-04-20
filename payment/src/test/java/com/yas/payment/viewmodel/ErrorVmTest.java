package com.yas.payment.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void compactConstructor_createsEmptyFieldErrors() {
        var vm = new ErrorVm("400", "Bad Request", "invalid payload");

        assertThat(vm.statusCode()).isEqualTo("400");
        assertThat(vm.title()).isEqualTo("Bad Request");
        assertThat(vm.detail()).isEqualTo("invalid payload");
        assertThat(vm.fieldErrors()).isEmpty();
    }

    @Test
    void canonicalConstructor_keepsGivenFieldErrors() {
        var vm = new ErrorVm("422", "Unprocessable", "validation failed", List.of("name required"));

        assertThat(vm.fieldErrors()).containsExactly("name required");
    }
}
