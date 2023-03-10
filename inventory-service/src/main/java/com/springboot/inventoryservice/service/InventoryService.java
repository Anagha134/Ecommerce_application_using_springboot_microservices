package com.springboot.inventoryservice.service;

import com.springboot.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor

public class InventoryService {

    private final InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    public boolean isInstock(String skuCode){
        return inventoryRepository.findBySkuCode().isPresent();

    }

}
