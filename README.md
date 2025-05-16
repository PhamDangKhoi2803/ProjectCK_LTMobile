#**Ứng dụng trò chuyện trực tuyến**

## 👨‍💻 **Nhóm 27**

| Họ tên                              | MSSV         |
| ----------------------------------- | ------------ |
| Phạm Đăng Khôi                      | 22110357     |
| Nguyễn Văn Hùng                     | 22110338     |

---

## 📌 **Giới Thiệu Dự Án**

**Mục đích dự án**
- Trong thời buổi công nghê xu hướng giao tiếp hiện đại, mọi lúc mọi nơi ở bất cứ đâu như là một nhu cầu thiết yếu của mỗi người
- Ứng dụng này được xây dựng nhằm hỗ trợ mọi người có thể kết nối với nhau đễ dàng hơn chỉ cần thông qua internet
---
**Công nghệ sử dụng**
Để xây dựng và phát triển ứng dụng ***Trò chuyện trực tuyến thời gian thực*** , nhóm sử dụng các công nghệ sau:
- **Android(Java)**:
  Nền tảng chính để xây dựng giao diện người dùng và xử lý các thao tác trên thiết bị di động.
- **Retrofit**:
  Thư viện của Android hỗ trợ thực hiện các yêu cầu mạng (HTTP), dùng để giao tiếp với hệ thống backend thông qua API.
- **SpringBoot(Java)**:
  Framework phía backend dùng để xây dựng các RESTful API, xử lý logic nghiệp vụ và truy xuất cơ sở dữ liệu.
- **MySQL**:
  Hệ quản trị cơ sở dữ liệu dùng để lưu trữ thông tin người dùng, user, tin nhắn, bạn bè và các dữ liệu liên quan đến hệ thống.

---

## ⚙️ **Cơ Chế Vận Hành**

**Hệ thống gồm 1 vai trò:**

- 👤 **USER (Người dùng)**

  - Đăng nhập, đăng ký, đổi mật khẩu.
  - Nhắn tin
  - Kết bạn, hủy kết bạn
  - Tạo nhóm, rời nhóm, xóa nhóm
  - Gọi voice call, video call
  - Cập nhật thông tin cá nhân
  - Thay đổi giao diện sáng/tối
  - Trò chuyện với AI
  - Xem nhật ký cuộc gọi
---

## 🛠️ **Hướng Dẫn Setup Dự Án**

### 🔧 **1. Cài đặt ban đầu**
Cách 1: Tạo thư mục → Clone repo dự án từ Github [https://github.com/PhamDangKhoi2803/ProjectCK_LTMobile.git](https://github.com/PhamDangKhoi2803/ProjectCK_LTMobile.git)

Cách 2: Download source code của toàn bộ dự án được sinh viên gửi trong phần nộp dự án cuối kì, sau đó tiến hành giải nén 

### 🗄️ **2. Cài đặt cơ sở dữ liệu**

- Mở **MySQL Workbench**:
  - Tạo database tên `ltdd`
  - Vào `Server → Data Import`
  - Chọn `Import from Dump Project Folder`
  - Chọn thư mục `db-cruebee-app` trong thư mục đi kèm với dự án 

### 💻 **3. Mở và chạy project**

- Mở **Android Studio**:
  - `File → Open → chọn thư mục vừa clone app về( hoặc chọn thư mục chứa project nếu tải trực tiếp source code về) 
- Mở **IntelliJ IDEA**
  - `File → Open → chọn thư mục vừa clone phần API về( hoặc chọn thư mục chứa API cho dự án nếu tải trực tiếp source code về) 
  - Mở file `application.properties`, chỉnh sửa phần cấu hình database:
    ```
    spring.datasource.username: <Tên người dùng MySQL>
    spring.datasource.password: <Mật khẩu MySQL>
    ```
- **RUN** bấm run cả 2 bên ứng dụng để tiến hành chạy ứng dụng
