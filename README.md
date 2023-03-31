# 2ntrip Backend Refactor
## Description
2022.03.30 부로, 진행중이던 [2ntrip-API-Kotlin](https://github.com/Entrip-Ajou/2ntrip-API-Kotlin) 프로젝트의 **리펙토링을 진행하는 프로젝트** 입니다. `에자일 방법론`에 기초하여 [Jira](https://refactor2ntrip.atlassian.net/jira/software/projects/REF/boards/2
) 툴을 사용해서, 1주 단위의 Sprint로 리펙토링을 진행, 아래와 같은 [목표](https://github.com/Entrip-Ajou/2ntrip-Backend-Refactor#Objective)를 이루고자 합니다. 세부 사항은 다음과 같습니다. <br>
* Public Cloud : **AWS EC2** (Amazon Linux 2)
* Language : **Kotlin** based on **java-1.8.0**


## Objective
`Clean Code` : 스파게티 코드와 같은 코드 개선 및 비즈니스 로직의 <br> 
`TDD` : 테스트 기반의 개발 및 테스트 코드를 통한 서버 오류 점검 <br>
`Performance` : N+1 쿼리와 같은 성능 저하 개선 <br>
`CI/CD` : Docker 등 컨테이너 환경 사용과 더불어 자동 CI/CD 구축 <br>

## Credits and References
이동환 (Github ID : hwanld), 이진희 (Github ID : Egenieee) <br>
Jira : https://refactor2ntrip.atlassian.net/jira/software/projects/REF/boards/2 <br>
Confluence : https://refactor2ntrip.atlassian.net/wiki/spaces/REF/overview <br>


