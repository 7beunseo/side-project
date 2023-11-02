package com.springboot.security.controller;


import com.springboot.security.data.dto.ChangeProductNameDto;
import com.springboot.security.data.dto.ProductDto;
import com.springboot.security.data.dto.ProductResponseDto;
import com.springboot.security.service.ProductService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("{number}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long number) {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("[getProduct] Request Data :: productId : {}", number);
        ProductResponseDto productResponseDto = productService.getProduct(number);

        if (productResponseDto == null) {
            // 상품을 찾지 못한 경우 404 에러 응답을 반환
            return ResponseEntity.notFound().build();
        }

        LOGGER.info(
                "[getProduct] Response Data :: productId : {}, productContent : {}, productFilename : {}",
                productResponseDto.getNumber(), productResponseDto.getContent(), productResponseDto.getFilename()
        );
        LOGGER.info("[getProduct] Response Time : {}ms", System.currentTimeMillis() - currentTime);

        return ResponseEntity.ok(productResponseDto);
    }

    @GetMapping() // 엔드포인트 경로 설정
    public List<ProductResponseDto> getProductList() {
        return productService.getProductList();
    }

    @Transactional
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급 받은 access_token", required = true, dataType = "String", paramType = "header")
    })
    @PostMapping()
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductDto productDto) {
        long currentTime = System.currentTimeMillis();
        ProductResponseDto productResponseDto = productService.saveProduct(productDto);

        LOGGER.info("[createProduct] Response Time : {}ms", System.currentTimeMillis() - currentTime);
        return ResponseEntity.status(HttpStatus.OK).body(productResponseDto);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급 받은 access_token", required = true, dataType = "String", paramType = "header")
    })
    @PutMapping()
    public ResponseEntity<ProductResponseDto> changeProductName(
        @RequestBody ChangeProductNameDto changeProductNameDto) throws Exception {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("[changeProductName] request Data :: productNumber : {}, productName : {}",
            changeProductNameDto.getNumber(), changeProductNameDto.getContent());

        ProductResponseDto productResponseDto = productService.changeProductContent(
            changeProductNameDto.getNumber(),
            changeProductNameDto.getContent());

        LOGGER.info(
            "[changeProductName] response Data :: productNumber : {}, productName : {}, productPrice : {}, productStock : {}",
            productResponseDto.getNumber(), productResponseDto.getContent(),
            productResponseDto.getFilename());
        LOGGER.info("[changeProductName] response Time : {}ms",
            System.currentTimeMillis() - currentTime);
        return ResponseEntity.status(HttpStatus.OK).body(productResponseDto);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급 받은 access_token", required = true, dataType = "String", paramType = "header")
    })
    @DeleteMapping()
    public ResponseEntity<String> deleteProduct(Long number) throws Exception {
        long currentTime = System.currentTimeMillis();
        LOGGER.info("[deleteProduct] request Data :: productNumber : {}", number);

        productService.deleteProduct(number);

        LOGGER.info("[deleteProduct] response Time : {}ms",
            System.currentTimeMillis() - currentTime);
        return ResponseEntity.status(HttpStatus.OK).body("정상적으로 삭제되었습니다.");
    }

}
