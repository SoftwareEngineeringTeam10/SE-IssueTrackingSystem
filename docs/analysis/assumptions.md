# SSD/OC 작성 가정사항

- 작성: 김태영
- 작성일: 2026-05-10
- 목적: 백엔드 도메인 모델이 확정되기 전 작성하는 SSD/OC의 전제 조건을 명시

백엔드 도메인 모델이 확정되면 정합성 점검 후 갱신할 것.

---

## 가정 1. 이슈 상태 (6단계)

정상 흐름: `NEW → ASSIGNED → FIXED → RESOLVED → CLOSED`
예외 흐름: `CLOSED → REOPENED → ASSIGNED` (재발 시)

- 출처: 5/7 회의 합의
- 명세서에 필드 명세는 5단계, 시나리오는 6단계로 차이가 있어서 시나리오 쪽을 따름 (FIXED 추가)

## 가정 2. 이슈 필드 구성

5/4 회의 발제 기반.

| 필드 | 설정 방식 | 비고 |
|---|---|---|
| Title | 사용자 입력 | 필수 |
| Description | 사용자 입력 | 필수 |
| Reporter | 자동 | 현재 로그인 유저 |
| Reported Date | 자동 | 현재 시각 |
| Priority | 사용자 선택 | 5단계, 기본값 MAJOR |
| Status | 자동 | 초기 NEW |
| Assignee | 선택 | PL이 배정 |
| Fixer | 선택 | FIXED 상태로 변경한 dev |
| Comments | 시스템 누적 | 날짜 포함 |

Priority 5단계: `BLOCKER` / `CRITICAL` / `MAJOR` / `MINOR` / `TRIVIAL`

## 가정 3. include / extend 관계

5/7 회의 합의 기준.

**include**

| Base UC | Included UC | 근거 |
|---|---|---|
| UC08 담당자 배정 | UC05 이슈 검색 | PL이 검색 후 배정 |
| UC09 이슈 상태 변경 | UC07 코멘트 추가 | 상태 변경 시 코멘트 자동 추가 |

**extend**

| Extending UC | Base UC | 근거 |
|---|---|---|
| UC11 추천 확인 | UC08 담당자 배정 | 선택 기능, 조건부 |
| UC07 코멘트 추가 | UC06 이슈 상세 조회 | 상세 조회 중 조건부 코멘트 |

## 가정 4. 시스템 오퍼레이션 명명

- camelCase 사용 (예: `createIssue`, `changeStatus`, `addComment`)
- 백엔드 메서드명과 맞추기로 함 (5/12 회의에서 최종 확정)

## 가정 5. 액터 (4종)

| 액터 | 설명 |
|---|---|
| Admin | 시스템 관리자, 프로젝트/계정 추가 |
| PL | Project Leader, 이슈 배정/통계/추천 활용 (closed 권한 여부 5/12 확정 예정) |
| Developer | 이슈 처리(fixed), 코멘트 작성 |
| Tester | 이슈 등록, 검증 후 resolved 처리 |

---

## 변경 이력

- 2026-05-10: 초안 작성 (김태영)
- 백엔드 도메인 모델 확정 후 정합성 점검 예정
