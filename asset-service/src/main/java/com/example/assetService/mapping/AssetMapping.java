package com.example.assetService.mapping;

import com.example.assetService.client.AssetServiceClient;
import com.example.assetService.dto.request.AssetRequest;
import com.example.assetService.dto.request.DeliveryRequest;
import com.example.assetService.dto.request.DepreciationRequest;
import com.example.assetService.dto.response.AssetDeliveryResponse;
import com.example.assetService.dto.response.AssetResponse;
import com.example.assetService.dto.response.AssetUpdateHistoryResponse;
import com.example.assetService.dto.response.UserResponse;
import com.example.assetService.model.*;
import com.example.assetService.repository.AccessaryRepository;
import com.example.assetService.repository.AssetTypeRepository;
import com.example.assetService.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AssetMapping {
    private final AssetServiceClient assetServiceClient;
    private final AssetTypeService assetTypeService;
    private final BrandService brandService;
    private final StorageService storageService;
    private final AccesaryService accesaryService;
    private final AssetDeliveryService assetDeliveryService;
    public AssetResponse getAssetResponse(Asset asset) {
        AssetResponse assetResponse = new AssetResponse();
        assetResponse.setAssetId(asset.getAssetId());
        assetResponse.setAssetName(asset.getAssetName());
        assetResponse.setAssetTypeId(asset.getAssetType());
        AssetType assetType = assetTypeService.findAssetTypeById(asset.getAssetType());
        assetResponse.setAssetTypeName(assetType.getAssetName());
        assetResponse.setAssetImage(asset.getAssetImage());
        assetResponse.setAmountOfYear(assetType.getAmountOfYear());
        assetResponse.setAssetGroupId(assetType.getAssetGroup().getId());
        assetResponse.setAssetGroup(assetType.getAssetGroup().getName());
        assetResponse.setPrice(asset.getPrice());
        assetResponse.setStatus(asset.getAssetStatus());

        switch (Math.toIntExact(asset.getAssetStatus())){
            case 0: assetResponse.setStatusName("Chưa sử dụng");break;
            case 1: assetResponse.setStatusName("Đang sử dụng");break;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assetResponse.setDateInStored(dateFormat.format(asset.getDateInStored()));
        if(asset.getDateUsed() != null)
            assetResponse.setDateUsed(dateFormat.format(asset.getDateUsed()));
        else
            assetResponse.setDateUsed("Chưa sử dụng");
        assetResponse.setUserIdUsed(asset.getUserUsedId());
        if(asset.getDateExperience()!=null)
            assetResponse.setExpDate(dateFormat.format(asset.getDateExperience()));
        else
            assetResponse.setExpDate("Chưa sử dụng");
        assetResponse.setDeptIdUsed(asset.getDeptUsedId());
        assetResponse.setSerial(asset.getSerialNumber());
        Brand brand = brandService.findById(asset.getBrandId());
        assetResponse.setAssetBrandId(asset.getBrandId());
        assetResponse.setAssetBrandName(brand.getBrandName());
        Storage storage = storageService.findById(asset.getStorageId());
        assetResponse.setAssetStorageId(asset.getStorageId());
        assetResponse.setAssetStorageName(storage.getStorageName());
        assetResponse.setAccessaries(accesaryService.findByAssetId(asset.getAssetId()));
        if(asset.getUserUsedId()!=null){
            assetResponse.setUser(assetServiceClient.fetchUser(Long.valueOf(asset.getUserUsedId())));
        }
        return assetResponse;
    }

    public Asset getAsset(AssetRequest assetRequest){
        Asset asset = new Asset();
        asset.setDateInStored(new Date());
        AssetType assetType = assetTypeService.findAssetTypeById(assetRequest.getAssetTypeId());
        asset.setTime(Long.valueOf(assetType.getAmountOfYear()));
        asset.setAssetName(assetRequest.getAssetName());
        asset.setAssetType(assetRequest.getAssetTypeId());
        asset.setAssetStatus(assetRequest.getStatus());
        asset.setPrice(assetRequest.getPrice());
        asset.setSerialNumber(assetRequest.getSerial());
        asset.setBrandId(assetRequest.getBrandId());
        asset.setStorageId(assetRequest.getStorageId());
        return asset;
    }
    //Thay đổi thông tin người sử dụng tài sản
    public Asset updateAsset(Asset asset, DeliveryRequest deliveryRequest){
        // Lấy thông tin người dùng
        UserResponse userResponse = assetServiceClient.fetchUser(Long.valueOf(deliveryRequest.getUserId()));
        if(userResponse == null)
            return null;
        asset.setAssetStatus(Long.valueOf(1));
        System.out.println(asset.getDateUsed());
        if(asset.getDateUsed()==null){
            LocalDate localDate = LocalDate.now();
            System.out.println(asset.getDateUsed());
            Date expDate = Date.from(localDate.plusMonths(asset.getTime()).atStartOfDay(ZoneId.systemDefault()).toInstant());
            asset.setDateExperience(expDate);
        }
        asset.setDateUsed(new Date());
        asset.setUserUsedId(deliveryRequest.getUserId());
        asset.setDeptUsedId(Long.valueOf(userResponse.getDept().getId()));
        DepreciationRequest depreciationRequest = new DepreciationRequest();
        depreciationRequest.setAssetId(asset.getAssetId());
        depreciationRequest.setDeptId(Long.valueOf(userResponse.getDept().getId()));
        depreciationRequest.setUserId(userResponse.getId());
        //Tạo thông tin khấu hao
        assetServiceClient.addDepreciation(depreciationRequest);
        //Tạo thông tin bàn giao
        AssetDelivery assetDelivery = new AssetDelivery();
        assetDelivery.setAssetId(asset.getAssetId());
        assetDelivery.setDeliveryType(0);
        assetDelivery.setUserCreateId(Long.valueOf(10));
        assetDelivery.setNote(deliveryRequest.getNote());
        assetDelivery.setStatus(asset.getAssetStatus());
        assetDelivery.setDeptId(Long.valueOf(userResponse.getDept().getId()));
        assetDelivery.setUserId(Long.valueOf(userResponse.getDept().getId()));
        assetDelivery.setCreateAt(new Date());
        assetDeliveryService.createDelivery(assetDelivery);
        return asset;
    }
    //Thu hồi tài sản
    public Asset recallAsset(Asset asset){
        asset.setAssetStatus(Long.valueOf(0));
        asset.setDateUsed(null);
        asset.setUserUsedId(null);
        asset.setDeptUsedId(null);
        //Nếu dừng khấu hao thành công thì trả về aset đã xóa người dùng
        if(assetServiceClient.recallAsset(asset.getAssetId()))
            return asset;
        return null;
    }
    public Double calculatorDepreciation(Asset asset, String fromDate, String toDate, Double value, String lastDate) throws ParseException {
        //Lấy thông tin tài sản và thời gian
        Date fDate = new SimpleDateFormat("yyyy-MM-dd").parse(fromDate);
        Date tDate = new SimpleDateFormat("yyyy-MM-dd").parse(toDate);
        Date lDate = new SimpleDateFormat("yyyy-MM-dd").parse(lastDate);
        int daysInMonth = LocalDate.from(fDate.toInstant().atZone(ZoneId.systemDefault())).lengthOfMonth();
        int amountMonth = (lDate.getDate() > daysInMonth/2 ? 0 : 1)
                + (11 - lDate.getMonth())
                + (asset.getDateExperience().getYear() - lDate.getYear() -1)*12
                + (asset.getDateExperience().getMonth())
                + (asset.getDateExperience().getDate() > daysInMonth/2 ? 1: 0);
        //Kiểm tra thông tin là tháng cuối hay chưa
        if(asset.getDateExperience().getMonth()==fDate.getMonth()&&asset.getDateExperience().getYear()==fDate.getYear())
            return depreciation3(asset.getPrice(),value,amountMonth);
        //Kiểm tra tài sản có nâng cấp hay không
        if(asset.getUpdateId()!=null){
            return depreciation2(asset.getPrice(),value,Long.valueOf(amountMonth),tDate.getDate()-fDate.getDate()+1,daysInMonth);
        }
        return depreciation1(asset.getPrice(), asset.getTime(),tDate.getDate()-fDate.getDate()+1,daysInMonth);
    }
    public Double calculatorDepreciationPerMonth(Asset asset, Double value, String lastDate) throws ParseException {
        //Lấy thông tin tài sản và thời gian
        Date lDate = new SimpleDateFormat("yyyy-MM-dd").parse(lastDate);
        //Tính số ngày trong tháng
        int daysInMonthLDate = LocalDate.from(lDate.toInstant().atZone(ZoneId.systemDefault())).lengthOfMonth();
        int daysInMonthEDate = LocalDate.from(asset.getDateExperience().toInstant().atZone(ZoneId.systemDefault())).lengthOfMonth();
        //Tính số tháng còn lại
        int amountMonth = (lDate.getDate() >= daysInMonthLDate/2 ? 0 : 1)
                + (11 - lDate.getMonth())
                + (asset.getDateExperience().getYear() - lDate.getYear() -1)*12
                + (asset.getDateExperience().getMonth())
                + (asset.getDateExperience().getDate() > daysInMonthEDate/2 ? 1: 0);
        //Kiểm tra tài sản có nâng cấp hay không
        if(asset.getUpdateId()!=null){
            return (asset.getPrice() - value)/amountMonth;
        }
        return asset.getPrice()/asset.getTime();
    }
    //Công thức tính khấu hao 1
    public Double depreciation1(Double price, Long amountMonth, int days, int amountDay) {
        return (price/amountMonth)*(Double.valueOf(days)/amountDay);
    }

    //Công thức tính khấu hao 2
    public Double depreciation2(Double price, Double valueUsed, Long amountMonth, int days, int amountDay){
        return ((price - valueUsed)/amountMonth)*(Double.valueOf(days)/amountDay);
    }
    //Công thức tính khấu hao 3
    public Double depreciation3(Double price, Double valueUsed,int amountMonth){
        return price - valueUsed - (amountMonth-1)*((price-valueUsed)/amountMonth);
    }

    public AssetDeliveryResponse getAssetDeliveryResponse(Asset asset){
        AssetDeliveryResponse assetDeliveryResponse = new AssetDeliveryResponse();
        assetDeliveryResponse.setStorageId(asset.getStorageId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Storage storage = storageService.findById(asset.getStorageId());
        assetDeliveryResponse.setStorageName(storage.getStorageName());
        assetDeliveryResponse.setStorageLocation(storage.getLocation());
        assetDeliveryResponse.setUserResponse(assetServiceClient.fetchUser(asset.getUserUsedId()));
        assetDeliveryResponse.setDateUsed(dateFormat.format(asset.getDateUsed()));
        assetDeliveryResponse.setDateInStored(dateFormat.format(asset.getDateInStored()));
        List<AssetDeliveryResponse.DeliveryHistory> listDelivery = new ArrayList<>();
        List<AssetDeliveryResponse.DeliveryHistory> listBroken = new ArrayList<>();
        //status == 0 == 1 => Lấy thông tin những cấp phát, thu hồi
        List<AssetDelivery> assetDeliveries = assetDeliveryService.findByAssetIdAndDeliveryType(asset.getAssetId());
        //status == 2 => Lấy lịch sử mất
        List<AssetDelivery> assetBroken = assetDeliveryService.findByAssetIdAndDeliveryType(asset.getAssetId(), 2);
        for(AssetDelivery assetDelivery : assetDeliveries){
            AssetDeliveryResponse.DeliveryHistory deliveryHistory = new AssetDeliveryResponse.DeliveryHistory();
            deliveryHistory.setDeliveryDate(dateFormat.format(assetDelivery.getCreateAt()));
            deliveryHistory.setNote(assetDelivery.getNote());
            UserResponse userResponse = assetServiceClient.fetchUser(assetDelivery.getUserId());
            deliveryHistory.setUserResponse(userResponse);
            UserResponse userCreate = assetServiceClient.fetchUser(assetDelivery.getUserCreateId());
            deliveryHistory.setUserCreateResponse(userCreate);
            deliveryHistory.setDeliveryType("Cấp phát");
            deliveryHistory.setStatus(assetDelivery.getStatus());
            deliveryHistory.setNote(assetDelivery.getNote());
            listDelivery.add(deliveryHistory);
        }
        for(AssetDelivery assetDelivery : assetBroken){
            AssetDeliveryResponse.DeliveryHistory brokenHistory = new AssetDeliveryResponse.DeliveryHistory();
            brokenHistory.setDeliveryDate(dateFormat.format(assetDelivery.getCreateAt()));
            brokenHistory.setNote(assetDelivery.getNote());
            UserResponse userResponse = assetServiceClient.fetchUser(assetDelivery.getUserId());
            brokenHistory.setUserResponse(userResponse);
            brokenHistory.setDeliveryType("Mất");
            brokenHistory.setStatus(assetDelivery.getStatus());
            brokenHistory.setNote(assetDelivery.getNote());
            listBroken.add(brokenHistory);
        }
        assetDeliveryResponse.setDeliveryHistories(listDelivery);
        assetDeliveryResponse.setBrokenHistories(listBroken);
        return assetDeliveryResponse;
    }

    public AssetUpdateHistoryResponse getAssetUpdateHistoryResponse(Asset asset,List<UpdateHistory> histories) throws ParseException {
        AssetUpdateHistoryResponse assetUpdateHistoryResponse = new AssetUpdateHistoryResponse();
        assetUpdateHistoryResponse.setAssetId(asset.getAssetId());
        assetUpdateHistoryResponse.setAssetName(asset.getAssetName());
        assetUpdateHistoryResponse.setTotalValueUpdate(0.0);
        assetUpdateHistoryResponse.setPricePre(asset.getPrice());
        assetUpdateHistoryResponse.setTimePre(asset.getTime());
        List<AssetUpdateHistoryResponse.UpdateHistoryResponse> list = new ArrayList<>();
        //Tạo 2 ngày mới nhất và cũ nhất
        Date dateOld = new Date();
        Date dateNew = asset.getDateUsed();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for(UpdateHistory updateHistory : histories){
            //Tính total Theo từng lịch sử
            assetUpdateHistoryResponse.setTotalValueUpdate(assetUpdateHistoryResponse.getTotalValueUpdate()+updateHistory.getValue());
            //Kiểm tra có phải ngày mới nhất chưa
            if(dateNew.before(updateHistory.getDateUpdate())) {
                dateNew = updateHistory.getDateUpdate();
                assetUpdateHistoryResponse.setNote(updateHistory.getNote());
            }
            //Kiểm tra có phải ngày cũ nhất chưa
            if(dateOld.after(updateHistory.getDateUpdate())){
                dateNew = updateHistory.getDateUpdate();
                assetUpdateHistoryResponse.setTimePrev(Long.valueOf(updateHistory.getAmountMonthPrev()));
                assetUpdateHistoryResponse.setPricePrev(updateHistory.getValuePrev());
            }
            UserResponse userUsed = assetServiceClient.fetchUser(updateHistory.getUserUsedId());
            UserResponse userUpdate = assetServiceClient.fetchUser(updateHistory.getUserUpdateId());
            AssetUpdateHistoryResponse.UpdateHistoryResponse response = new AssetUpdateHistoryResponse.UpdateHistoryResponse(
                    updateHistory.getId(),
                    userUpdate,
                    userUsed,
                    dateFormat.format(updateHistory.getCreateAt()),
                    dateFormat.format(updateHistory.getDateUpdate()),
                    updateHistory.getValue(),updateHistory.getValuePrev(),
                    updateHistory.getAmountMonthPresent()-updateHistory.getAmountMonthPrev(),
                    updateHistory.getNote(),
                    updateHistory.getStatus());
            list.add(response);
        }
        assetUpdateHistoryResponse.setDateUpdateNearest(dateFormat.format(dateNew));
        assetUpdateHistoryResponse.setUpdateHistoryResponses(list);
        return assetUpdateHistoryResponse;
    }
}
