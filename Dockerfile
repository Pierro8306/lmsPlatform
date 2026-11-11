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