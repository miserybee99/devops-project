package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.category.CategoryGetDetailVm;
import com.yas.product.viewmodel.category.CategoryGetVm;
import com.yas.product.viewmodel.category.CategoryListGetVm;
import com.yas.product.viewmodel.category.CategoryPostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MediaService mediaService;

    private CategoryService categoryService;

    private Category category;
    private Category parentCategory;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, mediaService);

        parentCategory = new Category();
        parentCategory.setId(100L);
        parentCategory.setName("Parent");
        parentCategory.setSlug("parent-slug");

        category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setSlug("test-category");
        category.setDescription("Test description");
        category.setMetaKeyword("keyword");
        category.setMetaDescription("meta desc");
        category.setDisplayOrder((short) 1);
        category.setIsPublished(true);
        category.setImageId(10L);
    }

    // ========== create() tests ==========

    @Test
    void create_happyPath_returnsCategory() {
        CategoryPostVm postVm = new CategoryPostVm(
            "New Category", "new-category", "desc", null,
            "meta keywords", "meta description", (short) 1, true, 5L
        );

        when(categoryRepository.findExistedName("New Category", null)).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.create(postVm);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        assertEquals("new-category", result.getSlug());
        assertEquals("desc", result.getDescription());
        assertNull(result.getParent());
        assertEquals(5L, result.getImageId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_withParentCategory_setsParent() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Child Category", "child-category", "desc", 100L,
            "meta", "meta desc", (short) 2, true, null
        );

        when(categoryRepository.findExistedName("Child Category", null)).thenReturn(null);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.create(postVm);

        assertNotNull(result);
        assertEquals(parentCategory, result.getParent());
        verify(categoryRepository).findById(100L);
    }

    @Test
    void create_withParentNotFound_throwsBadRequestException() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Child Category", "child-category", "desc", 999L,
            "meta", "meta desc", (short) 2, true, null
        );

        when(categoryRepository.findExistedName("Child Category", null)).thenReturn(null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> categoryService.create(postVm));
    }

    @Test
    void create_duplicateName_throwsDuplicatedException() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Existing Name", "existing-name", "desc", null,
            "meta", "meta desc", (short) 1, true, null
        );

        Category existingCategory = new Category();
        existingCategory.setName("Existing Name");
        when(categoryRepository.findExistedName("Existing Name", null)).thenReturn(existingCategory);

        assertThrows(DuplicatedException.class, () -> categoryService.create(postVm));
    }

    // ========== update() tests ==========

    @Test
    void update_happyPath_updatesCategory() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Updated Name", "updated-slug", "updated desc", null,
            "updated meta", "updated meta desc", (short) 3, false, 20L
        );

        when(categoryRepository.findExistedName("Updated Name", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.update(postVm, 1L);

        assertEquals("Updated Name", category.getName());
        assertEquals("updated-slug", category.getSlug());
        assertEquals("updated desc", category.getDescription());
        assertEquals((short) 3, category.getDisplayOrder());
        assertEquals("updated meta desc", category.getMetaDescription());
        assertEquals("updated meta", category.getMetaKeyword());
        assertEquals(false, category.getIsPublished());
        assertEquals(20L, category.getImageId());
        assertNull(category.getParent());
    }

    @Test
    void update_categoryNotFound_throwsNotFoundException() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Name", "slug", "desc", null,
            "meta", "meta desc", (short) 1, true, null
        );

        when(categoryRepository.findExistedName("Name", 999L)).thenReturn(null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.update(postVm, 999L));
    }

    @Test
    void update_parentIsItself_throwsBadRequestException() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Test Category", "test-category", "desc", 1L,
            "meta", "meta desc", (short) 1, true, null
        );

        Category selfCategory = new Category();
        selfCategory.setId(1L);
        selfCategory.setName("Test Category");

        when(categoryRepository.findExistedName("Test Category", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(selfCategory));

        assertThrows(BadRequestException.class, () -> categoryService.update(postVm, 1L));
    }

    @Test
    void update_withNullParentId_setsParentToNull() {
        category.setParent(parentCategory);
        CategoryPostVm postVm = new CategoryPostVm(
            "Test Category", "test-category", "desc", null,
            "meta", "meta desc", (short) 1, true, 10L
        );

        when(categoryRepository.findExistedName("Test Category", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.update(postVm, 1L);

        assertNull(category.getParent());
    }

    @Test
    void update_withValidParent_setsParent() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Test Category", "test-category", "desc", 100L,
            "meta", "meta desc", (short) 1, true, 10L
        );

        when(categoryRepository.findExistedName("Test Category", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(parentCategory));

        categoryService.update(postVm, 1L);

        assertEquals(parentCategory, category.getParent());
    }

    @Test
    void update_duplicateName_throwsDuplicatedException() {
        CategoryPostVm postVm = new CategoryPostVm(
            "Duplicate", "slug", "desc", null,
            "meta", "meta desc", (short) 1, true, null
        );

        Category existing = new Category();
        existing.setName("Duplicate");
        when(categoryRepository.findExistedName("Duplicate", 1L)).thenReturn(existing);

        assertThrows(DuplicatedException.class, () -> categoryService.update(postVm, 1L));
    }

    // ========== getCategoryByIds() tests ==========

    @Test
    void getCategoryByIds_returnsMappedList() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Cat 1");
        cat1.setSlug("cat-1");

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Cat 2");
        cat2.setSlug("cat-2");

        List<Long> ids = List.of(1L, 2L);
        when(categoryRepository.findAllById(ids)).thenReturn(List.of(cat1, cat2));

        List<CategoryGetVm> result = categoryService.getCategoryByIds(ids);

        assertEquals(2, result.size());
        assertEquals("Cat 1", result.get(0).name());
        assertEquals("Cat 2", result.get(1).name());
    }

    @Test
    void getCategoryByIds_emptyList_returnsEmpty() {
        List<Long> ids = List.of();
        when(categoryRepository.findAllById(ids)).thenReturn(List.of());

        List<CategoryGetVm> result = categoryService.getCategoryByIds(ids);

        assertTrue(result.isEmpty());
    }

    // ========== getTopNthCategories() tests ==========

    @Test
    void getTopNthCategories_returnsLimitedList() {
        List<String> categoryNames = List.of("Electronics", "Clothing", "Books");
        when(categoryRepository.findCategoriesOrderedByProductCount(any(Pageable.class)))
            .thenReturn(categoryNames);

        List<String> result = categoryService.getTopNthCategories(3);

        assertEquals(3, result.size());
        assertEquals("Electronics", result.get(0));
        assertEquals("Clothing", result.get(1));
        assertEquals("Books", result.get(2));
    }

    @Test
    void getTopNthCategories_returnsEmptyWhenNoCategories() {
        when(categoryRepository.findCategoriesOrderedByProductCount(any(Pageable.class)))
            .thenReturn(List.of());

        List<String> result = categoryService.getTopNthCategories(5);

        assertTrue(result.isEmpty());
    }

    // ========== getPageableCategories() tests ==========

    @Test
    void getPageableCategories_returnsCategoryListGetVm() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Cat 1");
        cat1.setSlug("cat-1");

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Cat 2");
        cat2.setSlug("cat-2");

        Page<Category> page = new PageImpl<>(
            List.of(cat1, cat2),
            PageRequest.of(0, 10),
            2
        );
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        CategoryListGetVm result = categoryService.getPageableCategories(0, 10);

        assertNotNull(result);
        assertEquals(2, result.categoryContent().size());
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
    }

    @Test
    void getPageableCategories_emptyPage_returnsEmptyList() {
        Page<Category> emptyPage = new PageImpl<>(
            List.of(),
            PageRequest.of(0, 10),
            0
        );
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        CategoryListGetVm result = categoryService.getPageableCategories(0, 10);

        assertNotNull(result);
        assertTrue(result.categoryContent().isEmpty());
        assertEquals(0, result.totalElements());
    }

    // ========== getCategoryById() tests ==========

    @Test
    void getCategoryById_withImageAndParent_returnsDetail() {
        category.setParent(parentCategory);
        NoFileMediaVm mediaVm = new NoFileMediaVm(10L, "caption", "file.png", "image/png", "http://example.com/file.png");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mediaService.getMedia(10L)).thenReturn(mediaVm);

        CategoryGetDetailVm result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Category", result.name());
        assertEquals("test-category", result.slug());
        assertEquals("Test description", result.description());
        assertEquals(100L, result.parentId());
        assertNotNull(result.categoryImage());
        assertEquals(10L, result.categoryImage().id());
        assertEquals("http://example.com/file.png", result.categoryImage().url());
    }

    @Test
    void getCategoryById_withoutImage_returnsDetailWithNullImage() {
        category.setImageId(null);
        category.setParent(parentCategory);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryGetDetailVm result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertNull(result.categoryImage());
        assertEquals(100L, result.parentId());
    }

    @Test
    void getCategoryById_withoutParent_returnsZeroParentId() {
        category.setParent(null);
        NoFileMediaVm mediaVm = new NoFileMediaVm(10L, "caption", "file.png", "image/png", "http://example.com/file.png");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mediaService.getMedia(10L)).thenReturn(mediaVm);

        CategoryGetDetailVm result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(0L, result.parentId());
    }

    @Test
    void getCategoryById_notFound_throwsNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    // ========== getCategories() tests ==========

    @Test
    void getCategories_withImage_returnsCategoryGetVmList() {
        category.setParent(parentCategory);
        NoFileMediaVm mediaVm = new NoFileMediaVm(10L, "caption", "file.png", "image/png", "http://example.com/file.png");

        when(categoryRepository.findByNameContainingIgnoreCase("Test")).thenReturn(List.of(category));
        when(mediaService.getMedia(10L)).thenReturn(mediaVm);

        List<CategoryGetVm> result = categoryService.getCategories("Test");

        assertEquals(1, result.size());
        CategoryGetVm vm = result.get(0);
        assertEquals(1L, vm.id());
        assertEquals("Test Category", vm.name());
        assertEquals("test-category", vm.slug());
        assertEquals(100L, vm.parentId());
        assertNotNull(vm.categoryImage());
        assertEquals(10L, vm.categoryImage().id());
        assertEquals("http://example.com/file.png", vm.categoryImage().url());
    }

    @Test
    void getCategories_withoutImage_returnsNullCategoryImage() {
        category.setImageId(null);
        category.setParent(null);

        when(categoryRepository.findByNameContainingIgnoreCase("Test")).thenReturn(List.of(category));

        List<CategoryGetVm> result = categoryService.getCategories("Test");

        assertEquals(1, result.size());
        CategoryGetVm vm = result.get(0);
        assertNull(vm.categoryImage());
        assertEquals(-1L, vm.parentId());
    }

    @Test
    void getCategories_noResults_returnsEmptyList() {
        when(categoryRepository.findByNameContainingIgnoreCase("nonexistent")).thenReturn(List.of());

        List<CategoryGetVm> result = categoryService.getCategories("nonexistent");

        assertTrue(result.isEmpty());
    }
}
