# 재고 예약 API의 동시성·멱등성·만료 처리

> 과제 유형: Backend 구현 + 동시성 검증 + 데이터 일관성
>
>
> 난이도: Level 2 — Concurrency & Data
>
> 상태: 준비
>

---

## 배경

땡땡 커머스는 한정 수량 상품을 판매한다. 사용자가 결제 화면으로 이동하는 동안 상품을 잠시 확보하기 위해 **재고 예약** 기능을 도입한다.

예약은 일정 시간 동안 재고를 점유한다. 사용자가 예약을 확정하면 판매된 재고가 되고, 취소하거나 예약 시간이 지나면 다시 구매 가능한 재고로 돌아온다.

이 API는 여러 애플리케이션 인스턴스에서 동시에 호출될 수 있다. 따라서 단일 요청이 정상 동작하는 것만으로는 완료로 간주하지 않는다. 동시 요청, 같은 요청의 재전송, 확정과 취소의 경합에서 데이터 상태가 어떻게
유지되는지 직접 검증해야 한다.

근본 원인이나 특정 동시성 제어 방법은 과제에서 미리 제공하지 않는다. 요구사항과 관측 결과를 바탕으로 필요한 보장 수준과 구현 방법을 판단한다.

이 과제는 `Concurrent Consistency Track`의 첫 단계다. 이 단계에서는 정합성 기준선을 구현하고 동시 요청에서 실제 동작을 관측하는 데 집중한다. Optimistic Locking,
Pessimistic Locking, Redis 등 전략의 최종 비교는 후속 비교 과제에서 동일 workload로 수행한다.

---

## 목표

다음 동작을 제공하는 재고 예약 API를 구현한다.

1. 상품의 재고를 조회한다.
2. 상품 재고를 일정 수량만큼 예약한다.
3. 예약을 확정하거나 취소한다.
4. 만료된 예약을 자동으로 종료하고 재고를 복구한다.
5. 같은 예약 요청이 재전송되어도 예약이 중복 생성되지 않게 한다.
6. 동시 요청에서도 재고가 음수가 되거나 실제 재고보다 많이 예약되지 않게 한다.

---

## 도메인 규칙

### 상품 재고

상품은 다음 재고 값을 가진다.

| 필드 | 의미 |
| --- | --- |
| `totalStock` | 상품에 등록된 전체 재고 |
| `availableStock` | 현재 새 예약에 사용할 수 있는 재고 |
| `soldStock` | 확정된 예약으로 판매된 재고 |
| `reservedStock` | 현재 `RESERVED` 상태인 예약 수량의 합 |

다음 조건은 항상 만족해야 한다.

```
totalStock >= 0
availableStock >= 0
soldStock >= 0
reservedStock >= 0
availableStock + soldStock + reservedStock = totalStock
```

`reservedStock`을 저장하지 않고 계산하는 것은 허용하지만, API 응답에는 현재 값을 표시해야 한다.

### 예약 상태

예약 상태는 다음 네 가지를 사용한다.

```
RESERVED   예약이 재고를 점유하고 있음
CONFIRMED  예약이 확정되어 판매 처리됨
CANCELED   사용자가 예약을 취소함
EXPIRED    예약 시간이 지나 자동 종료됨
```

허용되는 상태 전이는 다음과 같다.

```
RESERVED -> CONFIRMED
RESERVED -> CANCELED
RESERVED -> EXPIRED
```

`CONFIRMED`, `CANCELED`, `EXPIRED`는 terminal state다. 확정된 예약을 다시 취소하거나, 취소된 예약을 다시 확정하는 등 허용되지 않은 전이는 `409`로 응답해야 한다.

### 예약 시간

- 예약 유효 시간은 생성 시점부터 120초다.
- `expiresAt`과 현재 시각이 같거나 현재 시각이 지난 `RESERVED` 예약은 유효하지 않다.
- 만료된 예약은 더 이상 `reservedStock`에 포함되지 않고 `availableStock`으로 복구되어야 한다.
- 만료 처리는 백그라운드 작업으로 수행한다. 애플리케이션이 실행 중인 동안 만료 대상이 5초 이상 `RESERVED` 상태로 남아 있지 않도록 한다.
- 시간대가 다른 환경에서도 비교할 수 있도록 API의 시각은 UTC 기준 ISO-8601 형식의 `Instant`를 사용한다.

테스트가 실제 시간을 기다리지 않고 만료 동작을 검증할 수 있어야 한다. 시간을 추상화하는 구체적인 방법은 자유롭게 선택한다.

