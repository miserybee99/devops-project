package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// test job CD 
// test domain service
@SpringBootTest(classes = TaxRateService.class)
public class TaxServiceTest {
    @MockitoBean
    TaxRateRepository taxRateRepository;
    @MockitoBean
    LocationService locationService;
    @MockitoBean
    TaxClassRepository taxClassRepository;

    @Autowired
    TaxRateService taxRateService;

    TaxRate taxRate;
    @BeforeEach
    void setUp() {
        TaxClass taxClass = Instancio.create(TaxClass.class);
        taxRate = Instancio.of(TaxRate.class)
            .set(field("taxClass"), taxClass)
            .create();
        lenient().when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));
    }

    @Test
    void  testFindAll_shouldReturnAllTaxRate() {
        // run
        List<TaxRateVm> result = taxRateService.findAll();
        // assert
        assertThat(result).hasSize(1).contains(TaxRateVm.fromModel(taxRate));
    }

    @Test
    void createTaxRate_shouldSave_whenTaxClassExists() {
        TaxRatePostVm postVm = new TaxRatePostVm(7.5, "70000", 1L, 10L, 20L);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);

        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);
        when(taxRateRepository.save(org.mockito.ArgumentMatchers.any(TaxRate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TaxRate created = taxRateService.createTaxRate(postVm);

        assertThat(created.getRate()).isEqualTo(7.5);
        assertThat(created.getZipCode()).isEqualTo("70000");
        assertThat(created.getTaxClass()).isEqualTo(taxClass);
        assertThat(created.getStateOrProvinceId()).isEqualTo(10L);
        assertThat(created.getCountryId()).isEqualTo(20L);
        verify(taxRateRepository).save(org.mockito.ArgumentMatchers.any(TaxRate.class));
    }

    @Test
    void createTaxRate_shouldThrowNotFound_whenTaxClassMissing() {
        TaxRatePostVm postVm = new TaxRatePostVm(7.5, "70000", 999L, 10L, 20L);
        when(taxClassRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.createTaxRate(postVm))
            .isInstanceOf(NotFoundException.class);
        verify(taxRateRepository, never()).save(org.mockito.ArgumentMatchers.any(TaxRate.class));
    }

    @Test
    void findById_shouldReturnVm_whenTaxRateExists() {
        when(taxRateRepository.findById(taxRate.getId())).thenReturn(Optional.of(taxRate));

        TaxRateVm result = taxRateService.findById(taxRate.getId());

        assertThat(result).isEqualTo(TaxRateVm.fromModel(taxRate));
    }

    @Test
    void delete_shouldThrowNotFound_whenTaxRateMissing() {
        when(taxRateRepository.existsById(1234L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.delete(1234L))
            .isInstanceOf(NotFoundException.class);
        verify(taxRateRepository, never()).deleteById(1234L);
    }

    @Test
    void getTaxPercent_shouldReturnValueFromRepository_whenPresent() {
        when(taxRateRepository.getTaxPercent(84L, 12L, "70000", 3L)).thenReturn(8.0);

        double result = taxRateService.getTaxPercent(3L, 84L, 12L, "70000");

        assertThat(result).isEqualTo(8.0);
    }

    @Test
    void getTaxPercent_shouldReturnZero_whenRepositoryReturnsNull() {
        when(taxRateRepository.getTaxPercent(84L, 12L, "70000", 3L)).thenReturn(null);

        double result = taxRateService.getTaxPercent(3L, 84L, 12L, "70000");

        assertThat(result).isZero();
    }
}
