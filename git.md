# Git History Cleanup Commands

이 문서는 이전 작업에서 잘못 올라간 빌드 파일들을 지우고, 여러 커밋을 하나로 깔끔하게 합치기(Squash) 위해 사용했던 Git 명령어들의 흐름과 설명을 담고 있습니다.

### 사용된 전체 명령어
```bash
git reset --soft f9410b7
git rm -r -q --cached .
git add .
git commit --amend -m "feat: initial implementation of ArrayBlockingQueue"
git push -f origin master
```

---

### 단계별 상세 설명

#### 1. `git reset --soft f9410b7`
- **목적:** 커밋 내역을 가장 첫 번째 커밋(`f9410b7`) 상태로 되돌립니다.
- **`--soft` 옵션:** 기존 파일의 변경 사항이나 작업 폴더의 상태는 그대로 유지하면서, Git의 커밋 이력(HEAD)만 과거로 이동시킵니다. 이를 통해 이후 모든 변경 사항을 하나의 커밋으로 뭉칠 수 있게 됩니다.

#### 2. `git rm -r -q --cached .`
- **목적:** 현재 Git이 추적하고 있는 모든 파일의 기록을 메모리(인덱스)에서 지웁니다.
- **`--cached` 옵션:** 로컬(컴퓨터)에 있는 실제 파일은 지우지 않고, 오직 Git의 추적 목록에서만 삭제합니다. 이 과정을 거쳐야 나중에 다시 추가할 때 새로 작성한 `.gitignore` 규칙이 완벽하게 적용됩니다.

#### 3. `git add .`
- **목적:** 프로젝트 폴더의 모든 파일을 다시 Git의 추적 대상(Staging Area)으로 추가합니다.
- **결과:** 이 때 2번 과정을 거쳤기 때문에 새로 추가한 `.gitignore`의 내용이 반영되어 `.gradle`, `build` 등의 폴더는 제외되고 순수 소스 코드만 추가됩니다.

#### 4. `git commit --amend -m "feat: initial implementation of ArrayBlockingQueue"`
- **목적:** 새로운 커밋을 만들지 않고, 1번에서 돌아갔던 최초 커밋에 변경 사항을 덮어씌웁니다.
- **결과:** 쓸데없는 이전 커밋 기록들은 사라지고, 깔끔한 메시지와 정리된 파일만 포함된 단 하나의 커밋으로 리베이스(Squash) 됩니다.

#### 5. `git push -f origin master`
- **목적:** 로컬에서 완전히 재구성된 커밋 내역을 GitHub(원격 저장소)에 강제로 덮어씌웁니다.
- **`-f` (force) 옵션:** 원격 저장소와 로컬 저장소의 커밋 역사가 달라졌기 때문에(로컬에서 역사를 조작했으므로), 원격 저장소의 경고를 무시하고 로컬 기준으로 덮어씁니다.