### 요청 멱등성

예약 생성 요청에는 클라이언트가 생성한 `requestId`가 포함된다.

- 같은 `productId`와 같은 `requestId`로 같은 요청이 다시 들어오면 기존 예약을 반환한다.
- 위 상황에서 예약을 새로 만들거나 재고를 다시 차감하면 안 된다.
- 같은 `requestId`를 사용했지만 `customerId` 또는 `quantity`가 다르면 요청을 거부한다.
- 기존 예약이 `CONFIRMED`, `CANCELED`, `EXPIRED` 상태가 된 뒤에도 같은 `requestId`를 다시 사용하면 새 예약을 만들지 않고 기존 예약을 반환한다. 새로운 예약은 새로운
  `requestId`를 사용해야 한다.
- `requestId`는 상품별로 유일하게 관리한다. 서로 다른 상품에서는 같은 문자열을 사용할 수 있다.

---

## 고정 API 계약

아래 URL, HTTP method, 요청·응답 필드명, 상태코드, 에러 코드의 의미는 변경하지 않는다. 응답에 필드를 추가할 수는 있지만 기존 필드를 삭제하거나 이름을 바꾸면 안 된다.

### 1. 재고 조회

```
GET /api/v1/products/{productId}/stock
```

성공 응답: `200 OK`

```json
{
  "productId": "limited-keyboard",
  "totalStock": 10,
  "availableStock": 7,
  "reservedStock": 2,
  "soldStock": 1
}
```

상품이 없으면 `404 NOT_FOUND`를 반환한다.

### 2. 예약 생성

```
POST /api/v1/products/{productId}/reservations
Content-Type: application/json
```

요청 본문:

```json
{
  "customerId": "customer-001",
  "quantity": 2,
  "requestId": "reserve-request-001"
}
```

최초 생성 성공 응답: `201 CREATED`

```json
{
  "reservationId": "res-uuid",
  "productId": "limited-keyboard",
  "customerId": "customer-001",
  "quantity": 2,
  "status": "RESERVED",
  "createdAt": "2026-08-27T05:00:00Z",
  "expiresAt": "2026-08-27T05:02:00Z"
}
```

같은 `productId`와 `requestId`로 동일 요청을 재전송한 경우:

- `200 OK`
- 최초 응답과 같은 `reservationId`, `expiresAt`, `quantity`를 반환
- 재고를 추가로 차감하지 않음

재고가 부족하면:

- `409 CONFLICT`
- 에러 코드: `INSUFFICIENT_STOCK`

```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "예약 가능한 재고가 부족합니다."
}
```

존재하지 않는 상품이면 `404 NOT_FOUND`를 반환한다.

같은 `requestId`에 요청 내용이 다르면:

- `409 CONFLICT`
- 에러 코드: `DUPLICATE_REQUEST_MISMATCH`

필수 값 누락, `quantity <= 0`, 공백인 식별자 등 입력 오류는 `400 BAD_REQUEST`
와 에러 코드 `INVALID_REQUEST`를 반환한다.

### 3. 예약 조회

```
GET /api/v1/reservations/{reservationId}
```

성공 시 `200 OK`와 예약 생성 응답과 같은 필드를 반환한다. 예약이 없으면
`404 NOT_FOUND`를 반환한다.

조회 시점에 이미 만료된 예약은 `RESERVED`로 반환하면 안 된다.

### 4. 예약 확정

```
POST /api/v1/reservations/{reservationId}/confirm
Content-Type: application/json
```

요청 본문은 없다.

확정 성공 시 `200 OK`와 상태가 `CONFIRMED`인 예약을 반환한다. 이때 해당 수량은 `soldStock`에 포함되어야 한다.

확정 요청 시점에 `expiresAt`에 도달했거나 지난 예약은 먼저 `EXPIRED`로 처리되어야 하며, 확정 요청에는 `409 CONFLICT`와
`INVALID_RESERVATION_STATE`를 반환한다.

이미 `CONFIRMED`인 예약에 동일한 확정 요청을 반복하면 `200 OK`와 같은 결과를 반환한다. `CANCELED` 또는 `EXPIRED` 예약을 확정하려 하면
`409 CONFLICT`, 에러 코드 `INVALID_RESERVATION_STATE`를 반환한다.

### 5. 예약 취소

```
POST /api/v1/reservations/{reservationId}/cancel
Content-Type: application/json
```

요청 본문은 없다.

취소 성공 시 `200 OK`와 상태가 `CANCELED`인 예약을 반환한다. 해당 수량은
`availableStock`으로 복구되어야 한다.

