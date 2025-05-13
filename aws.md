
# 🚀 AWS Ubuntu 기본 세팅 가이드 (SSGFC 프로젝트 기준)
제발제발제발제발제발 한줄씩 복붙하세요
> - 본 설정은 **팀 프로젝트 1: SSGFC** 기준입니다.  
> - **Ubuntu 22.04**, **Java 11**, **MySQL 8**, **Spring Boot 내장 톰캣 사용**
> - 프로젝트는  **Java 11**, **MySQL 8**, **Spring Boot 2.7.14**
> - **파일질라(FireZilla)** 기반 파일 업로드/다운로드  
> - **프리티어가 아닌 인스턴스 사용 시 꼭 중지 또는 삭제할 것, 프리티어도 과금발생할 수 있으니 그냥 영상찍고 삭제 ㄱ**  
> - 로그, JAR, SQL, TMP는 모두 `~/ssgfc` 폴더 기준
> - 참고로 main껄로 업로드 시도 시 막힘 (따로 자바 설정한거 있음 귀찮아서 git push + 병합 안함)

---

## ⚠️ 주의사항

- 인스톨 시 `-y` 옵션으로 `y` 입력 스킵 가능
- Spring Boot 내장 톰캣 사용 시 별도 톰캣 설치 안 해도 됨
- `vi`에서 insert 모드 안 쓸 경우, 한 줄씩 복붙 권장
- 인스턴스가 **프리티어가 아닐 경우**, 실 사용 후 반드시 중지하거나 삭제
- ssgfc.jar 안에 내장톰캣이 있기 때문에 우분투 안에서 톰캣설정 안하고 실행했음.

---

## 🔐 계정 및 기본 설정

```bash
sudo passwd root
sudo passwd ubuntu
```

---

## ☕ Java 11 설치 및 환경 변수 설정

```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk -y
sudo vi /etc/profile
```

`/etc/profile`에 다음 내용 추가:

```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/ext:$JAVA_HOME/lib/tools.jar
```

```bash
source /etc/profile
echo $JAVA_HOME
clear
```

---

## 🌀 (선택) Tomcat 9 설치 및 방화벽 설정

```bash
sudo apt-get install tomcat9 -y
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
sudo ufw enable
sudo ufw status
sudo service tomcat9 start
sudo service tomcat9 stop
systemctl status tomcat9
```

> ※ Spring Boot 내장 톰캣으로 충분히 가능하므로 생략 가능

---

## 🌐 (선택) 네트워크 도구 설치 및 포트 확인

```bash
sudo apt-get install net-tools
netstat -plnt | grep ':8080'
clear
```

---

## 🐬 MySQL 8 설치 및 기본 설정

```bash
sudo apt-get install mysql-server-8.0 -y
sudo mysql -uroot
```

```sql
-- MySQL 내부
use mysql;
UPDATE user SET plugin='caching_sha2_password' WHERE user='root';
flush privileges;
SET password for 'root'@'localhost'='1234';
exit
```

```bash
sudo mysql -uroot -p
```

> ※ DB 생성 시 `create database dbname;` 입력

---

## 🏃 Spring Boot 실행 (SSGFC 프로젝트 기준)

```bash
cd ~/ssgfc
nohup java -jar ssgfc.jar & jobs
```

또는 로그까지 출력:

```bash
nohup java -jar ssgfc.jar > log.txt 2>&1 &
jobs
tail -f log.txt
```

---

## 🧩 DB 적용 (last.sql import)

```bash
mysql -h localhost -P 3306 -uroot -p1234 ssgfc
mysql -h localhost -P 3306 -uroot -p1234 ssgfc < last.sql
SHOW TABLES;
```

---

## 🖼️ 이미지 추출 및 업로드 경로 설정

```bash
cd ~/ssgfc
unzip ssgfc.jar "BOOT-INF/classes/static/images/*" -d tmp

mkdir -p /home/ubuntu/uploads/{userimage,players,board,logo}
cp -r tmp/BOOT-INF/classes/static/images/userimage/* /home/ubuntu/uploads/userimage/
cp -r tmp/BOOT-INF/classes/static/images/players/* /home/ubuntu/uploads/players/
cp -r tmp/BOOT-INF/classes/static/images/board/* /home/ubuntu/uploads/board/
cp -r tmp/BOOT-INF/classes/static/images/logo/* /home/ubuntu/uploads/logo/
chmod -R 755 /home/ubuntu/uploads/
```

---

