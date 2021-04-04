package webflux.security.demo;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class SupplierController {
	
	public static Flux<Supplier> data = Flux.<Supplier>empty();
	
	public SupplierController() {
		
		data = data.concatWithValues(Supplier.builder().id(UUID.randomUUID().toString()).supplierId(1000).description("JACK").build()).
					concatWithValues(Supplier.builder().id(UUID.randomUUID().toString()).supplierId(1001).description("RUDY").build()).
					concatWithValues(Supplier.builder().id(UUID.randomUUID().toString()).supplierId(1001).description("STEPHANE").build()).
					concatWithValues(Supplier.builder().id(UUID.randomUUID().toString()).supplierId(1001).description("NATHAN").build());
	}
	
	/**
	 * @return
	 */
	@GetMapping(value = "/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Flux<Supplier>> getAll() {
		
		log.info(" > Get suppliers.");
		
		return ResponseEntity.ok(data);
	}
	
	@Data @NoArgsConstructor @AllArgsConstructor @Builder
	public static class Supplier {
		
		@Exclude
		private String id;
		
		private Integer supplierId;
		
		@Exclude
		private String description;
	}
}