취소 요청 시점에 `expiresAt`에 도달했거나 지난 예약은 먼저 `EXPIRED`로 처리되어야 하며, 취소 요청에는 `409 CONFLICT`와
`INVALID_RESERVATION_STATE`를 반환한다.

이미 `CANCELED`인 예약에 동일한 취소 요청을 반복하면 `200 OK`와 같은 결과를 반환한다. `CONFIRMED` 또는 `EXPIRED` 예약을 취소하려 하면
`409 CONFLICT`, 에러 코드 `INVALID_RESERVATION_STATE`를 반환한다.

---

## 초기 데이터

애플리케이션 시작 시 다음 상품을 준비한다. 초기화 방식은 자유롭지만, 매번 같은 결과가 되도록 한다.

| `productId` | `totalStock` | `availableStock` | `soldStock` |
| --- | --- | --- | --- |
| `limited-keyboard` | 10 | 10 | 0 |
| `limited-monitor` | 3 | 3 | 0 |

초기화 과정에서 기존 상품의 재고를 임의로 덮어쓰거나 테스트 실행 사이의 데이터를 공유하지 않도록 테스트 격리를 고려한다.

---

## 구현 범위

다음 범위를 구현한다.

- REST Controller 및 요청 검증
- 예약 생성·조회·확정·취소의 애플리케이션 로직
- 상품과 예약의 영속화
- 예약 만료 처리와 재고 복구
- 중복 `requestId` 처리
- 동시 요청에서도 도메인 규칙을 보장하는 트랜잭션 경계
- 정상·경계·실패·동시성 시나리오를 검증하는 테스트

클래스, 패키지, 테이블, 쿼리, 동시성 제어 방식은 자유롭게 정한다. 다만 선택한 방법이 어떤 invariant와 실패 상황을 보장하는지 설명할 수 있어야 한다.

## 학습 계약

### 핵심 질문

동일한 상품 재고 또는 동일한 예약에 동시 요청이 들어올 때, 재고 수치와 예약 상태의 정합성을 어떻게 보장할 것인가?

### 기준선과 관측

- 최소한 정상 요청과 동시 요청을 실행할 수 있는 기준 구현을 만든다.
- 동시 요청 수, 성공·실패 수, 최종 재고 수치, 중복 예약 수를 기록한다.
- 이 과제에서는 특정 해결책을 정답으로 가정하지 않는다.

### 전이 질문

동시 요청 수가 증가하거나 특정 상품에 요청이 집중되고, 애플리케이션 인스턴스가 여러 개로 늘어나면 현재 판단은 여전히 유효한가?

---

## 기술 및 실행 환경

| 항목 | 사양 |
| --- | --- |
| 언어 | Java |
| 프레임워크 | Spring Boot 4.x |
| 런타임 | Java 25+ |
| 데이터베이스 | H2 인메모리, MySQL 호환 모드 |
| 빌드 | Gradle Groovy |
| 테스트 | JUnit 6 |
| 기본 포트 | 8080 |
| 실행 방식 | 단일 애플리케이션 인스턴스 |

Docker, Redis, Kafka, 외부 Lock 서버, 별도 메시지 브로커는 추가하지 않는다. 현재 과제의 규모에서는 애플리케이션과 H2만으로 요구사항을 검증한다.

---

## 검증 시나리오

아래 결과를 테스트 또는 재현 가능한 실행 명령으로 증명한다.

### 시나리오 1: 정상 예약

- `limited-keyboard`의 수량 2를 예약한다.
- `201 CREATED`와 `RESERVED` 상태를 확인한다.
- 조회 결과 `availableStock = 8`, `reservedStock = 2`, `soldStock = 0`인지 확인한다.

### 시나리오 2: 재고 부족

- `limited-monitor`에 수량 4를 예약한다.
- `409 CONFLICT`와 `INSUFFICIENT_STOCK`를 확인한다.
- 재고가 변하지 않는지 확인한다.

### 시나리오 3: 같은 요청의 중복 전송

- 같은 `productId`, `customerId`, `quantity`, `requestId`로 예약 요청을 여러 번 보낸다.
- 최초 요청 외에는 `200 OK`를 받고, 모든 성공 응답의
  `reservationId`가 같은지 확인한다.
- 예약 레코드가 1개이고 재고가 한 번만 차감되는지 확인한다.

### 시나리오 4: 같은 요청의 동시 전송

