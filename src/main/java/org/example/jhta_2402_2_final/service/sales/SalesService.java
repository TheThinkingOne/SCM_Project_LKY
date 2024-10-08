package org.example.jhta_2402_2_final.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.jhta_2402_2_final.dao.sales.SalesDao;
import org.example.jhta_2402_2_final.model.dto.sales.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.example.jhta_2402_2_final.controller.sales.SalesAdminController.alter;


@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesDao salesDao;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public int createKitOrder(KitOrderDto kitOrderDto) {
        UUID newKitOrderId = UUID.randomUUID();
        kitOrderDto.setKitOrderId(newKitOrderId);
        kitOrderDto.setStatusId(1);
        return salesDao.insert(kitOrderDto);
    }

    public List<KitOrderDetailDto> getAllKitOrderDetail() {
        return salesDao.findAllDetail();
    }

    //업체명이랑 pk가져오기
    public List<Map<String, String>> getKitCompanyIdAndNames() {

        return salesDao.getKitCompanyIdAndNames();
    }

    //밀키트 명이랑 pk가져오기
    public List<Map<String, Object>> getMealkitIdAndNames() {

        return salesDao.getMealkitIdAndNames();
    }

    // 상태 변경하기
    public int updateKitOrderStatus(UUID kitOrderId, int statusId) {
        int result = salesDao.updateKitOrderStatus(kitOrderId, statusId);
        int result02 = salesDao.insertKitOrderLog(kitOrderId);

        if (result > 0 && result02 > 0) return 1;
        else return 0;
    }

    //밀키트랑 재료 가져오기
    public List<KitSourceDetailDto> getAllKitSourceDetail() {
        return salesDao.findAllKitSourceDetail();
    }

    public List<Map<String, String>> getSourceIdAndNames(){
        return salesDao.getSourceIdAndNames();
    }

    //새로운 밀키트 등록, 그에맞는 재료도 같이 등록
    @Transactional
    public void insertMealkit(String mealkitName, List<String> sourceIds, List<Integer> quantities) {
        // 1. 밀키트를 삽입
        salesDao.insertMealkit(mealkitName);

        // 2. 삽입된 밀키트의 ID 조회
        String mealkitId = salesDao.getMealkitIdByName(mealkitName);

        int sourceIdIndex = 0;
        // 3. 밀키트와 선택된 재료를 매핑, 수량이 0 이상인 것만
        for (int i = 0; i < quantities.size(); i++) {
            if (quantities.get(i) != null) {
                salesDao.insertKitSources(mealkitId, sourceIds.get(sourceIdIndex), quantities.get(i));
                sourceIdIndex++;
            }
        }
    }

    //로그
    public List<KitOrderLogDto> getKitOrderLogs() {
        return salesDao.selectKitOrderLogs();
    }

    public int insertKitOrderLog(UUID kitOrderId){
        return salesDao.insertKitOrderLog(kitOrderId);
    }

    // 창고 테이블 가져오기
    public List<KitCompletedDto> getKitStorages() {
        return salesDao.selectKitStorage();
    }

    // 처리 완료된 애들만 가져오기
    public List<KitOrderLogDto> getAllCompleted(){
        return salesDao.findAllCompleted();
    }

    //창고 업데이트
    public void updateKitStorage(UUID kitOrderId) {
        // kitOrderId를 통해 KIT_ORDER 정보를 가져옴
        KitOrderDto kitOrder = salesDao.selectKitOrderById(kitOrderId);

        String kitCompanyId = kitOrder.getKitCompanyId();
        String mealkitId = kitOrder.getMealkitId();
        int quantity = kitOrder.getQuantity();

        //밀키트 아이디와 판매업체 아이디로 창고 재고를 확인
        KitStorageDto kitStorageDto = salesDao.selectKitStorageById(kitCompanyId,mealkitId);
        UUID kiStorageId = UUID.randomUUID();

        //해당 재고가 없으면 새로 추가
        if (kitStorageDto == null) {
            salesDao.insertKitStorage(kiStorageId, kitCompanyId, mealkitId, quantity);
        }

        //재고가 있으면 업데이트
        else {
            salesDao.updateKitStorage(kitStorageDto.getKitStorageId(), quantity);
        }
    }

    //생산 업체별 재료 가격
    public List<SourcePriceDto> getAllSourcePrice() {
        return salesDao.selectAllSourcePrice();
    }

    //최소값
    public List<SourcePriceDto> getMinSourcePrice() {
        return salesDao.selectMinSourcePrice();
    }

    //밀키트 별 최소 가격
    public List<KitPriceDto> getMinKitPrice(){
        return salesDao.selectMinKitPrice();
    }

    //밀키트 가격 변경
    public void updateKitPrice(String mealkitId, int minPrice) {
        salesDao.updateKitPrice(mealkitId, minPrice);
    }

    //밀키트 주문별 최저가 재료 포함한 상세정보
    public List<OrderDetailDto> getOrderDetails(UUID kitOrderId, int quantity) {

        return salesDao.selectOrderDetail(kitOrderId,quantity);
    }



    //product_order 테이블 상세 조회
    public List<ProductOrderDetailDto> selectProductOrder(){
        return salesDao.selectProductOrder();
    }

    public void processOrder(String[] sourceNames, String[] companyNames, int[] itemQuantities,int[] stackQuantities, int[] minPrices , UUID kitOrderId) {
        for (int i = 0; i < sourceNames.length; i++) {
            //창고 재료 보다 부족한 경우에만 발주 신청
            if (itemQuantities[i] > stackQuantities[i]){
                insertProductOrder(companyNames[i], sourceNames[i], itemQuantities[i]-stackQuantities[i], minPrices[i], kitOrderId);
            }

        }
    }

    //product order, product_order_log 생성
    public void insertProductOrder(String companyName, String sourceName, int itemQuantity, int minPrice, UUID kitOrderId) {
        UUID productOrderId = UUID.randomUUID();
        salesDao.insertProductOrder(productOrderId, companyName, sourceName, itemQuantity, minPrice, kitOrderId);
        salesDao.insertProductOrderLog(productOrderId);
    }



    //product_order_log 셀렉
    public List<ProductOrderLogDetailDto> selectProductOrderLog() {
        return salesDao.selectProductOrderLog();
    }

    public void updateKitOrderCancel(UUID kitOrderId) {

        salesDao.updateKitOrderCancel(kitOrderId);
        salesDao.updateProductOrderCancel(kitOrderId);
        salesDao.insertKitOrderLog(kitOrderId);

        List<UUID> productOrderIds = salesDao.selectProductOrderIdByKitOrderId(kitOrderId);
        for (UUID id : productOrderIds) {
            salesDao.insertProductOrderLog(id);
        }

    }


    //밀키트 별 창고 재고 확인
    public List<Map<String, String>> selectKitStorageTotalQuantity() {
        return salesDao.selectKitStorageTotalQuantity();
    }

    public List<Map<String, String>> selectKitTotalQuantity() {
        return salesDao.selectKitTotalQuantity();
    }

    //업체별 누적 판매량 , 판매금액
    public List<Map<String, String>> selectTotalQuantityByCompanyName() {
        return salesDao.selectTotalQuantityByCompanyName();
    }


    //업체별 월별 매출액
    public List<Map<String, Object>> getMonthlySales() {

        List<MonthlySalesDto> dtos = salesDao.getMonthlySales();

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, int[]> companyMap = new LinkedHashMap<>();

        int index = 0;
        int[] arr = new int[12];
        String prevCompany = null;  // 이전 회사명을 저장하기 위한 변수

        for (MonthlySalesDto dto : dtos) {
            String key = dto.getCompanyName();

            // 회사가 바뀌었을 때 배열을 맵에 저장하고 새로운 배열을 시작
            if (prevCompany != null && !prevCompany.equals(key)) {
                companyMap.put(prevCompany, arr.clone());  // 배열을 깊은 복사하여 저장
                arr = new int[12];  // 새 배열 초기화
                index = 0;  // 인덱스도 다시 0으로 초기화
            }

            arr[index] = dto.getTotalSales();
            index++;

            // 회사명 갱신
            prevCompany = key;
        }

        // 마지막 회사에 대해 배열을 저장
        if (prevCompany != null) {
            companyMap.put(prevCompany, arr.clone());  // 마지막으로 배열 저장
        }

        // 결과를 리스트에 담기
        for (Map.Entry<String, int[]> entry : companyMap.entrySet()) {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("companyName", entry.getKey());
            resultMap.put("monthlySales", entry.getValue());
            resultList.add(resultMap);
        }

        return resultList;
    }

    public void insertKitCompany(InsertKitCompanyDto insertKitCompanyDto) {
        UUID kitCompanyId = UUID.randomUUID();
        insertKitCompanyDto.setKitCompanyId(kitCompanyId);

        UUID userPk = UUID.randomUUID();
        insertKitCompanyDto.setUserPk(userPk);

        insertKitCompanyDto.setPassword(bCryptPasswordEncoder.encode(insertKitCompanyDto.getPassword()));

        System.out.println("insertKitCompanyDto = >>>>>" + insertKitCompanyDto);
        int result01 = salesDao.insertKitCompany(insertKitCompanyDto);
        if (result01 > 0) System.out.println("킷 컴퍼니 인서트 성공");
        else System.out.println("킷 컴퍼니 인서트 실패");

        int result02 = salesDao.insertUser(insertKitCompanyDto);
        if (result02>0) System.out.println("유저 인서트 성공");
        else System.out.println("유저 인서트 실패");


        int result03 = salesDao.insertKitCompanyMember(insertKitCompanyDto);
        if (result03 > 0) System.out.println("반정규화 인서트 성공");
        else System.out.println("반정규화 인서트 실패 ");
    }

    public String getKitOrderStatus(UUID kitOrderId) {
        return salesDao.getKitOrderStatus(kitOrderId);
    }

    public KitPriceDto getCurrentPriceAndMinPrice (String mealkitId) {
        return salesDao.getCurrentPriceAndMinPrice(mealkitId);
    }

    //유저아이디 중복 검사
    public boolean checkUserIdExists(String userId) {
        return salesDao.checkUserIdExists(userId);
    }

    //업체명 중복검사
    public boolean checkCompanyNameExists(String companyName) {
        return salesDao.checkCompanyNameExists(companyName);
    }

    //이메일 중복검사
    public boolean checkEmailExists(String email) {
        return salesDao.checkEmailExists(email);
    }

    public boolean checkTelExists(String tel) {
        return salesDao.checkTelExists(tel);
    }

    public boolean checkAddressExists(String address) {
        return salesDao.checkAddressExists(address);
    }

    public int getTotalMonthSale(int currentYear, int currentMonth) {
        return salesDao.getTotalMonthSale(currentYear, currentMonth);
    }

    public int getTotalYearSale(int currentYear) {
        return salesDao.getTotalYearSale(currentYear);
    }

    public int getProcessCount() {
        return salesDao.getProcessingCount();
    }

    public int getCompleteCount() {
        return salesDao.getCompleteCount();
    }
}

