### UC09 이슈 상태 변경

**Operation:** changeStatus(issueId, newStatus, currentUser)
**Cross Ref:** UC09 이슈 상태 변경

**Precondition:**

- currentUser가 로그인된 상태이다.
- issueId에 해당하는 Issue i가 존재한다.
- i.status에서 newStatus로의 전환이 유효하다.
- currentUser가 newStatus로 변경할 권한을 가진다.

**Postcondition (공통):**

- i의 status가 newStatus로 변경되었다.
- Comment 인스턴스 c가 생성되었다.
- c가 i와 연관관계를 맺었다 (has).
- c가 currentUser(작성자)와 연관관계를 맺었다 (writes).
- c의 content가 상태 변경 내용을 담도록 설정되었다.
- c의 date가 현재 시각으로 설정되었다.

**Postcondition (newStatus = FIXED 인 경우 추가):**

- i가 currentUser(해결자)와 연관관계를 맺었다 (fixes).

**Postcondition (newStatus = REOPENED 인 경우 추가):**

- i와 담당자(assignee) 간의 연관관계가 해제되었다.