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
- `i.reporter := currentUser`로 설정되었다. *(attribute modification)*
- `i.reportedDate := 현재 시각`으로 설정되었다. *(attribute modification)*
- `i.status := NEW`로 설정되었다. *(attribute modification)*
- `i.issueId := 시스템 생성 고유 식별자`로 설정되었다. *(attribute modification)*
- `i.comments := []`로 초기화되었다. *(attribute modification)*
- `i`와 `currentProject` 간 연관관계가 형성되었다. *(association formed)*

## 작성 노트

- Postcondition은 Larman 원칙에 따라 *상태 변화*(instance creation / attribute modification / association formed)만 기술한다. 알고리즘, UI 동작, 검증 로직은 OC 범위 밖이다.
- `issueId` 생성 방식(UUID, sequential 등)은 클래스 다이어그램 단계에서 결정한다.
- `priority` 기본값(MAJOR) 처리는 시스템 책임. 명세 2.3절 *"기본값은 major"* 직접 인용 + MVC 원칙(비즈니스 룰은 모델)에 따라 시스템이 default 설정.

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
| --- | --- | --- |
| 2026-05-11 | 초안 작성 | 김태영 |

> ⚠️ 백엔드 도메인 모델 확정 후 속성명·연관관계 정합성 점검 예정 (5/12 회의)