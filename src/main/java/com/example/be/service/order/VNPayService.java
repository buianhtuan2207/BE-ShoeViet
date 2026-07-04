package com.example.be.service.order;

import com.example.be.util.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnpayConfig;

    public String createPaymentUrl(long amount, String orderCode, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderType = "other";
        long vnp_Amount = amount * 100;

        // Bắt buộc dùng orderCode làm mã tham chiếu để sau này mapping được đơn hàng
        String vnp_TxnRef = orderCode;
        String vnp_IpAddr = vnpayConfig.getIpAddress(request);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh_toan_don_hang_" + orderCode);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        try {
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    String encodedKey = encodeVnpay(fieldName);
                    String encodedValue = encodeVnpay(fieldValue);
                    hashData.append(encodedKey);
                    hashData.append('=');
                    hashData.append(encodedValue);
                    query.append(encodedKey);
                    query.append('=');
                    query.append(encodedValue);
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = vnpayConfig.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
            return vnpayConfig.getApiUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi sinh link VNPay", e);
        }
    }

    // Dùng để xác thực dữ liệu do VNPAY trả về
    public int orderReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // Tính toán lại Hash để so sánh với Hash của VNPAY gửi về
        String signValue = vnpayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1; // Thanh toán thành công
            } else {
                return 0; // Thanh toán thất bại / Khách hủy
            }
        } else {
            return -1; // Chữ ký không hợp lệ
        }
    }

    private String encodeVnpay(String value) throws Exception {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
    }
}