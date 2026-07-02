package com.example.be.service.order;

import com.example.be.dto.req.order.GhnFeeRequest;
import com.example.be.dto.req.order.ShippingFeeRequest;
import com.example.be.dto.res.order.GhnFeeResponse;
import com.example.be.dto.res.order.ShippingFeeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ShippingService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.shop.id}")
    private String ghnShopId;

    @Value("${ghn.api.url.fee}")
    private String urlFee;

    @Value("${ghn.api.url.province}")
    private String urlProvince;

    @Value("${ghn.api.url.district}")
    private String urlDistrict;

    @Value("${ghn.api.url.ward}")
    private String urlWard;

    // Helper tạo Header dùng chung cho các request GHN
    private HttpHeaders createGhnHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // 1. Lấy danh sách Tỉnh/Thành
    public Object getProvinces() {
        HttpEntity<String> entity = new HttpEntity<>(createGhnHeaders());
        ResponseEntity<Object> response = restTemplate.exchange(urlProvince, HttpMethod.GET, entity, Object.class);
        return response.getBody();
    }

    // 2. Lấy danh sách Quận/Huyện theo Tỉnh
    public Object getDistricts(Integer provinceId) {
        HttpEntity<String> entity = new HttpEntity<>(createGhnHeaders());
        String url = urlDistrict + "?province_id=" + provinceId;
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
        return response.getBody();
    }

    // 3. Lấy danh sách Phường/Xã theo Quận/Huyện
    public Object getWards(Integer districtId) {
        HttpEntity<String> entity = new HttpEntity<>(createGhnHeaders());
        String url = urlWard + "?district_id=" + districtId;
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
        return response.getBody();
    }

    // 4. Tính phí vận chuyển (Đã viết trước đó, thêm header ShopId)
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest req) {
        HttpHeaders headers = createGhnHeaders();
        headers.set("ShopId", ghnShopId);

        GhnFeeRequest ghnRequest = GhnFeeRequest.builder()
                .serviceTypeId(2)
                .toDistrictId(req.getToDistrictId())
                .toWardCode(req.getToWardCode())
                .weight(req.getWeightInGrams() != null ? req.getWeightInGrams() : 200)
                .length(15).width(15).height(15)
                .insuranceValue(req.getOrderTotalValue())
                .build();

        HttpEntity<GhnFeeRequest> entity = new HttpEntity<>(ghnRequest, headers);

        try {
            ResponseEntity<GhnFeeResponse> response = restTemplate.exchange(urlFee, HttpMethod.POST, entity, GhnFeeResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getCode() == 200) {
                Double finalFee = response.getBody().getData().getTotal();

                if (req.getOrderTotalValue() != null && req.getOrderTotalValue() >= 2000000) {
                    finalFee = 0.0;
                }
                return new ShippingFeeResponse(finalFee, "3-5 ngày");
            } else {
                throw new RuntimeException("Lỗi GHN API");
            }
        } catch (Exception e) {
            return new ShippingFeeResponse(35000.0, "3-5 ngày (Dự kiến)");
        }
    }
}