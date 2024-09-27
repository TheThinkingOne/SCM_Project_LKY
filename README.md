# Scm Project
* 판매업체, 유통업체, 생산업체 간의 식재료 주문 발주/출고를 할 수 있는 SCM API 입니다.
* 개발기간 : 24.08.20 ~ 24.09.25
* 참여 인원 : 6명
# 프로젝트 기술 목표
* 사용자가 별도로 필요 재료의 수량이나 종류를 따질 필요 없이 맞춤형으로 발주/출고를 할 수 있다
* 사용자가 알아보기 쉬운 시각화된 그래프 자료를 볼 수 있다.
* 주문로그를 쉽게 추적하고 취소/변경에 어려움이 없어야 한다
* 각 업체의 업무끼리 결합도가 높아선 안되고 구분이 확실해야 한다.
## 기술 Stack
* Language : <img src="https://img.shields.io/badge/Java-blue?style=for-the-badge&logo=jameson&logoColor=004027">
* Framework : <img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"><img src="https://img.shields.io/badge/Javascript-F7DF1E?style=for-the-badge&logo=Javascript&logoColor=white"> <img src="https://img.shields.io/badge/jquery-0769AD?style=for-the-badge&logo=jquery&logoColor=white">
* DB : <img src="https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white"> <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
* CI/CD : <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
* Server : <img src="https://img.shields.io/badge/amazon web services-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=white"> 
* Communication : <img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white">
<img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white"> <img src="https://img.shields.io/badge/google drive-4285F4?style=for-the-badge&logo=googledrive&logoColor=white">
# ERD
![ERD 설명포함 화면](https://github.com/user-attachments/assets/6472a7e3-73be-4c43-b8b0-ac360c5bafc3)
# 담당 기능 구현
* Logistic(유통) 메인 페이지 구현
* Web Socket을 적용한 실시간 유통창고 재고 조회 그래프
* 주문로그의 상태에 따른 주문 정보 테이블 구현
* 물품 출고시에 밀키트 재료별로 수량이 충분한 경우 출고되도록 구현, 트랜잭션 처리
* 물품 발주시에 부족한 재료를 선별해서 식재료 생산업체에 발주 요청 구현, 트랜잭션 처리
* 발주/출고를 할 때마다 주문로그를 생성하거나 상태를 변경하여 다른 업체에서 이 데이터를 사용할 수 있음
# 핵심 로직 코드
<details>
  <summary>FIFO 방식의 재고 출고(차감) 로직</summary>
```java
@Transactional
    public boolean processKitOrder(String kitOrderId) {

        // 처리 전 상태(1) 이 아니면 로직 안 수행되게 false 로 리턴
        int currentStatus = kitOrderProcessDao.findKitOrderStatus(kitOrderId);
        if (currentStatus != 1) {
            return false;
        }

        // 1. 주문에 해당하는 재료 목록과 필요 수량 가져옴
        Integer orderQuantity = findOrderQuantityByKitOrderId(kitOrderId);

        // 2. 밀키트 주문에 해당하는 밀키트 ID 가져오기
        String mealKitId = findMealKitByKitOrderId(kitOrderId);

        // 3. 주문에 해당하는 재료 목록과 필요 수량을 가져옴 (각 재료별)
        List<Map<String, Object>> ingredients = kitOrderProcessDao.findKitRecipeWithStock(mealKitId, orderQuantity);

        // 4. 모든 재료의 재고가 충분한지 먼저 확인 (검증 단계)
        for (Map<String, Object> ingredient : ingredients) {
            String sourceId = (String) ingredient.get("재료번호");
            int required = ((Number) ingredient.get("필요수량")).intValue(); // Number로 받고 int로 변환
            int totalStockOfSameSource = ((Number) ingredient.get("창고재고수량")).intValue(); // 동일하게 변환

            // 하나라도 재고가 부족하면 바로 실패 처리 (출고 로직 처음부터 실행하지 않음)
            if (totalStockOfSameSource < required) {
                return false; // 재고 부족으로 출고 실패
            }
        }

        // 5. 모든 재료의 재고가 충분한 경우에만 출고 로직을 실행
        for (Map<String, Object> ingredient : ingredients) {
            String sourceId = (String) ingredient.get("재료번호");
            int required = ((Number) ingredient.get("필요수량")).intValue();

            // 재고 리스트를 오래된 순서로 가져옴
            List<Map<String, Object>> warehouseStacks = kitOrderProcessDao.findWarehouseStacks(sourceId);

            // FIFO 방식으로 재고 차감
            for (Map<String, Object> stack : warehouseStacks) {
                if (required <= 0) {
                    break; // 필요 수량을 모두 처리했으면 중단
                }

                String stackId = (String) stack.get("유통창고ID");
                int stock = (int) stack.get("재고수량");

                if (stock > 0) {
                    if (stock >= required) {
                        kitOrderProcessDao.updateWarehouseStockWithStackId(stackId, required); // 재고 차감
                        required = 0; // 필요 수량 충족
                        break; // 현재 재료는 처리 완료
                    } else {
                        kitOrderProcessDao.updateWarehouseStockWithStackId(stackId, stock);
                        required -= stock; // 남은 필요 수량 계산
                    }
                }
            }
        }

        // 6. 모든 재료가 성공적으로 출고되었으면 주문 상태를 '처리 완료'로 업데이트
        kitOrderProcessDao.updateKitOrderStatus(kitOrderId, 6); // 상태 6으로 변경
        kitOrderProcessDao.insertKitOrderLog(kitOrderId, 6); // 로그 기록 추가

        messagingTemplate.convertAndSend("/topic/warehouse/update", "update");

        return true; // 성공적으로 처리 완료
    }
```</details>


