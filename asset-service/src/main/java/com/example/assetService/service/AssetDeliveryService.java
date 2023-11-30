package com.example.assetService.service;

import com.example.assetService.model.AssetDelivery;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Component
public interface AssetDeliveryService {
    List<AssetDelivery> findByAssetIdAndStatus(Long assetId, int status);
    List<AssetDelivery> findByAssetIdAndDeliveryType(Long assetId, int deliveryType);
}
