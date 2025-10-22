#!/bin/bash

echo "🔍 서비스 헬스체크 시작..."
echo ""

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_service() {
    local service_name=$1
    local container_name=$2
    local check_command=$3

    echo -n "[$service_name] "

    if docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
        if eval "$check_command" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 정상${NC}"
            return 0
        else
            echo -e "${YELLOW} 실행 중이나 응답 없음${NC}"
            return 1
        fi
    else
        echo -e "${RED}✗ 중지됨${NC}"
        return 2
    fi
}

# Zookeeper 확인
check_service "Zookeeper" "zookeeper" "docker exec zookeeper zkServer.sh status"

# Kafka 브로커 확인
check_service "Kafka-1" "kafka-1" "docker exec kafka-1 kafka-broker-api-versions --bootstrap-server localhost:9091"
check_service "Kafka-2" "kafka-2" "docker exec kafka-2 kafka-broker-api-versions --bootstrap-server localhost:9092"
check_service "Kafka-3" "kafka-3" "docker exec kafka-3 kafka-broker-api-versions --bootstrap-server localhost:9093"

# Kafka UI 확인
check_service "Kafka UI" "kafka-ui" "curl -s http://localhost:8989"

# MariaDB 확인
check_service "MariaDB" "mariadb" "docker exec mariadb mysqladmin -uroot -p'pass123#' ping"

# Redis 확인
check_service "Redis" "redis" "docker exec redis redis-cli ping"

echo ""
echo " Kafka 클러스터 상태:"
if docker exec kafka-1 kafka-topics --list --bootstrap-server localhost:9091 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 클러스터 정상 작동${NC}"
    echo ""
    echo "토픽 목록:"
    docker exec kafka-1 kafka-topics --list --bootstrap-server localhost:9091 2>/dev/null || echo "토픽 없음"
else
    echo -e "${RED}✗ 클러스터 응답 없음${NC}"
fi

echo ""
echo " 접속 정보:"
echo "  • Kafka UI: http://localhost:8989"
echo "  • Kafka Brokers: localhost:29091, localhost:29092, localhost:29093"
echo "  • MariaDB: localhost:4000 (user: root, db: profiles)"
echo "  • Redis: localhost:6379"

echo ""
echo " 헬스체크 완료"
