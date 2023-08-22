package com.programmingtechie.inventoryservice.service;

import com.programmingtechie.inventoryservice.dto.InventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.programmingtechie.inventoryservice.repository.InventoryRepository;

import java.util.List;

@Service
public class InventoryService {

	@Autowired
	private InventoryRepository inventoryRepository;

	/**
	 * Here findBySkuCodeIn() will return a list if Inventory. Hence, using streams here.
	 * Querying the repository to find all the inventory objects for the skuCode in the list.
	 * Then Mapping the List of Inventory object that we get from findBySkuCodeIn() method to InventoryResponse Object.
	 * Then finally, returning the List of InventoryResponse Objects as the response to the controller.
	 */
	@Transactional(readOnly = true)
	public List<InventoryResponse> isInStock(List<String> skuCodeList) {
		return inventoryRepository.findBySkuCodeIn(skuCodeList).stream()
				.map(inventory ->
						InventoryResponse.builder()
							.skuCode(inventory.getSkuCode())
							.isInStock(inventory.getQuantity() > 0)
							.build()
				).toList();
	}
}
