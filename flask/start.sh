#!/bin/bash

# Docker 이미지 빌드
docker build -t flask-app:v1 .

# Docker 컨테이너 실행
docker run -d -p 80:5000 flask-app:v1