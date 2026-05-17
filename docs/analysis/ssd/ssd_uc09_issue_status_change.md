

- getIssueDetail(issueId) : 사용자가 특정 이슈의 상세 정보를 시스템이 요청한다.
- displayIssueDetail(issueData, currentStatus) : 시스템이 해당 이슈의 현재 상태(currentStatus)와 기존 데이터들을 화면에 보여준다.
- requestStatusChange(newStatus, comment) : 사용자가 바꾸고 싶은 상태와 코멘트를 입력하여 변경을 요청한다.
- updateStatusAndCreateComment() : 시스템이 스스로의 상태를 갱신하는 내부 로직을 실행한다.
- ALT
    1. Case 1(성공) : 상태가 다르고 권한이 있을 경우, 내부적으로 상태를 업데이트하고 코멘트를 생성(updateStatusAndCreateComment)한 뒤 성공 메시지(notifySuccessWithUpdatedIssue)를 보낸다.
    2. Case 2 (중복) : 현재 상태와 똑같은 상태로 변경하려고 할 때, “변화 없음”에러(notifyError)를 보낸다.
    3. Case 3 (거부) : 해당 상태로 바꿀 권한이 없는 계정일 경우, 권한 거부 메시지(notifyError)를 보낸다.

