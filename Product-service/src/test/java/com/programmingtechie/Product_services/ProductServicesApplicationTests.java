package com.programmingtechie.Product_services;

import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.dto.ProductResponse;
import com.programmingtechie.Product_services.model.Product;
import com.programmingtechie.Product_services.repository.ProductRepository;
import com.programmingtechie.Product_services.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
class ProductServicesApplicationTests {
//
//	@Test
//	void contextLoads() {
//
//
//	}


	@Mock
	private ProductRepository productRepository;

	private ProductService productService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		productService = new ProductService(productRepository);
	}

	@Test
	void createProduct_shouldSaveProduct() {
		ProductRequest productRequest = new ProductRequest();

		productRequest.setName("Laptop");
		productRequest.setDescription("Gaming Laptop");
		productRequest.setPrice(BigDecimal.valueOf(50000));

		productService.createProduct(productRequest);

		verify(productRepository).save(any(Product.class));
	}


//	@Test
//	void getAllProducts_shouldReturnProductResponses() {
//
//		Product product = Product.builder()
//				.id("123")
//				.name("Laptop")
//				.Description("Gaming Laptop")
//				.price(BigDecimal.valueOf(50000))
//				.build();
//
//		when(productRepository.findAll())
//				.thenReturn(List.of(product));
//
//		List<ProductResponse> result = productService.getAllproduct();
//
//		assertThat(result).hasSize(1);
//		assertThat(result.get(0).getId()).isEqualTo("123");
//		assertThat(result.get(0).getName()).isEqualTo("Laptop");
//		assertThat(result.get(0).getDescription()).isEqualTo("Gaming Laptop");
//		assertThat(result.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(50000));
//
//		verify(productRepository).findAll();
//	}



//	@Test
//	void getAllProducts_shouldReturnEmptyList_whenNoProductsExist() {
//
//		when(productRepository.findAll())
//				.thenReturn(List.of());
//
//		List<ProductResponse> result =
//				productService.getAllproduct();
//
//		assertThat(result).isEmpty();
//
//		verify(productRepository).findAll();
//	}


	@Test
	void getAllProducts_shouldReturnProductResponses() {

		Product product = Product.builder()
				.id("123")
				.name("Laptop")
				.description("Gaming Laptop")
				.price(BigDecimal.valueOf(50000))
				.build();

		when(productRepository.findAll())
				.thenReturn(List.of(product));

		List<ProductResponse> result = productService.getAllproduct();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo("123");
		assertThat(result.get(0).getName()).isEqualTo("Laptop");
		assertThat(result.get(0).getDescription()).isEqualTo("Gaming Laptop");
		assertThat(result.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(50000));

		verify(productRepository).findAll();
	}




	@Test
	void getAllProducts_shouldReturnEmptyList_whenNoProductsExist() {

		when(productRepository.findAll())
				.thenReturn(List.of());

		List<ProductResponse> result =
				productService.getAllproduct();

		assertThat(result).isEmpty();

		verify(productRepository).findAll();
	}


	@Test
	void getProductById_shouldReturnProductResponse_whenProductExists() {

		Product product = Product.builder()
				.id("product-1")
				.name("Laptop")
				.description("Gaming Laptop")
				.price(BigDecimal.valueOf(50000))
				.build();

		when(productRepository.findById("product-1"))
				.thenReturn(Optional.of(product));

		ProductResponse response =
				productService.getProductById("product-1");

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo("product-1");
		assertThat(response.getName()).isEqualTo("Laptop");
		assertThat(response.getDescription()).isEqualTo("Gaming Laptop");
		assertThat(response.getPrice())
				.isEqualByComparingTo(BigDecimal.valueOf(50000));

		verify(productRepository).findById("product-1");
	}


	@Test
	void getProductById_shouldThrowException_whenProductDoesNotExist() {

		when(productRepository.findById("not-found"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() ->
				productService.getProductById("not-found")
		)
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Product not found");

		verify(productRepository).findById("not-found");
	}

}
