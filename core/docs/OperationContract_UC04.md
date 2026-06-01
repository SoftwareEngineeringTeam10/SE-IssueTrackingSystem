### UC04 이슈 등록

**Operation:** createIssue(projectId, newIssue, currentUser)
**Cross Ref:** UC04 이슈 등록

**Precondition:**

- currentUser가 로그인된 상태이다.
- currentUser의 role이 {PL, DEV, TESTER} 중 하나이다.
- projectId에 해당하는 Project가 존재한다.

**Postcondition:**

- Issue 인스턴스 i가 생성되었다.
- i가 해당 Project 인스턴스와 연관관계를 맺었다 (contains).
- i가 currentUser(보고자)와 연관관계를 맺었다 (reports).
- i의 status가 NEW가 되었다.
- i의 priority가 MAJOR가 되었다 (별도로 지정되지 않은 경우).
- i의 reportedDate가 현재 시각으로 설정되었다.