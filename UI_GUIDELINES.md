# Quy Ước Thiết Kế & Giao Diện UI PetCare (UI Guidelines)

Tài liệu này chứa thông tin về hệ thống màu sắc, typography, các component tái sử dụng và quy trình làm việc chuẩn dành cho AI Agent khi thiết kế/sinh mã nguồn Compose cho ứng dụng **PetCare**.

---

## 1. Bảng Màu Chủ Đạo (Color Palette)

Tất cả màu sắc được định nghĩa trong `com.example.petcare.ui.theme.Color`:

| Tên Biến | Giá Trị Hex | Mô Tả & Cách Sử Dụng |
| :--- | :--- | :--- |
| `GreenPrimary` | `#338C63` | **Màu chủ đạo (Primary Accent)**: Sử dụng cho Nút bấm chính (Primary Button), Icon nổi bật, trạng thái Active, Checkmark. |
| `GreenLight` | `#EBF5EB` | **Màu phụ / Highlight**: Nền thẻ phụ, badge, background cho icon hoặc item được chọn. |
| `BackgroundLight` | `#F7FAF5` | **Màu nền ứng dụng (Screen Background)**: Sử dụng làm màu nền chung cho tất cả các màn hình. |
| `TextPrimary` | `#1A2621` | **Màu chữ chính**: Dùng cho tiêu đề (Heading), văn bản quan trọng, giá trị input. |
| `TextSecondary` | `#6E7A73` | **Màu chữ phụ**: Dùng cho mô tả (Subtitle), nhãn (Label), helper text, placeholder. |
| `BorderLight` | `#DEE5DB` | **Màu viền (Border)**: Viền của Input TextField, Outlined Button, Card viền mảnh. |
| `Color.White` | `#FFFFFF` | **Màu nền Card / Input**: Nền của Ô nhập liệu (TextField), Card container. |
| Error Color | `#E53935` | **Màu báo lỗi**: Dùng cho thông báo lỗi validation hoặc trạng thái cảnh báo. |

---

## 2. Quy Chuẩn Typography & Spacing

* **Primary Font**: Mặc định hệ thống / Sans-serif.
* **Border Radius / Shape**:
  * **Input Fields & Buttons**: `RoundedCornerShape(14.dp)`
  * **Cards / Bottom Sheet / Dialogs**: `RoundedCornerShape(16.dp)` tới `24.dp`
  * **Pill Buttons / Badges**: `RoundedCornerShape(50)` hoặc `CircleShape`
* **Spacing Standard**:
  * Screen Padding: `horizontal = 20.dp` hoặc `24.dp`, `vertical = 16.dp`
  * Item Spacing: `8.dp`, `12.dp`, `16.dp`, `24.dp`

---

## 3. Danh Sách Component Tái Sử Dụng (Reusable Components)

Các component chung nằm trong thư mục `com.example.petcare.ui.components`:

### 3.1. `PetCareTextField`
* **File**: `ui/components/PetCareTextField.kt`
* **Công dụng**: Ô nhập liệu chuẩn với thiết kế viền bo tròn 14dp, hỗ trợ Label, Placeholder, Helper Text (loại input/báo lỗi) và Trailing Icon (ẩn/hiện mật khẩu, icon xóa, v.v.).
* **Cách dùng**:
  ```kotlin
  PetCareTextField(
      value = textValue,
      onValueChange = { textValue = it },
      label = "Tên bé",
      placeholder = "Nhập tên thú cưng",
      helperText = helperText,
      trailingIcon = { /* Icon */ }
  )
  ```

### 3.2. Primary Button (Nút Chính)
* **Quy chuẩn**: Sử dụng `Button` với màu `GreenPrimary`, bo góc `14.dp` hoặc `28.dp`, chữ in đậm màu trắng `Color.White`, chiều cao `50.dp` - `54.dp`.
* **Cách dùng**:
  ```kotlin
  Button(
      onClick = { /* Action */ },
      modifier = Modifier.fillMaxWidth().height(52.dp),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
  ) {
      Text(text = "Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
  }
  ```

### 3.3. Secondary / Outlined Button (Nút Phụ)
* **Quy chuẩn**: Sử dụng `OutlinedButton` với màu nền `Color.White`, viền `BorderLight`, chữ `TextPrimary`.

### 3.4. Top Navigation Bar (Thanh Tiêu Đề Top)
* **Quy chuẩn**: Hàng chứa nút Back (`IconButton` góc trái) và Title ở giữa/trái. Khoảng cách an toàn với `statusBarsPadding()`.

---

## 4. Quy Cấu Trúc Thư Mục & Code Conventions

```
app/src/main/java/com/example/petcare/
├── ui/
│   ├── theme/        # Color.kt, Theme.kt, Type.kt
│   ├── components/   # PetCareTextField.kt, TopBar.kt, PetCard.kt...
│   ├── navigation/   # AppScreen.kt, NavGraph.kt
│   └── screens/      # LoginScreen.kt, HomeScreen.kt, AddPetCameraScreen.kt...
└── utils/            # InputTypeUtils.kt, Formatters...
```

---

## 5. Quy Trình Làm Việc Dành Cho AI Agent

Mỗi khi nhận yêu cầu tạo hoặc cập nhật màn hình mới:

1. **Yêu cầu Link Figma cụ thể có `node-id`**:
   * Dạng link: `https://www.figma.com/design/.../PETCARE?node-id=X-Y`
2. **Tuân thủ hệ thống màu & Component**:
   * KHÔNG tự tiện định nghĩa lại màu Hex trùng lặp.
   * Luôn import và dùng các màu từ `com.example.petcare.ui.theme.*`.
   * Sử dụng `PetCareTextField` thay vì `OutlinedTextField` hoặc `TextField` mặc định.
3. **Thêm Compose Preview**:
   * Luôn bao bọc màn hình trong `PetCareTheme` ở hàm `@Preview`.
4. **Chia nhỏ từng task**:
   * Tạo từng màn hình một thay vì sinh hàng loạt màn hình cùng lúc.
