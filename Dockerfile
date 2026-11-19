# 1. Dùng hệ điều hành thu gọn có cài sẵn Java 17 làm nền
FROM eclipse-temurin:17-jre-alpine

# 2. Tạo thư mục làm việc chính bên trong container
WORKDIR /app

# 3. Copy file JAR (file code Spring Boot đã biên dịch) từ máy Host vào container
COPY target/*.jar app.jar

# 4. Khai báo cổng ứng dụng (chỉ để document, Spring vẫn chạy cổng cấu hình trong properties)
EXPOSE 8080

# 5. Lệnh thực thi bật ứng dụng Spring Boot khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]

# 6. Cài cppckeck
RUN apt-get update && apt-get install -y --no-install-recommends \
      cppcheck python3 python3-venv curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# 7. Cài Bandit và Flake8
RUN python3 -m venv /opt/venv \
 && /opt/venv/bin/pip install --no-cache-dir bandit==1.7.10 flake8==7.1.1

# 8. Cài SpotBugs
ARG SPOTBUGS_VERSION=4.8.6
RUN curl -fsSL https://github.com/spotbugs/spotbugs/releases/download/${SPOTBUGS_VERSION}/spotbugs-${SPOTBUGS_VERSION}.tgz \
    | tar xz -C /opt \
 && ln -s /opt/spotbugs-${SPOTBUGS_VERSION} /opt/spotbugs

# 9. Cấu hình PATH
ENV PATH="/opt/venv/bin:/opt/spotbugs/bin:${PATH}"