## Operation Contract

[Operation Contract : getIssueDetail]

- Operation : getIssueDetail(issueId : int)
- Cross References : Use Case UC09 (이슈 상태 변경), UC05 (이슈 상세 정보 확인)
- Pre-conditions:
    1. 사용자가 시스템에 로그인되어 있어야 한다.(currentUser 존재)
    2. 입력받은 issueId 에 해당하는 이슈가 시스템(JSON)에 존재해야 한다.
- Post-conditions:
    1. 시스템이 요청된 IssueId 와 일치하는 Issue 객체를 조회한다.
    2. 해당 이슈의 모든 필드 정보(제목, 설명, 현재 상태, 보고자, 담당자, 해결자, 보고 날짜 등)가 사용자 인터페이스에 전달된다.
    3. 해당 이슈에 연결된 comments 리스트가 조회되어 사용자에게 반환된다.

[Operation Contract : requestStatusChange]

- Operation : requestStatusChange(newStatus, comment)
- Pre-conditions:
    1. 시스템에 유효한 세션(로그인)이 존재해야 한다.
    2. 수정 대상인 issueId가 유효해야 한다.
    3. newStatus는 현재 상태와 달라야 한다.
    4. 현재 유저의 Role이 변경 권한을 가져야 한다.
- Post-conditions (Success Case):
    1. Issue 객체의 Status가 newStatus로 변경된다.
    2. 입력받은 comment를 내용으로 하는 새로운 Comment 객체가 생성되어 해당 이슈의 comments 리스트에 추가된다.
    3. 상태가 Fixed일 경우, fixerId 필드가 현재 유저 ID로 업데이트된다.
    4. 시스템은 업데이트된 전체 이슈 정보를 updatedIssueInfo 에 담아 반환한다.