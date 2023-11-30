package com.example.depreciationService.client;

import com.example.depreciationService.dto.response.AssetResponse;
import com.example.depreciationService.dto.response.AssetTypeResponse;
import com.example.depreciationService.dto.response.DepartmentResponse;
import com.example.depreciationService.dto.response.UserResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@NoArgsConstructor
public class DepreciationServiceClient {
    @Autowired
    private RestTemplate template;

    public AssetResponse fetchAsset(Long assetId) {
        return template.getForObject("http://ASSET-SERVICE/api/asset/v1/" + assetId, AssetResponse.class);
    }
    public UserResponse fetchUser(Long userId) {
        return template.getForObject("http://USER-SERVICE/api/user/v1/" + userId, UserResponse.class);
    }

    public Double getDepreciationValue(Long assetId, String fromDate, String toDate){
        return template.getForObject("http://ASSET-SERVICE/api/asset/depreciation/" + assetId +"?fromDate="+fromDate+"&toDate="+toDate, Double.class);
    }

    public DepartmentResponse fetchDepartment(Long deptId) {
        return template.getForObject("http://USER-SERVICE/api/user/department/v1/" + deptId, DepartmentResponse.class);
    }
    public AssetTypeResponse fetchAssetType(Long typeId) {
        return template.getForObject("http://ASSET-SERVICE/api/asset/type/" + typeId, AssetTypeResponse.class);
    }

}
