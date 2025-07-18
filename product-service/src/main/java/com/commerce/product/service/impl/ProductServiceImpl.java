package com.commerce.product.service.impl;

import com.commerce.common.dto.IdsDTO;
import com.commerce.common.dto.ProductResponseDTO;
import com.commerce.common.dto.StockUpdateBatchDTO;
import com.commerce.common.exception.ConflictException;
import com.commerce.common.exception.ResourceNotFoundException;
import com.commerce.product.dtos.ProductCreateDTO;
import com.commerce.product.dtos.ProductUpdateDTO;
import com.commerce.product.models.ProductDAO;
import com.commerce.product.repository.ProductRepository;
import com.commerce.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository r, ModelMapper modelMapper) {
        this.productRepository = r;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDAO create(ProductCreateDTO productCreateDTO) {
        // Check if product with same name/sku already exists
        if (productRepository.existsByName(productCreateDTO.getName())) {
            throw new ConflictException("Product with this name already exists");
        }

        ProductDAO u = modelMapper.map(productCreateDTO, ProductDAO.class);
        log.info("Creating product: {}", u);
        return productRepository.save(u);
    }

    /*@Override
    public ProductDAO find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Product not found"));
    }*/
    @Override
    public ProductDAO find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public ResponseEntity<List<ProductResponseDTO>> findProducts(IdsDTO idsDTO) {
        List<ProductDAO> productDAOS = productRepository.findProductByIdIn(idsDTO.getIds());
        List<ProductResponseDTO> responseDTOS = productDAOS.stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .toList();
        return ResponseEntity.ok(responseDTOS);
    }


    @Override
    public List<ProductDAO> findAll() {
        return productRepository.findAll();
    }

    @Override
    public ProductDAO decreaseStock(Long productId, int quantity) {
        ProductDAO productDAO = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException("Product not found"));

        if (productDAO.getStock() < quantity) {
            log.error("Insufficient stock for product ID {}: available {}, requested {}",
                    productId, productDAO.getStock(), quantity);
            throw new ServiceException(
                    "Available stock: " + productDAO.getStock() + ", requested: " + quantity
            );
        }
        productDAO.setStock(productDAO.getStock() - quantity);
        return productRepository.save(productDAO);
    }

    @Override
    public ProductDAO increaseStock(Long productId, int quantity) {
        ProductDAO productDAO = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException("Product not found"));
        if (productDAO.getStock() < quantity) {
            throw new ServiceException(
                    "Available stock: " + productDAO.getStock() + ", requested: " + quantity
            );
        }
        productDAO.setStock(productDAO.getStock() + quantity);
        return productRepository.save(productDAO);
    }

    @Override
    public ProductDAO update(ProductUpdateDTO productUpdateDTO) {
        ProductDAO pro = productRepository.findById(productUpdateDTO.id())
                .orElseThrow(() -> new ServiceException("Product not found"));
        modelMapper.map(productUpdateDTO, pro);
        return productRepository.save(pro);
    }

    @Transactional
    @Override
    public ResponseEntity<Void> batchDecreaseStock(StockUpdateBatchDTO batchDTO) {
        List<Long> productIds = batchDTO.getItems().stream()
                .map(StockUpdateBatchDTO.StockUpdateItem::getProductId)
                .toList();

        // Fetch all products at once
        List<ProductDAO> productDAOS = productRepository.findProductByIdIn(productIds);

        // Create a map for an easy lookup
        Map<Long, ProductDAO> productMap = productDAOS.stream()
                .collect(Collectors.toMap(ProductDAO::getId, product -> product));

        // Validate and update stock for all items
        List<String> errors = new ArrayList<>();

        for (StockUpdateBatchDTO.StockUpdateItem item : batchDTO.getItems()) {
            ProductDAO productDAO = productMap.get(item.getProductId());
            if (productDAO == null) {
                errors.add("Product not found for ID: " + item.getProductId());
                continue;
            }

            if (productDAO.getStock() < item.getQuantity()) {
                errors.add("Insufficient stock for product ID " + item.getProductId() +
                        ": available " + productDAO.getStock() + ", requested " + item.getQuantity());
                continue;
            }

            productDAO.setStock(productDAO.getStock() - item.getQuantity());
        }

        if (!errors.isEmpty()) {
            throw new ServiceException("Batch stock decrease failed: " + String.join("; ", errors));
        }
        productRepository.saveAll(productDAOS);
        return ResponseEntity.ok().build();
    }
}