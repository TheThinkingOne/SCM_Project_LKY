# Scm Project
* 판매업체, 유통업체, 생산업체 간의 식재료 주문 발주/출고를 할 수 있는 SCM API 입니다.
* 사용자가 별도로 필요 재료의 수량이나 종류를 따질 필요 없이 맞춤형으로 발주/출고를 할 수 있습니다.
* 개발기간 : 24.08.20 ~ 24.09.25
* 참여 인원 : 6명

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


