# Operation Contract OC#1: createIssue

## Operation

`createIssue(title: String, description: String, priority: Priority): IssueId`

## Cross References

- **Use Case**: UC04 이슈 등록
- **SSD**: `../ssd/ssd_uc04_register_issue.puml` (2번째 시스템 오퍼레이션)

## Preconditions

- 사용자가 로그인되어 있고, 시스템에 `currentUser` 정보가 존재한다.
- 활성 프로젝트(`currentProject`)가 `selectProject(projectId)`로 선택되어 있다.
- `title`은 길이 1 이상의 문자열이다.
- `description`은 길이 1 이상의 문자열이다.
- `priority`가 명시된 경우, `{BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL}` 중 하나이다. (미지정 호출 허용)

## Postconditions

- 새 `Issue` 인스턴스 `i`가 생성되었다. *(instance creation)*
- `i.title := title`, `i.description := description`으로 설정되었다. *(attribute modification)*
- `priority`가 명시된 경우 `i.priority := priority`, 미지정인 경우 `i.priority := MAJOR`로 설정되었다. *(attribute modification)*
- `i.reporterId := currentUser.id`로 설정되었다. *(attribute modification)*
- `i.reportedDate := 현재 시각`으로 설정되었다. *(attribute modification)*
- `i.status := NEW`로 설정되었다. *(attribute modification)*
- `i.issueId := 시스템 생성 고유 식별자`로 설정되었다. *(attribute modification)*
- `i.comments := []`로 초기화되었다. *(attribute modification)*
- `i`와 `currentProject` 간 연관관계가 형성되었다. *(association formed)*