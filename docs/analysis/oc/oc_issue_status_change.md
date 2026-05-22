## Operation Contract

[Operation Contract : getIssueDetail]

- Operation : getIssueDetail(issueId : int, newState : String, comment : String)
- Cross References : Use Case UC09 (이슈 상태 변경), UC05 (이슈 상세 정보 확인)
- Pre-conditions:
  1. 사용자가 시스템에 로그인되어 있어야 한다.(currentUser 존재)
  2. 입력받은 issueId 에 해당하는 이슈가 시스템(JSON)에 존재해야 한다.
- Post-conditions:
  - [Case 1: Success] 현재 유저의 Role이 변경 권한을 가지고 있고, newStatus가 현재 상태와 다를 경우 :
    - Issue 객체의 Status가 newStatus 로 변경되었다.
    - 입력받은 comment를 내용으로 하는 새로운 Comment 객체가 생성되어 해당 이슈의 comments 리스트에 추가되었다
    - 변경된 상태가 Fixed일 경우, fixerId 필드가 현재 유저 ID로 업데이트되었다.
    - 시스템이 업데이트된 전체 이슈 정보를 updatedIssueInfo 에 담아 반환하였다.
  - [Case 2: Duplicate Status] newStatus 가 현재 이슈의 상태와 동일하고 댓글(comment) 입력만 존재할 경우  :
    - 시스템 내부의 어떠한 상태 변경도 일어나지 않으나, 입력받은 comment 객체는 리스트에 추가하였다.
    - 시스템이 “No changes detected.” 에러 메시지를 반환하였다.
  - [Case 3: Permission Denied] 현재 유저의 Role이 해당 상태 변경 권한을 가지고 있지 않을 경우 :
    - 시스템 내부의 어떠한 상태 변경도 일어나지 않았다.
    - 시스템이 “You do not have permission for this transition.” 에러 메시지를 반환하였다.

[Operation Contract : requestStatusChange]

- Operation : requestStatusChange(newStatus, comment)
- Pre-conditions:
  1. 시스템에 유효한 세션(로그인)이 존재해야 한다.
  2. 현재 시스템 컨텍스트에 수정 대상인 이슈 객체(currentIssue)가 활성화되어 있어야 한다
- Post-conditions (Success Case):

  [Case 1: Success] 현재 유저의 Role이 변경 권한을 가지고 있고, newStatus가 현재 상태와 다를 경우:
  - Issue 객체의 Status가 newStatus로 변경되었다.
  - 입력받은 comment를 내용으로 하는 새로운 Comment 객체가 생성되어 해당 이슈의 comments 리스트에 추가되었다.
  - 변경된 상태가 FIXED일 경우, fixerId 필드가 현재 유저 ID로 업데이트되었다.
  - 시스템이 업데이트된 전체 이슈 정보를 updatedIssueInfo에 담아 반환하였다.

  [Case 2: Duplicate Status] newStatus가 현재 이슈의 상태와 동일하고 댓글 입력만 존재할 경우:
  - Issue 객체의 상태 변경은 일어나지 않았으나, 입력받은 comment 객체는 정상적으로 생성되어 comments 리스트에 추가되었다.
  - 시스템이 “No changes detected.” 에러 메시지를 반환하였다.

  [Case 3: Permission Denied] 현재 유저의 Role이 해당 상태 변경 권한을 가지고 있지 않을 경우:
  - 시스템 내부의 어떠한 상태 변경(상태 수정 및 댓글 추가)도 일어나지 않았다.
  - 시스템이 “You do not have permission for this transition.” 에러 메시지를 반환하였다.