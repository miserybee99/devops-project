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
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    void findById_shouldThrowNotFound_whenTaxRateMissing() {
        when(taxRateRepository.findById(1234L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxRateService.findById(1234L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateTaxRate_shouldThrowNotFound_whenTaxClassMissing() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "60000", 99L, 5L, 6L);
        TaxRate existingTaxRate = taxRate;
        existingTaxRate.setId(888L);

        when(taxRateRepository.findById(888L)).thenReturn(Optional.of(existingTaxRate));
        when(taxClassRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.updateTaxRate(postVm, 888L))
            .isInstanceOf(NotFoundException.class);
        verify(taxRateRepository, never()).save(org.mockito.ArgumentMatchers.any(TaxRate.class));
    }

    @Test
    void updateTaxRate_shouldSave_whenTaxClassIdIsUnchanged() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "50000", taxRate.getTaxClass().getId(), 7L, 8L);
        TaxRate existingTaxRate = taxRate;
        existingTaxRate.setId(999L);

        when(taxRateRepository.findById(999L)).thenReturn(Optional.of(existingTaxRate));
        when(taxClassRepository.existsById(taxRate.getTaxClass().getId())).thenReturn(true);
        when(taxClassRepository.getReferenceById(taxRate.getTaxClass().getId()))
            .thenReturn(taxRate.getTaxClass());
        when(taxRateRepository.save(org.mockito.ArgumentMatchers.any(TaxRate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        taxRateService.updateTaxRate(postVm, 999L);

        verify(taxRateRepository).save(existingTaxRate);
        assertThat(existingTaxRate.getRate()).isEqualTo(15.0);
        assertThat(existingTaxRate.getZipCode()).isEqualTo("50000");
        assertThat(existingTaxRate.getStateOrProvinceId()).isEqualTo(7L);
        assertThat(existingTaxRate.getCountryId()).isEqualTo(8L);
        assertThat(existingTaxRate.getTaxClass()).isEqualTo(taxRate.getTaxClass());
    }

    @Test
    void createTaxRate_shouldGetReferenceTaxClassBeforeSave() {
        TaxRatePostVm postVm = new TaxRatePostVm(7.5, "70000", 1L, 10L, 20L);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);

        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);
        when(taxRateRepository.save(org.mockito.ArgumentMatchers.any(TaxRate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TaxRate created = taxRateService.createTaxRate(postVm);

        verify(taxClassRepository).getReferenceById(1L);
        assertThat(created.getTaxClass()).isEqualTo(taxClass);
    }

    @Test
    void getTaxPercent_shouldReturnZero_whenRepositoryReturnsZero() {
        when(taxRateRepository.getTaxPercent(84L, 12L, "70000", 3L)).thenReturn(0.0);

        double result = taxRateService.getTaxPercent(3L, 84L, 12L, "70000");

        assertThat(result).isZero();
    }

    @Test
    void getBulkTaxRate_shouldReturnEmptyWhenNoTaxRates() {
        when(taxRateRepository.getBatchTaxRates(21L, 11L, "70000", new HashSet<>(List.of(3L, 4L))))
            .thenReturn(List.of());

        List<TaxRateVm> bulkTaxRates = taxRateService.getBulkTaxRate(List.of(3L, 4L), 21L, 11L, "70000");

        assertThat(bulkTaxRates).isEmpty();
    }

    @Test
    void getPageableTaxRates_shouldReturnDetailContentForMultipleTaxRates() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(2L);
        taxClass.setName("Reduced");

        TaxRate taxRate1 = TaxRate.builder()
            .id(101L)
            .rate(5.0)
            .zipCode("70000")
            .stateOrProvinceId(11L)
            .countryId(21L)
            .taxClass(taxClass)
            .build();

        TaxRate taxRate2 = TaxRate.builder()
            .id(102L)
            .rate(7.0)
            .zipCode("70001")
            .stateOrProvinceId(11L)
            .countryId(21L)
            .taxClass(taxClass)
            .build();

        Page<TaxRate> pageable = new PageImpl<>(List.of(taxRate1, taxRate2), PageRequest.of(0, 10), 2);
        StateOrProvinceAndCountryGetNameVm locationNameVm = new StateOrProvinceAndCountryGetNameVm(11L,
            "Hanoi",
            "Vietnam");

        when(taxRateRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(pageable);
        when(locationService.getStateOrProvinceAndCountryNames(org.mockito.ArgumentMatchers.any()))
            .thenReturn(List.of(locationNameVm));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertThat(result.taxRateGetDetailContent()).hasSize(2);
        assertThat(result.taxRateGetDetailContent().get(0).stateOrProvinceName()).isEqualTo("Hanoi");
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2);
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

    @Test
    void delete_shouldDelete_whenTaxRateExists() {
        when(taxRateRepository.existsById(1234L)).thenReturn(true);

        taxRateService.delete(1234L);

        verify(taxRateRepository).deleteById(1234L);
    }

    @Test
    void updateTaxRate_shouldThrowNotFound_whenTaxRateMissing() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "60000", 2L, 5L, 6L);
        when(taxRateRepository.findById(987L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxRateService.updateTaxRate(postVm, 987L))
            .isInstanceOf(NotFoundException.class);
        verify(taxRateRepository, never()).save(org.mockito.ArgumentMatchers.any(TaxRate.class));
    }

    @Test
    void updateTaxRate_shouldSave_whenTaxRateExistsAndTaxClassExists() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "50000", 2L, 7L, 8L);
        TaxRate existingTaxRate = taxRate;
        existingTaxRate.setId(999L);
        existingTaxRate.setTaxClass(new TaxClass());
        existingTaxRate.getTaxClass().setId(2L);

        TaxClass newTaxClass = new TaxClass();
        newTaxClass.setId(2L);
        newTaxClass.setName("Reduced");

        when(taxRateRepository.findById(999L)).thenReturn(Optional.of(existingTaxRate));
        when(taxClassRepository.existsById(2L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(2L)).thenReturn(newTaxClass);
        when(taxRateRepository.save(org.mockito.ArgumentMatchers.any(TaxRate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        taxRateService.updateTaxRate(postVm, 999L);

        verify(taxRateRepository).save(existingTaxRate);
        assertThat(existingTaxRate.getRate()).isEqualTo(15.0);
        assertThat(existingTaxRate.getZipCode()).isEqualTo("50000");
        assertThat(existingTaxRate.getTaxClass()).isEqualTo(newTaxClass);
        assertThat(existingTaxRate.getStateOrProvinceId()).isEqualTo(7L);
        assertThat(existingTaxRate.getCountryId()).isEqualTo(8L);
    }

    @Test
    void getPageableTaxRates_shouldReturnPagedDetailContent() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(2L);
        taxClass.setName("Standard");

        TaxRate pageableTaxRate = TaxRate.builder()
            .id(100L)
            .rate(12.5)
            .zipCode("70000")
            .stateOrProvinceId(10L)
            .countryId(20L)
            .taxClass(taxClass)
            .build();

        Page<TaxRate> pageable = new PageImpl<>(List.of(pageableTaxRate), PageRequest.of(0, 10), 1);
        StateOrProvinceAndCountryGetNameVm locationNameVm = new StateOrProvinceAndCountryGetNameVm(10L,
            "Hanoi",
            "Vietnam");

        when(taxRateRepository.findAll(PageRequest.of(0, 10))).thenReturn(pageable);
        when(locationService.getStateOrProvinceAndCountryNames(List.of(10L)))
            .thenReturn(List.of(locationNameVm));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertThat(result.pageNo()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
        assertThat(result.taxRateGetDetailContent()).hasSize(1);
        assertThat(result.taxRateGetDetailContent().get(0).taxClassName()).isEqualTo("Standard");
        assertThat(result.taxRateGetDetailContent().get(0).stateOrProvinceName()).isEqualTo("Hanoi");
        assertThat(result.taxRateGetDetailContent().get(0).countryName()).isEqualTo("Vietnam");
    }

    @Test
    void getBulkTaxRate_shouldReturnVmListForBatchTaxClassIds() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(3L);
        taxClass.setName("Reduced");

        TaxRate taxRate1 = TaxRate.builder()
            .id(101L)
            .rate(5.0)
            .zipCode("70000")
            .stateOrProvinceId(11L)
            .countryId(21L)
            .taxClass(taxClass)
            .build();

        TaxRate taxRate2 = TaxRate.builder()
            .id(102L)
            .rate(7.0)
            .zipCode("70001")
            .stateOrProvinceId(11L)
            .countryId(21L)
            .taxClass(taxClass)
            .build();

        Set<Long> taxClassIds = new HashSet<>(List.of(3L, 4L));
        when(taxRateRepository.getBatchTaxRates(21L, 11L, "70000", taxClassIds))
            .thenReturn(List.of(taxRate1, taxRate2));

        List<TaxRateVm> bulkTaxRates = taxRateService.getBulkTaxRate(List.of(3L, 4L), 21L, 11L, "70000");

        assertThat(bulkTaxRates).hasSize(2);
        assertThat(bulkTaxRates).containsExactly(TaxRateVm.fromModel(taxRate1), TaxRateVm.fromModel(taxRate2));
    }

    @Test
    void getPageableTaxRates_shouldReturnEmptyWhenNoTaxRates() {
        Page<TaxRate> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(taxRateRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertThat(result.pageNo()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();
        assertThat(result.taxRateGetDetailContent()).isEmpty();
        verify(locationService, never()).getStateOrProvinceAndCountryNames(org.mockito.ArgumentMatchers.any());
    }
}