- 재고가 1개인 상품을 준비하거나 테스트 픽스처로 만든다.
- 동일한 요청을 최소 50개 스레드에서 동시에 전송한다.
- 예약은 1개만 생성되어야 한다.
- 성공 응답은 동일한 예약을 가리켜야 하며, 재고는 1개만 차감되어야 한다.
- `requestId` 중복 때문에 `500`이 발생하면 안 된다.

### 시나리오 5: 서로 다른 요청의 재고 경쟁

- 재고 10개인 상품에 수량 1의 서로 다른 예약 요청 100개를 동시에 보낸다.
- 성공 예약 수량의 합은 10을 초과하면 안 된다.
- 최종 `availableStock`은 음수가 아니어야 한다.
- 재고 부족 요청은 계약에 맞는 `409`를 받아야 한다.
- 예약 레코드와 재고 수치가 서로 모순되지 않아야 한다.

### 시나리오 6: 확정과 취소

- 예약 1건을 생성한 뒤 확정한다.
- 확정 후 `soldStock`은 예약 수량만큼 증가하고 `availableStock`은 다시 증가하지 않는지 확인한다.
- 이미 확정된 예약에 확정을 반복해도 판매 수량이 중복 증가하지 않는지 확인한다.
- 별도 예약을 생성한 뒤 취소하고, 취소 후 `availableStock`이 정확히 복구되는지 확인한다.
- 이미 취소된 예약에 취소를 반복해도 재고가 중복 복구되지 않는지 확인한다.

### 시나리오 7: 만료

- 예약을 생성한다.
- 테스트에서 시간을 제어하여 `expiresAt` 이후로 이동하거나, 과제에서 제공하는 만료 실행 방법을 사용한다.
- 예약 상태가 `EXPIRED`가 되고, `availableStock`이 예약 수량만큼 복구되는지 확인한다.
- 만료된 예약을 확정하거나 취소할 수 없어야 한다.
- 만료 처리를 여러 번 실행해도 재고가 중복 복구되지 않아야 한다.

### 시나리오 8: 확정과 취소의 동시 실행

- 하나의 `RESERVED` 예약에 대해 확정과 취소 요청을 동시에 실행한다.
- 최종 상태가 `CONFIRMED`, `CANCELED` 중 하나로만 결정되는지 확인한다.
- 두 상태의 효과가 동시에 적용되지 않아야 한다.
- 다음 수치가 항상 성립해야 한다.

```
availableStock + reservedStock + soldStock = totalStock
```

---

## 수용 조건

- `./gradlew test`가 통과한다.
- 고정 API 계약의 URL, method, 필드명, 상태코드, 에러 코드 의미를 지킨다.
- 수량이 음수가 되거나 전체 재고를 초과하는 예약이 생기지 않는다.
- 중복 요청과 중복 확정·취소·만료 처리로 재고가 중복 반영되지 않는다.
- 동시성 검증이 우연히 통과한 것이 아님을 설명할 수 있도록 반복 실행 방법 또는 테스트 근거를 남긴다.
- 테스트가 실제 운영 코드와 다른 가짜 저장소만 검증하지 않도록 한다.
- 변경한 설계에서 어떤 상태 전이와 데이터 invariant를 보장하는지
  `진행상황.md`에 기록한다.

---

## 제출물

과제 디렉터리 아래에 다음을 남긴다.

```
과제.md
진행상황.md
project/
    실제 구현 프로젝트
```

관측된 동시성 오류, 데이터 불일치, 테스트 불안정성 또는 성능 문제가 있을 때만 `트러블슈팅.md`를 추가한다. 아직 실행하지 않은 추측을 관측 결과처럼 기록하지 않는다.

중요한 설계 결정을 내렸다면 `결정기록.md`에 다음을 간단히 남긴다.

```
Decision:
Context:
Why:
Alternatives Considered:
Trade-off:
What would make us reconsider this decision?
```

---

## 변경 금지 및 유의사항

- Docker, Redis, Kafka, 외부 분산 락 서비스 등 별도 인프라를 추가하지 않는다.
- API 계약에 없는 성공 우회 응답을 만들지 않는다.
- 재고 문제를 해결하기 위해 전역 `synchronized` 하나에 의존하는 구현은 애플리케이션 인스턴스가 늘어났을 때의 한계를 설명해야 한다.
- 동시성 테스트를 `Thread.sleep`만으로 통과시키지 않는다.
- 테스트를 통과시키기 위해 수용 조건이나 고정 계약을 완화하지 않는다.
- 문제를 발견했을 때는 현상, 영향, 가설, 증거, 원인, 수정, 회귀 검증을 분리해 기록한다.