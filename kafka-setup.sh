#!/bin/bash

# Kafka 토픽 생성 스크립트
echo "Kafka 토픽 생성 중..."

# profile-events 토픽 생성 (프로필 업데이트 이벤트)
docker exec -it kafka-1 kafka-topics --create \
  --bootstrap-server localhost:9091 \
  --topic profile-events \
  --partitions 3 \
  --replication-factor 3 \
  --config min.insync.replicas=2

# nickname-changed-events 토픽 생성 (닉네임 변경 이벤트)
docker exec -it kafka-1 kafka-topics --create \
  --bootstrap-server localhost:9091 \
  --topic nickname-changed-events \
  --partitions 3 \
  --replication-factor 3 \
  --config min.insync.replicas=2

# 토픽 목록 확인
echo ""
echo "생성된 토픽 목록:"
docker exec -it kafka-1 kafka-topics --list --bootstrap-server localhost:9091

# 토픽 상세 정보 확인
echo ""
echo "profile-events 토픽 정보:"
docker exec -it kafka-1 kafka-topics --describe \
  --bootstrap-server localhost:9091 \
  --topic profile-events

echo ""
echo "nickname-changed-events 토픽 정보:"
docker exec -it kafka-1 kafka-topics --describe \
  --bootstrap-server localhost:9091 \
  --topic nickname-changed-events

echo ""
echo " Kafka 토픽 생성 완료!"
echo "Kafka UI 접속: http://localhost:8989"
