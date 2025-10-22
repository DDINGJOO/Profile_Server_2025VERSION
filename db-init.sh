#!/bin/bash

# ===========================================
# Database Initialization Script
# ===========================================
# MariaDB 초기화 및 스키마/데이터 생성

set -e

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "  Profile Server DB 초기화"
echo "======================================"
echo ""

# 환경변수 로드
if [ -f .env ]; then
    echo -e "${GREEN}✓${NC} .env 파일 로드"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo -e "${RED}✗${NC} .env 파일이 없습니다."
    exit 1
fi

# Docker Compose 실행 여부 확인
if ! docker ps | grep -q mariadb; then
    echo -e "${YELLOW}⚠${NC} MariaDB 컨테이너가 실행 중이 아닙니다."
    echo "다음 명령어로 실행하세요: docker-compose up -d mariadb"
    exit 1
fi

echo -e "${GREEN}✓${NC} MariaDB 컨테이너 실행 확인"

# MariaDB 준비 대기
echo "MariaDB 준비 대기 중..."
sleep 5

# 데이터베이스 생성 (이미 있으면 건너뜀)
echo ""
echo "======================================"
echo "  1. 데이터베이스 생성"
echo "======================================"

docker exec mariadb mysql -uroot -p"${DATABASE_PASSWORD}" -e "
CREATE DATABASE IF NOT EXISTS ${DATABASE_NAME}
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 데이터베이스 '${DATABASE_NAME}' 생성/확인 완료"
else
    echo -e "${RED}✗${NC} 데이터베이스 생성 실패"
    exit 1
fi

# 스키마 생성
echo ""
echo "======================================"
echo "  2. 테이블 스키마 생성"
echo "======================================"

docker exec -i mariadb mysql -uroot -p"${DATABASE_PASSWORD}" ${DATABASE_NAME} < src/main/resources/sql/schema.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 스키마 생성 완료"
else
    echo -e "${RED}✗${NC} 스키마 생성 실패"
    exit 1
fi

# 초기 데이터 삽입
echo ""
echo "======================================"
echo "  3. 초기 데이터 삽입"
echo "======================================"

docker exec -i mariadb mysql -uroot -p"${DATABASE_PASSWORD}" ${DATABASE_NAME} < src/main/resources/sql/Data.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 초기 데이터 삽입 완료"
else
    echo -e "${RED}✗${NC} 초기 데이터 삽입 실패"
    exit 1
fi

# 테이블 확인
echo ""
echo "======================================"
echo "  4. 테이블 확인"
echo "======================================"

TABLES=$(docker exec mariadb mysql -uroot -p"${DATABASE_PASSWORD}" ${DATABASE_NAME} -e "SHOW TABLES;" 2>/dev/null | tail -n +2)

echo "$TABLES"
TABLE_COUNT=$(echo "$TABLES" | wc -l | tr -d ' ')

echo ""
echo -e "${GREEN}✓${NC} 총 ${TABLE_COUNT}개 테이블 생성됨"

# 데이터 개수 확인
echo ""
echo "======================================"
echo "  5. 데이터 확인"
echo "======================================"

docker exec mariadb mysql -uroot -p"${DATABASE_PASSWORD}" ${DATABASE_NAME} -e "
SELECT 'location_names' as table_name, COUNT(*) as count FROM location_names
UNION ALL
SELECT 'genre_name', COUNT(*) FROM genre_name
UNION ALL
SELECT 'instrument_name', COUNT(*) FROM instrument_name
UNION ALL
SELECT 'user_info', COUNT(*) FROM user_info;
" 2>/dev/null

# 인덱스 확인
echo ""
echo "======================================"
echo "  6. 인덱스 확인"
echo "======================================"

INDEX_COUNT=$(docker exec mariadb mysql -uroot -p"${DATABASE_PASSWORD}" ${DATABASE_NAME} -e "
SELECT COUNT(*) as count
FROM information_schema.statistics
WHERE table_schema = '${DATABASE_NAME}'
  AND index_name != 'PRIMARY';
" 2>/dev/null | tail -n 1)

echo -e "${GREEN}✓${NC} 총 ${INDEX_COUNT}개 인덱스 생성됨"

# 완료 메시지
echo ""
echo "======================================"
echo "  ✅ 초기화 완료!"
echo "======================================"
echo ""
echo "접속 정보:"
echo "  • Host: localhost"
echo "  • Port: ${DATABASE_PORT}"
echo "  • Database: ${DATABASE_NAME}"
echo "  • Username: ${DATABASE_USER_NAME}"
echo ""
echo "연결 테스트:"
echo "  mysql -h localhost -P ${DATABASE_PORT} -u ${DATABASE_USER_NAME} -p ${DATABASE_NAME}"
echo ""
echo "Spring Boot 애플리케이션 실행:"
echo "  ./gradlew bootRun"
echo ""
