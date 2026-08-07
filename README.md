# SKALA-SHOP API

온라인 쇼핑몰 백엔드 REST API 실습 프로젝트입니다.

## 환경
- Java 21
- Spring Boot 4.1.0
- Gradle
- Spring Web / Spring Data JPA / H2
- Lombok
- JWT (JJWT 0.11.5)
- Swagger/OpenAPI (springdoc 3.0.3)
- AOP / Actuator

## 실행
Spring Initializr로 Gradle Wrapper가 생성되어 있다면:

```bash
./gradlew bootRun
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

H2 Console:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:skalashop
User: sa
Password: (빈 값)
```

## 핵심 시나리오
1. POST /api/customers - 회원가입 (초기 포인트 1,000,000)
2. POST /api/customers/login - 로그인 및 JWT Cookie 발급
3. GET /api/products - 상품 조회
4. POST /api/customers/order - 주문 및 포인트 차감
5. GET /api/customers/{customerId} - 주문 상품 조회
6. POST /api/customers/cancel - 주문 취소 및 포인트 환급

## 참고
강의자료의 API 표와 Controller 예시 사이에 일부 URI 표기 차이가 있어,
목록 조회는 `/api/products`와 `/api/products/list`, `/api/customers`와 `/api/customers/list`를 모두 지원합니다.
고객 삭제도 DELETE body 방식과 DELETE `/api/customers/{customerId}` 방식을 모두 지원합니다.
