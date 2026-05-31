package com.yas.product.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailGetVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductEsDetailVm;
import com.yas.product.viewmodel.product.ProductExportingDetailVm;
import com.yas.product.viewmodel.product.ProductFeatureGetVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductInfoVm;
import com.yas.product.viewmodel.product.ProductListGetFromCategoryVm;
import com.yas.product.viewmodel.product.ProductListGetVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductSlugGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import com.yas.product.viewmodel.product.ProductsGetVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionValueRepository productOptionValueRepository;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private Brand brand;
    private Category category;
    private Product product;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("TestBrand");
        brand.setSlug("test-brand");

        category = new Category();
        category.setId(1L);
        category.setName("TestCategory");
        category.setSlug("test-category");

        ProductImage productImage = ProductImage.builder()
            .id(1L)
            .imageId(100L)
            .build();

        ProductCategory productCategory = ProductCategory.builder()
            .id(1L)
            .category(category)
            .build();

        product = Product.builder()
            .id(1L)
            .name("Test Product")
            .slug("test-product")
            .shortDescription("Short desc")
            .description("Description")
            .specification("Spec")
            .sku("SKU001")
            .gtin("GTIN001")
            .price(99.99)
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(true)
            .isVisibleIndividually(true)
            .stockTrackingEnabled(true)
            .stockQuantity(100L)
            .thumbnailMediaId(10L)
            .brand(brand)
            .taxClassId(1L)
            .metaTitle("Meta Title")
            .metaKeyword("Meta Keyword")
            .metaDescription("Meta Description")
            .productCategories(new ArrayList<>(List.of(productCategory)))
            .productImages(new ArrayList<>(List.of(productImage)))
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        productImage.setProduct(product);
        productCategory.setProduct(product);

        noFileMediaVm = new NoFileMediaVm(10L, "caption", "file.jpg", "image/jpeg", "http://url.com/file.jpg");
    }

    // --- 1. getProductsWithFilter ---

    @Test
    void getProductsWithFilter_shouldReturnProductListGetVm() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class)))
            .thenReturn(productPage);

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "Test", "TestBrand");

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
        assertTrue(result.isLast());
    }

    // --- 2. getProductById ---

    @Test
    void getProductById_whenProductExists_shouldReturnProductDetailVm() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Product", result.name());
        assertEquals("test-product", result.slug());
        assertEquals(1L, result.brandId());
        assertNotNull(result.thumbnailMedia());
        assertEquals(1, result.productImageMedias().size());
        assertEquals(1, result.categories().size());
    }

    @Test
    void getProductById_whenProductNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void getProductById_withNullBrandAndNullThumbnail_shouldHandleGracefully() {
        Product noBrandProduct = Product.builder()
            .id(2L)
            .name("No Brand Product")
            .slug("no-brand")
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();
        when(productRepository.findById(2L)).thenReturn(Optional.of(noBrandProduct));

        ProductDetailVm result = productService.getProductById(2L);

        assertNull(result.brandId());
        assertNull(result.thumbnailMedia());
    }

    // --- 3. getLatestProducts ---

    @Test
    void getLatestProducts_withPositiveCount_shouldReturnProducts() {
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getLatestProducts(5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    @Test
    void getLatestProducts_withZeroCount_shouldReturnEmptyList() {
        List<ProductListVm> result = productService.getLatestProducts(0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestProducts_withEmptyResult_shouldReturnEmptyList() {
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(Collections.emptyList());

        List<ProductListVm> result = productService.getLatestProducts(5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- 4. getProductsByBrand ---

    @Test
    void getProductsByBrand_whenBrandExists_shouldReturnProducts() {
        when(brandRepository.findBySlug("test-brand")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        List<ProductThumbnailVm> result = productService.getProductsByBrand("test-brand");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
        assertEquals("http://url.com/file.jpg", result.get(0).thumbnailUrl());
    }

    @Test
    void getProductsByBrand_whenBrandNotFound_shouldThrowNotFoundException() {
        when(brandRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("unknown"));
    }

    // --- 5. getProductsFromCategory ---

    @Test
    void getProductsFromCategory_whenCategoryExists_shouldReturnProducts() {
        ProductCategory pc = ProductCategory.builder().product(product).category(category).build();
        Page<ProductCategory> page = new PageImpl<>(List.of(pc), PageRequest.of(0, 10), 1);

        when(categoryRepository.findBySlug("test-category")).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), eq(category))).thenReturn(page);
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 10, "test-category");

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
        assertEquals(0, result.pageNo());
        assertTrue(result.isLast());
    }

    @Test
    void getProductsFromCategory_whenCategoryNotFound_shouldThrowNotFoundException() {
        when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsFromCategory(0, 10, "unknown"));
    }

    // --- 6. getFeaturedProductsById ---

    @Test
    void getFeaturedProductsById_shouldReturnThumbnailGetVms() {
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
        assertEquals(99.99, result.get(0).price());
    }

    @Test
    void getFeaturedProductsById_whenProductHasEmptyThumbnailAndParent_shouldFallbackToParent() {
        Product parentProduct = Product.builder()
            .id(10L)
            .name("Parent")
            .slug("parent-slug")
            .thumbnailMediaId(20L)
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        Product childProduct = Product.builder()
            .id(2L)
            .name("Child Product")
            .slug("child-slug")
            .price(49.99)
            .thumbnailMediaId(99L)
            .parent(parentProduct)
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        NoFileMediaVm emptyUrlMedia = new NoFileMediaVm(99L, "cap", "f.jpg", "image/jpeg", "");
        NoFileMediaVm parentMedia = new NoFileMediaVm(20L, "cap", "p.jpg", "image/jpeg", "http://parent-url.com/p.jpg");

        when(productRepository.findAllByIdIn(List.of(2L))).thenReturn(List.of(childProduct));
        when(mediaService.getMedia(99L)).thenReturn(emptyUrlMedia);
        when(productRepository.findById(10L)).thenReturn(Optional.of(parentProduct));
        when(mediaService.getMedia(20L)).thenReturn(parentMedia);

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(2L));

        assertEquals(1, result.size());
        assertEquals("http://parent-url.com/p.jpg", result.get(0).thumbnailUrl());
    }

    // --- 7. getListFeaturedProducts ---

    @Test
    void getListFeaturedProducts_shouldReturnProductFeatureGetVm() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(productPage);
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertNotNull(result);
        assertEquals(1, result.productList().size());
        assertEquals(1, result.totalPage());
    }

    // --- 8. getProductDetail ---

    @Test
    void getProductDetail_whenProductExists_shouldReturnProductDetailGetVm() {
        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Product", result.name());
        assertEquals("TestBrand", result.brandName());
        assertEquals(1, result.productCategories().size());
        assertEquals("http://url.com/file.jpg", result.thumbnailMediaUrl());
        assertEquals(1, result.productImageMediaUrls().size());
    }

    @Test
    void getProductDetail_withNullBrand_shouldReturnNullBrandName() {
        Product noBrandProduct = Product.builder()
            .id(3L)
            .name("No Brand")
            .slug("no-brand")
            .thumbnailMediaId(10L)
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        when(productRepository.findBySlugAndIsPublishedTrue("no-brand")).thenReturn(Optional.of(noBrandProduct));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("no-brand");

        assertNull(result.brandName());
    }

    @Test
    void getProductDetail_withAttributeValues_shouldMapAttributeGroups() {
        ProductAttributeGroup attrGroup = new ProductAttributeGroup();
        attrGroup.setId(1L);
        attrGroup.setName("Dimensions");

        ProductAttribute attr = ProductAttribute.builder()
            .id(1L)
            .name("Color")
            .productAttributeGroup(attrGroup)
            .build();

        ProductAttributeValue attrValue = new ProductAttributeValue();
        attrValue.setId(1L);
        attrValue.setProductAttribute(attr);
        attrValue.setValue("Red");

        product.setAttributeValues(List.of(attrValue));

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertNotNull(result.productAttributeGroups());
        assertEquals(1, result.productAttributeGroups().size());
        assertEquals("Dimensions", result.productAttributeGroups().get(0).name());
    }

    @Test
    void getProductDetail_withNullAttributeGroup_shouldUseNoneGroup() {
        ProductAttribute attr = ProductAttribute.builder()
            .id(1L)
            .name("Weight")
            .productAttributeGroup(null)
            .build();

        ProductAttributeValue attrValue = new ProductAttributeValue();
        attrValue.setId(1L);
        attrValue.setProductAttribute(attr);
        attrValue.setValue("500g");

        product.setAttributeValues(List.of(attrValue));

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertNotNull(result.productAttributeGroups());
        assertEquals(1, result.productAttributeGroups().size());
        assertEquals("None group", result.productAttributeGroups().get(0).name());
    }

    @Test
    void getProductDetail_whenNotFound_shouldThrowNotFoundException() {
        when(productRepository.findBySlugAndIsPublishedTrue("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductDetail("missing"));
    }

    // --- 9. deleteProduct ---

    @Test
    void deleteProduct_whenProductExists_shouldUnpublishAndSave() {
        product.setParent(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertFalse(product.isPublished());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenProductHasParentAndCombinations_shouldDeleteCombinations() {
        Product parentProduct = Product.builder().id(99L).build();
        product.setParent(parentProduct);

        ProductOptionCombination combination = ProductOptionCombination.builder()
            .id(1L).product(product).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product))
            .thenReturn(List.of(combination));

        productService.deleteProduct(1L);

        verify(productOptionCombinationRepository).deleteAll(List.of(combination));
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenProductNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.deleteProduct(999L));
    }

    // --- 10. getProductsByMultiQuery ---

    @Test
    void getProductsByMultiQuery_shouldReturnProductsGetVm() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
            anyString(), anyString(), any(), any(), any(Pageable.class)))
            .thenReturn(productPage);
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductsGetVm result = productService.getProductsByMultiQuery(0, 10, "Test", "test-category", 0.0, 200.0);

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
        assertEquals(0, result.pageNo());
        assertTrue(result.isLast());
    }

    // --- 11. getProductVariationsByParentId ---

    @Test
    void getProductVariationsByParentId_whenHasOptions_shouldReturnVariations() {
        ProductOption option = new ProductOption();
        option.setId(1L);
        option.setName("Size");

        Product variation = Product.builder()
            .id(2L)
            .name("Variation 1")
            .slug("var-1")
            .sku("SKU-VAR")
            .gtin("GTIN-VAR")
            .price(109.99)
            .isPublished(true)
            .thumbnailMediaId(20L)
            .productImages(new ArrayList<>())
            .build();

        product.setHasOptions(true);
        product.setProducts(List.of(variation));

        ProductOptionCombination combo = ProductOptionCombination.builder()
            .id(1L)
            .product(variation)
            .productOption(option)
            .value("Large")
            .displayOrder(1)
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combo));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Variation 1", result.get(0).name());
        assertEquals("Large", result.get(0).options().get(1L));
    }

    @Test
    void getProductVariationsByParentId_whenNoOptions_shouldReturnEmptyList() {
        product.setHasOptions(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProductVariationsByParentId_whenNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductVariationsByParentId(999L));
    }

    // --- 12. exportProducts ---

    @Test
    void exportProducts_shouldReturnExportingDetails() {
        when(productRepository.getExportingProducts(anyString(), anyString())).thenReturn(List.of(product));

        List<ProductExportingDetailVm> result = productService.exportProducts("Test", "TestBrand");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Test Product", result.get(0).name());
        assertEquals(1L, result.get(0).brandId());
        assertEquals("TestBrand", result.get(0).brandName());
    }

    // --- 13. getProductSlug ---

    @Test
    void getProductSlug_whenProductHasNoParent_shouldReturnOwnSlug() {
        product.setParent(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertEquals("test-product", result.slug());
        assertNull(result.productVariantId());
    }

    @Test
    void getProductSlug_whenProductHasParent_shouldReturnParentSlug() {
        Product parentProduct = Product.builder()
            .id(10L)
            .slug("parent-slug")
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();
        product.setParent(parentProduct);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertEquals("parent-slug", result.slug());
        assertEquals(1L, result.productVariantId());
    }

    @Test
    void getProductSlug_whenNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductSlug(999L));
    }

    // --- 14. getProductEsDetailById ---

    @Test
    void getProductEsDetailById_whenProductExists_shouldReturnEsDetailVm() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Product", result.name());
        assertEquals("TestBrand", result.brand());
        assertEquals(10L, result.thumbnailMediaId());
        assertEquals(1, result.categories().size());
    }

    @Test
    void getProductEsDetailById_withNullBrandAndNullThumbnail_shouldHandleNulls() {
        Product noBrandProduct = Product.builder()
            .id(2L)
            .name("No Brand")
            .slug("no-brand")
            .price(10.0)
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(noBrandProduct));

        ProductEsDetailVm result = productService.getProductEsDetailById(2L);

        assertNull(result.brand());
        assertNull(result.thumbnailMediaId());
    }

    @Test
    void getProductEsDetailById_whenNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductEsDetailById(999L));
    }

    // --- 15. getRelatedProductsBackoffice ---

    @Test
    void getRelatedProductsBackoffice_shouldReturnRelatedProducts() {
        Product relatedProduct = Product.builder()
            .id(2L)
            .name("Related Product")
            .slug("related-product")
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(false)
            .isVisibleIndividually(true)
            .price(49.99)
            .taxClassId(1L)
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        ProductRelated productRelated = ProductRelated.builder()
            .id(1L)
            .product(product)
            .relatedProduct(relatedProduct)
            .build();

        product.setRelatedProducts(List.of(productRelated));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Related Product", result.get(0).name());
    }

    @Test
    void getRelatedProductsBackoffice_whenNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getRelatedProductsBackoffice(999L));
    }

    // --- 16. getRelatedProductsStorefront ---

    @Test
    void getRelatedProductsStorefront_shouldReturnProductsGetVm() {
        Product relatedProduct = Product.builder()
            .id(2L)
            .name("Related")
            .slug("related")
            .isPublished(true)
            .price(59.99)
            .thumbnailMediaId(20L)
            .productCategories(new ArrayList<>())
            .productImages(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        ProductRelated pr = ProductRelated.builder()
            .id(1L).product(product).relatedProduct(relatedProduct).build();

        Page<ProductRelated> page = new PageImpl<>(List.of(pr), PageRequest.of(0, 10), 1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRelatedRepository.findAllByProduct(eq(product), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
        assertTrue(result.isLast());
    }

    @Test
    void getRelatedProductsStorefront_whenNotFound_shouldThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getRelatedProductsStorefront(999L, 0, 10));
    }

    // --- 17. getProductsForWarehouse ---

    @Test
    void getProductsForWarehouse_shouldReturnProductInfoVms() {
        when(productRepository.findProductForWarehouse(anyString(), anyString(), anyList(), anyString()))
            .thenReturn(List.of(product));

        List<ProductInfoVm> result = productService.getProductsForWarehouse(
            "Test", "SKU", List.of(1L), FilterExistInWhSelection.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
        assertEquals("SKU001", result.get(0).sku());
    }

    // --- 18. updateProductQuantity ---

    @Test
    void updateProductQuantity_shouldUpdateStockQuantities() {
        ProductQuantityPostVm quantityPostVm = new ProductQuantityPostVm(1L, 200L);
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        productService.updateProductQuantity(List.of(quantityPostVm));

        assertEquals(200L, product.getStockQuantity());
        verify(productRepository).saveAll(List.of(product));
    }

    // --- 19. subtractStockQuantity ---

    @Test
    void subtractStockQuantity_shouldDecreaseStock() {
        product.setStockQuantity(100L);
        product.setStockTrackingEnabled(true);

        ProductQuantityPutVm quantityPutVm = new ProductQuantityPutVm(1L, 30L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(quantityPutVm));

        assertEquals(70L, product.getStockQuantity());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void subtractStockQuantity_whenResultNegative_shouldSetToZero() {
        product.setStockQuantity(10L);
        product.setStockTrackingEnabled(true);

        ProductQuantityPutVm quantityPutVm = new ProductQuantityPutVm(1L, 20L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(quantityPutVm));

        assertEquals(0L, product.getStockQuantity());
    }

    // --- 20. restoreStockQuantity ---

    @Test
    void restoreStockQuantity_shouldIncreaseStock() {
        product.setStockQuantity(50L);
        product.setStockTrackingEnabled(true);

        ProductQuantityPutVm quantityPutVm = new ProductQuantityPutVm(1L, 25L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(quantityPutVm));

        assertEquals(75L, product.getStockQuantity());
        verify(productRepository).saveAll(List.of(product));
    }

    // --- 21. getProductByIds ---

    @Test
    void getProductByIds_shouldReturnProductListVms() {
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByIds(List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    // --- 22. getProductByCategoryIds ---

    @Test
    void getProductByCategoryIds_shouldReturnProductListVms() {
        when(productRepository.findByCategoryIdsIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByCategoryIds(List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // --- 23. getProductByBrandIds ---

    @Test
    void getProductByBrandIds_shouldReturnProductListVms() {
        when(productRepository.findByBrandIdsIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByBrandIds(List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // --- 24. getProductCheckoutList ---

    @Test
    void getProductCheckoutList_shouldReturnCheckoutListVm() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class)))
            .thenReturn(productPage);
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.productCheckoutListVms().size());
        assertEquals(0, result.pageNo());
        assertTrue(result.isLast());
    }

    @Test
    void getProductCheckoutList_withEmptyThumbnailUrl_shouldKeepDefaultUrl() {
        NoFileMediaVm emptyUrlMedia = new NoFileMediaVm(10L, "cap", "f.jpg", "image/jpeg", "");
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class)))
            .thenReturn(productPage);
        when(mediaService.getMedia(anyLong())).thenReturn(emptyUrlMedia);

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.productCheckoutListVms().size());
    }

    // --- 25. setProductImages ---

    @Test
    void setProductImages_withNullImageMediaIds_shouldDeleteAndReturnEmpty() {
        List<ProductImage> result = productService.setProductImages(null, product);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productImageRepository).deleteByProductId(product.getId());
    }

    @Test
    void setProductImages_withEmptyList_shouldDeleteAndReturnEmpty() {
        List<ProductImage> result = productService.setProductImages(Collections.emptyList(), product);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productImageRepository).deleteByProductId(product.getId());
    }

    @Test
    void setProductImages_whenProductHasNoExistingImages_shouldCreateNewImages() {
        Product noImageProduct = Product.builder()
            .id(3L)
            .name("No Image")
            .slug("no-image")
            .productCategories(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .relatedProducts(new ArrayList<>())
            .build();

        List<ProductImage> result = productService.setProductImages(List.of(200L, 201L), noImageProduct);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void setProductImages_withNewAndDeletedImages_shouldHandleBoth() {
        ProductImage existingImage = ProductImage.builder().id(1L).imageId(100L).product(product).build();
        product.setProductImages(List.of(existingImage));

        List<ProductImage> result = productService.setProductImages(List.of(100L, 200L), product);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getImageId());
    }

    @Test
    void setProductImages_removingImages_shouldDeleteRemovedOnes() {
        ProductImage img1 = ProductImage.builder().id(1L).imageId(100L).product(product).build();
        ProductImage img2 = ProductImage.builder().id(2L).imageId(101L).product(product).build();
        product.setProductImages(List.of(img1, img2));

        List<ProductImage> result = productService.setProductImages(List.of(100L), product);

        verify(productImageRepository).deleteByImageIdInAndProductId(List.of(101L), product.getId());
    }
}
