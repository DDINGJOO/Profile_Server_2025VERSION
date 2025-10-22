# 로컬 개발 환경 설정 가이드

## 📋 사전 요구사항

- Docker Desktop (macOS용)
- Docker Compose

## 🚀 빠른 시작

### 1. 환경변수 설정 확인

`.env` 파일이 프로젝트 루트에 있는지 확인:

```bash
# .env 파일 내용
DATABASE_HOST=localhost
DATABASE_PORT=4000
DATABASE_NAME=profiles
DATABASE_USER_NAME=root
DATABASE_PASSWORD="pass123#"

REDIS_HOST=localhost
REDIS_PORT=6379

KAFKA_URL1=localhost:29091
KAFKA_URL2=localhost:29092
KAFKA_URL3=localhost:29093
```

### 2. Docker Compose 실행

전체 인프라 시작:

```bash
# 전체 서비스 시작 (백그라운드 실행)
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

특정 서비스만 시작:

```bash
# Kafka만 시작
docker-compose up -d zookeeper kafka-1 kafka-2 kafka-3 kafka-ui

# MariaDB만 시작
docker-compose up -d mariadb

# Redis만 시작
docker-compose up -d redis
```

### 3. Kafka 토픽 생성

```bash
# 토픽 자동 생성 스크립트 실행
./kafka-setup.sh
```

수동으로 토픽 생성:

```bash
# 컨테이너 접속
docker exec -it kafka-1 bash

# 토픽 생성
kafka-topics --create \
  --bootstrap-server localhost:9091 \
  --topic profile-events \
  --partitions 3 \
  --replication-factor 3
```

### 4. 서비스 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 서비스 헬스체크
docker-compose ps --services
```

## 🔌 서비스 접속 정보

### Kafka
- **Broker 1**: `localhost:29091`
- **Broker 2**: `localhost:29092`
- **Broker 3**: `localhost:29093`
- **Kafka UI**: http://localhost:8989

### Database
- **MariaDB**: `localhost:4000`
  - User: `root`
  - Password: `pass123#`
  - Database: `profiles`

### Cache
- **Redis**: `localhost:6379`

## 📊 Kafka UI 사용법

1. 브라우저에서 http://localhost:8989 접속
2. 클러스터 이름: `local-cluster`
3. 다음 기능 사용 가능:
   - 토픽 생성/삭제/조회
   - 메시지 발행/소비
   - Consumer Group 모니터링
   - 브로커 상태 확인

## 🛠 유용한 명령어

### 로그 확인
```bash
# 특정 서비스 로그
docker-compose logs -f kafka-1

# 모든 서비스 로그
docker-compose logs -f

# 최근 100줄 로그
docker-compose logs --tail=100 kafka-1
```

### 토픽 관리
```bash
# 토픽 목록 조회
docker exec -it kafka-1 kafka-topics --list --bootstrap-server localhost:9091

# 토픽 상세 정보
docker exec -it kafka-1 kafka-topics --describe \
  --bootstrap-server localhost:9091 \
  --topic profile-events

# 메시지 발행 (테스트)
docker exec -it kafka-1 kafka-console-producer \
  --bootstrap-server localhost:9091 \
  --topic profile-events

# 메시지 소비 (테스트)
docker exec -it kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9091 \
  --topic profile-events \
  --from-beginning
```

### Consumer Group 관리
```bash
# Consumer Group 목록
docker exec -it kafka-1 kafka-consumer-groups \
  --bootstrap-server localhost:9091 \
  --list

# Consumer Group 상세 정보
docker exec -it kafka-1 kafka-consumer-groups \
  --bootstrap-server localhost:9091 \
  --group profile-consumer-group \
  --describe
```

### 데이터베이스 접속
```bash
# MariaDB 접속
docker exec -it mariadb mysql -uroot -p"pass123#" profiles

# Redis 접속
docker exec -it redis redis-cli
```

## 🔄 서비스 재시작

```bash
# 전체 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart kafka-1

# 설정 변경 후 재시작 (이미지 재빌드)
docker-compose up -d --force-recreate
```

## 🧹 정리

```bash
# 모든 컨테이너 중지
docker-compose down

# 컨테이너 + 볼륨 삭제 (데이터 완전 삭제)
docker-compose down -v

# 컨테이너 + 이미지 삭제
docker-compose down --rmi all
```

## ⚠️ 문제 해결

### Kafka 연결 실패
```bash
# Kafka 브로커 상태 확인
docker-compose ps kafka-1 kafka-2 kafka-3

# Zookeeper 상태 확인
docker exec -it zookeeper zkServer.sh status

# 로그 확인
docker-compose logs kafka-1 kafka-2 kafka-3
```

### 포트 충돌
```bash
# 포트 사용 확인
lsof -i :29091
lsof -i :4000
lsof -i :6379

# 충돌 시 기존 프로세스 종료 후 재시작
docker-compose down
docker-compose up -d
```

### 디스크 공간 부족
```bash
# 사용하지 않는 컨테이너/이미지 정리
docker system prune -a

# 볼륨 정리
docker volume prune
```

## 🔧 macOS 특화 설정

### Docker Desktop 메모리 설정
1. Docker Desktop 열기
2. Settings → Resources → Memory
3. 최소 4GB 이상 할당 권장 (Kafka 클러스터용)

### 파일 공유 설정
1. Docker Desktop 열기
2. Settings → Resources → File Sharing
3. 프로젝트 디렉토리 경로 추가

## 📝 Spring Boot 애플리케이션 실행

```bash
# 환경변수 자동 로드
./gradlew bootRun

# 또는 IDE에서 실행 시 .env 파일 자동 인식
```

## 🎯 테스트

```bash
# Kafka 메시지 발행 테스트
curl -X POST http://localhost:8080/api/test/kafka \
  -H "Content-Type: application/json" \
  -d '{"message": "test"}'

# Redis 캐시 테스트
curl http://localhost:8080/api/test/redis
```

## 📚 추가 리소스

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka UI GitHub](https://github.com/provectus/kafka-ui)
- [MariaDB Documentation](https://mariadb.com/kb/en/)
- [Redis Documentation](https://redis.io/documentation)