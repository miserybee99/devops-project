package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    @Test
    void getProductDetailById_Success_withoutOptions() {
        long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("Simple Product")
                .shortDescription("Short desc")
                .description("Full description")
                .specification("Spec")
                .sku("SKU-001")
                .gtin("GTIN-001")
                .slug("simple-product")
                .isAllowedToOrder(true)
                .isPublished(true)
                .isFeatured(false)
                .isVisibleIndividually(true)
                .stockTrackingEnabled(false)
                .price(29.99)
                .taxClassId(1L)
                .metaTitle("Meta Title")
                .metaKeyword("keyword")
                .metaDescription("meta desc")
                .hasOptions(false)
                .productCategories(new ArrayList<>())
                .attributeValues(new ArrayList<>())
                .productImages(new ArrayList<>())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(productId);

        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Simple Product");
        assertThat(result.getSlug()).isEqualTo("simple-product");
        assertThat(result.getPrice()).isEqualTo(29.99);
        assertThat(result.getIsPublished()).isTrue();
        assertThat(result.getBrandId()).isNull();
        assertThat(result.getBrandName()).isNull();
        assertThat(result.getCategories()).isEmpty();
        assertThat(result.getAttributeValues()).isEmpty();
        assertThat(result.getVariations()).isEmpty();
        assertThat(result.getThumbnail()).isNull();
        assertThat(result.getProductImages()).isEmpty();
    }

    @Test
    void getProductDetailById_Success_withBrandAndCategories() {
        long productId = 2L;

        Brand brand = new Brand();
        brand.setId(10L);
        brand.setName("Nike");

        Category category1 = new Category();
        category1.setId(100L);
        category1.setName("Shoes");

        Category category2 = new Category();
        category2.setId(101L);
        category2.setName("Running");

        ProductCategory pc1 = ProductCategory.builder().category(category1).build();
        ProductCategory pc2 = ProductCategory.builder().category(category2).build();

        Product product = Product.builder()
                .id(productId)
                .name("Nike Runner")
                .shortDescription("Fast shoes")
                .description("Very fast running shoes")
                .specification("Lightweight")
                .sku("NIKE-RUN-001")
                .gtin("GTIN-NIKE")
                .slug("nike-runner")
                .isAllowedToOrder(true)
                .isPublished(true)
                .isFeatured(true)
                .isVisibleIndividually(true)
                .stockTrackingEnabled(true)
                .price(149.99)
                .taxClassId(2L)
                .metaTitle("Nike Runner")
                .metaKeyword("nike,running")
                .metaDescription("Nike running shoes")
                .hasOptions(false)
                .brand(brand)
                .productCategories(List.of(pc1, pc2))
                .attributeValues(new ArrayList<>())
                .productImages(new ArrayList<>())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(productId);

        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Nike Runner");
        assertThat(result.getBrandId()).isEqualTo(10L);
        assertThat(result.getBrandName()).isEqualTo("Nike");
        assertThat(result.getCategories()).hasSize(2);
        assertThat(result.getCategories()).extracting(Category::getName)
                .containsExactly("Shoes", "Running");
        assertThat(result.getIsFeatured()).isTrue();
        assertThat(result.getStockTrackingEnabled()).isTrue();
    }

    @Test
    void getProductDetailById_Success_withVariations() {
        long productId = 3L;

        ProductOption colorOption = new ProductOption();
        colorOption.setId(50L);
        colorOption.setName("Color");

        ProductOption sizeOption = new ProductOption();
        sizeOption.setId(51L);
        sizeOption.setName("Size");

        Product variation1 = Product.builder()
                .id(31L)
                .name("T-Shirt Red M")
                .slug("tshirt-red-m")
                .sku("TS-RED-M")
                .gtin("GTIN-V1")
                .price(25.00)
                .isPublished(true)
                .productImages(new ArrayList<>())
                .build();

        Product variation2 = Product.builder()
                .id(32L)
                .name("T-Shirt Blue L")
                .slug("tshirt-blue-l")
                .sku("TS-BLUE-L")
                .gtin("GTIN-V2")
                .price(25.00)
                .isPublished(true)
                .productImages(new ArrayList<>())
                .build();

        Product unpublishedVariation = Product.builder()
                .id(33L)
                .name("T-Shirt Green S")
                .slug("tshirt-green-s")
                .sku("TS-GREEN-S")
                .gtin("GTIN-V3")
                .price(25.00)
                .isPublished(false)
                .productImages(new ArrayList<>())
                .build();

        ProductOptionCombination combo1Color = ProductOptionCombination.builder()
                .productOption(colorOption).value("Red").build();
        ProductOptionCombination combo1Size = ProductOptionCombination.builder()
                .productOption(sizeOption).value("M").build();

        ProductOptionCombination combo2Color = ProductOptionCombination.builder()
                .productOption(colorOption).value("Blue").build();
        ProductOptionCombination combo2Size = ProductOptionCombination.builder()
                .productOption(sizeOption).value("L").build();

        Product product = Product.builder()
                .id(productId)
                .name("T-Shirt")
                .shortDescription("Cotton T-Shirt")
                .description("Comfortable cotton t-shirt")
                .specification("100% cotton")
                .sku("TS-MAIN")
                .gtin("GTIN-MAIN")
                .slug("t-shirt")
                .isAllowedToOrder(true)
                .isPublished(true)
                .isFeatured(false)
                .isVisibleIndividually(true)
                .stockTrackingEnabled(false)
                .price(25.00)
                .taxClassId(1L)
                .metaTitle("T-Shirt")
                .metaKeyword("tshirt")
                .metaDescription("Cotton tshirt")
                .hasOptions(true)
                .products(List.of(variation1, variation2, unpublishedVariation))
                .productCategories(new ArrayList<>())
                .attributeValues(new ArrayList<>())
                .productImages(new ArrayList<>())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation1))
                .thenReturn(List.of(combo1Color, combo1Size));
        when(productOptionCombinationRepository.findAllByProduct(variation2))
                .thenReturn(List.of(combo2Color, combo2Size));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(productId);

        assertThat(result.getVariations()).hasSize(2);

        assertThat(result.getVariations().get(0).id()).isEqualTo(31L);
        assertThat(result.getVariations().get(0).name()).isEqualTo("T-Shirt Red M");
        assertThat(result.getVariations().get(0).options())
                .containsEntry(50L, "Red")
                .containsEntry(51L, "M");

        assertThat(result.getVariations().get(1).id()).isEqualTo(32L);
        assertThat(result.getVariations().get(1).name()).isEqualTo("T-Shirt Blue L");
        assertThat(result.getVariations().get(1).options())
                .containsEntry(50L, "Blue")
                .containsEntry(51L, "L");

        verify(productOptionCombinationRepository, never()).findAllByProduct(unpublishedVariation);
    }

    @Test
    void getProductDetailById_ProductNotFound() {
        long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailService.getProductDetailById(productId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getProductDetailById_ProductNotPublished() {
        long productId = 4L;
        Product product = Product.builder()
                .id(productId)
                .name("Draft Product")
                .isPublished(false)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productDetailService.getProductDetailById(productId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getProductDetailById_withThumbnailAndImages() {
        long productId = 5L;
        Long thumbnailMediaId = 200L;
        Long imageId1 = 201L;
        Long imageId2 = 202L;

        ProductImage img1 = ProductImage.builder().imageId(imageId1).build();
        ProductImage img2 = ProductImage.builder().imageId(imageId2).build();

        Product product = Product.builder()
                .id(productId)
                .name("Product with Media")
                .shortDescription("Has images")
                .description("Product with thumbnail and images")
                .specification("Spec")
                .sku("MEDIA-001")
                .gtin("GTIN-MEDIA")
                .slug("product-with-media")
                .isAllowedToOrder(true)
                .isPublished(true)
                .isFeatured(false)
                .isVisibleIndividually(true)
                .stockTrackingEnabled(false)
                .price(59.99)
                .taxClassId(1L)
                .metaTitle("Product with Media")
                .metaKeyword("media")
                .metaDescription("product media desc")
                .hasOptions(false)
                .thumbnailMediaId(thumbnailMediaId)
                .productCategories(new ArrayList<>())
                .attributeValues(new ArrayList<>())
                .productImages(List.of(img1, img2))
                .build();

        NoFileMediaVm thumbnailMedia = new NoFileMediaVm(
                thumbnailMediaId, "Thumbnail", "thumb.jpg", "image/jpeg",
                "http://media.example.com/thumb.jpg"
        );
        NoFileMediaVm imageMedia1 = new NoFileMediaVm(
                imageId1, "Image 1", "img1.jpg", "image/jpeg",
                "http://media.example.com/img1.jpg"
        );
        NoFileMediaVm imageMedia2 = new NoFileMediaVm(
                imageId2, "Image 2", "img2.jpg", "image/jpeg",
                "http://media.example.com/img2.jpg"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(thumbnailMediaId)).thenReturn(thumbnailMedia);
        when(mediaService.getMedia(imageId1)).thenReturn(imageMedia1);
        when(mediaService.getMedia(imageId2)).thenReturn(imageMedia2);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(productId);

        assertThat(result.getThumbnail()).isNotNull();
        assertThat(result.getThumbnail().id()).isEqualTo(thumbnailMediaId);
        assertThat(result.getThumbnail().url()).isEqualTo("http://media.example.com/thumb.jpg");

        assertThat(result.getProductImages()).hasSize(2);
        assertThat(result.getProductImages().get(0).id()).isEqualTo(imageId1);
        assertThat(result.getProductImages().get(0).url()).isEqualTo("http://media.example.com/img1.jpg");
        assertThat(result.getProductImages().get(1).id()).isEqualTo(imageId2);
        assertThat(result.getProductImages().get(1).url()).isEqualTo("http://media.example.com/img2.jpg");

        verify(mediaService).getMedia(thumbnailMediaId);
        verify(mediaService).getMedia(imageId1);
        verify(mediaService).getMedia(imageId2);
    }
}