## 🌐 WebConfig.java - 정적 리소스 매핑

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/userimage/**")
            .addResourceLocations("file:/home/ubuntu/uploads/userimage/");
    registry.addResourceHandler("/images/players/**")
            .addResourceLocations("file:/home/ubuntu/uploads/players/");
    registry.addResourceHandler("/images/board/**")
            .addResourceLocations("file:/home/ubuntu/uploads/board/");
    registry.addResourceHandler("/images/logo/**")
            .addResourceLocations("file:/home/ubuntu/uploads/logo/");
}
```

---

## ✅ 변경된 사항 요약

1. **인스턴스 유형 `t2.medium`으로 업그레이드**
   - 탄력적 IP 사용 안 함
   - 반드시 테스트 후 인스턴스 **중지 또는 삭제**
   - 하루 10시간씩 30일 돌리면 최소 2만원 과금 예측, 최댓값은 예측 불가

2. **우분투 내부에 이미지 파일 직접 업로드**
   - `/home/ubuntu/uploads` 경로에 이미지 저장
   - WebConfig에서 해당 경로 정적 매핑
---


# 💸 AWS 과금 정리 (프리티어인데 요금 나오는 이유 + 현재 상황 분석)

---

## ✅ 1. 프리티어인데도 요금이 발생하는 주요 이유

| 항목 | 설명 | 비고 |
|------|------|------|
| **인스턴스 타입** | `t2.micro`만 프리티어 대상<br> → `t2.medium`, `t3.small` 등은 **프리티어 적용 ❌** | 난 현재 `t2.medium` 사용 중 → 유료 (아직 이 가격은 책정안됨)|
| **EBS 디스크 용량** | 프리티어는 **30GB**까지만 무료 | 초과 시 디스크 요금 발생 |
| **아웃바운드 트래픽** | 매달 **1GB까지만 무료**<br> → 초과하면 GB당 $0.12 과금 | 난 **3GB 사용 → 2GB 과금 대상** |
| **탄력 IP (Elastic IP)** | EC2와 연결 안 됐거나, EC2가 꺼져있으면 **유휴 요금 발생** | 인스턴스 끈 상태 + 탄력 IP 유지하면 돈 나감 |
| **NAT Gateway / VPC Logs** | 숨겨진 유료 서비스. Flow Logs, NAT 등은 무조건 과금 | 사용 시 주의 필요 |

---

## ✅ 2. 내 실제 트래픽 과금 상황 해석 (2025.05.13 14:44 기준)

| 항목 | 상태 |
|------|------|
| 프리티어 트래픽 한도 | 1GB/월 (아웃바운드 기준) |
| 현재까지 사용량 | **3GB** 사용 |
| 초과 사용량 | **2GB 초과** (프리티어 초과됨) |
| 청구된 트래픽 요금 | **현재는 1GB만 청구됨 ($0.12)** |
| 남은 청구 예상 | 나머지 1GB도 곧 청구 예정 → 합계 약 **$0.24** |
| 예상 총 요금 | **$0.24 ~ $0.40 정도 (₩300 ~ 600원)** 예상

---

## ✅ 3. AWS 대시보드에서 본 트래픽 과금 상태

| 항목 | 값 |
|------|-----|
| 서비스 | AWS Data Transfer |
| 프리티어 한도 | 1GB |
| 현재 사용량 | 3GB |
| MTD 실제 사용량 % | **100.00%** → 프리티어 완전 소진 |
| MTD 예상 사용량 % | **258.33%** → 약 2.58GB 초과 예상

---

## ✅ 지금 할 수 있는 조치

| 할 일 | 설명 |
|--------|------|
| EC2 인스턴스 중지 | 트래픽/CPU 요금 방지 |
| 탄력 IP 해제 | 연결되지 않은 탄력 IP 요금 방지 |
| 이미지 리소스 최소화 | 트래픽 줄이기 |
| Budget 알람 설정 | 예산 초과 알림 받기 (ex. 1000원 넘으면 알림) |
| Cost Explorer 활성화 | 트래픽 및 비용 흐름 시각화 (최대 24시간 대기 필요) |

---

## 🧠 추가 팁

- **트래픽은 “나가는 방향”만 과금**됨 (인바운드는 무료)
- **하루만 EC2 풀가동 해도 몇백 원 나올 수 있음**
- **내장 톰캣, 정적 이미지**가 트래픽 유발 주범일 수 있음
- **t2.medium은 프리티어 대상이 절대 아님**

