#!/bin/bash

# Let's Encrypt 초기 인증서 발급 스크립트
# 최초 1회만 실행하면 됩니다.

DOMAIN="taja.myvnc.com"

if [ -f .env ]; then
  EMAIL=$(grep -s '^CERTBOT_EMAIL=' .env | cut -d'=' -f2)
fi

echo ">>> nginx 시작 (HTTP only)..."

# 1. certbot 인증용 디렉토리 생성
mkdir -p ./certbot/www
mkdir -p ./certbot/conf

# 2. 임시 자체서명 인증서 생성 (nginx가 시작할 수 있도록)
mkdir -p ./certbot/conf/live/$DOMAIN
openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
  -keyout ./certbot/conf/live/$DOMAIN/privkey.pem \
  -out ./certbot/conf/live/$DOMAIN/fullchain.pem \
  -subj "/CN=$DOMAIN"

echo ">>> 임시 인증서 생성 완료"

# 3. nginx + app 시작
docker compose up -d nginx

echo ">>> 5초 대기..."
sleep 5

# 4. 임시 인증서 삭제
rm -rf ./certbot/conf/live/$DOMAIN

# 5. Let's Encrypt 인증서 발급
echo ">>> Let's Encrypt 인증서 발급 중..."

if [ -z "$EMAIL" ]; then
  EMAIL_ARG="--register-unsafely-without-email"
else
  EMAIL_ARG="--email $EMAIL"
fi

docker compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  $EMAIL_ARG \
  --agree-tos \
  --no-eff-email \
  -d $DOMAIN

# 6. nginx 재시작 (실제 인증서 적용)
echo ">>> nginx 재시작..."
docker compose restart nginx

echo ">>> HTTPS 설정 완료! https://$DOMAIN 으로 접속해보세요."
