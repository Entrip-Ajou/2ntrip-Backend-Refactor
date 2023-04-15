#!/usr/bin/env bash

# profile.sh
# 미사용 중인 profile을 잡는다.

function find_idle_profile()
{
    # curl 결과로 현재 동작중인 profile를 CURRENT_PROFILE에 저장
    # curl 명령어를 통해서, 해당 url (8080포트 : real1)의 Http Status Code를 받아옴.
    # real1 server port : 8081, real2 server port : 8082
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" https://2ntrip.link/api/v2/profile)

    if [ ${RESPONSE_CODE} -ne 200 ]
    then
        CURRENT_PROFILE=none
    else
        CURRENT_PROFILE=$(curl -s https://2ntrip.link/api/v2/profile)
    fi

    # IDLE_PROFILE : nginx와 연결되지 않은 profile
    if [ ${CURRENT_PROFILE} == real1 ]
    then
      IDLE_PROFILE=real2
    else
      IDLE_PROFILE=real1
    fi

    # bash script는 값의 반환이 안된다.
    # echo로 결과 출력 후, 그 값을 잡아서 사용한다.
    echo "${IDLE_PROFILE}"
}

# 쉬고 있는 profile의 port 찾기
function find_idle_port()
{
    IDLE_PROFILE=$(find_idle_profile)

    if [ ${IDLE_PROFILE} == real1 ]
    then
      echo "8081"
    else
      echo "8082"
    fi
}