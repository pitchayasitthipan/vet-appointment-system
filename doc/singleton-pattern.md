# การประยุกต์ใช้ Singleton Pattern ผ่าน Spring Bean ในระบบ

## 1. ภาพรวมของ Singleton Pattern
**Singleton Pattern** จัดอยู่ในกลุ่ม **Creational Design Patterns** มีวัตถุประสงค์เพื่อ:
1. รับประกันว่าคลาสหนึ่งๆ จะมี **Instance เดียวเท่านั้น** ตลอดวงจรชีวิตการทำงานของแอปพลิเคชัน (Single Instance)
2. เป็นจุดศูนย์กลางในการเข้าถึง Instance นั้นร่วมกันทั่วทั้งระบบ (Global Access Point / Shared State)

---

## 2. ทำไมถึงเลือกใช้ Singleton ผ่าน Spring Bean แทน Classic GoF Singleton?

ในการพัฒนาแอปพลิเคชันด้วย Spring Boot การบริหารจัดการ Singleton ผ่าน **Spring IoC Container (@Service / @Scope("singleton"))** มีข้อดีเหนือกว่าการเขียน Classic Singleton (Private Constructor + `public static getInstance()`) ดังนี้:

| มิติการเปรียบเทียบ | Classic GoF Singleton | Spring Bean Managed Singleton (ที่เราเลือกใช้) |
| :--- | :--- | :--- |
| **การเชื่อมต่อ (Coupling)** | เป็น Tight Coupling เนื่องจากคลาสอื่นๆ ต้องเรียกผ่าน `ClassName.getInstance()` โดยตรง | เป็น **Loose Coupling** คลาสอื่นๆ เรียกใช้ผ่าน **Dependency Injection (DI)** |
| **ความสะดวกในการทดสอบ (Testability)** | ทดสอบยากมาก ไม่สามารถ Mocking หรือสลับ Instance ใน Unit Test ได้ | **ง่ายต่อการทดสอบ** สามารถ Mock หรือ Inject ค่าจำลองใน Unit Test ได้อย่างอิสระ |
| **การจัดการวงจรชีวิต (Lifecycle)** | ต้องเขียนโค้ดจัดการ Thread-safe และ Lazy/Eager Loading เอง | **Spring IoC จัดการให้อัตโนมัติ** ตั้งแต่สร้าง, กำหนดค่า ไปจนถึงทำลายเมื่อปิดแอปพลิเคชัน |
| **ความยืดหยุ่น (Flexibility)** | เปลี่ยน Scope ได้ยากมาก | สามารถปรับเปลี่ยน Scope ได้ง่ายเพียงแค่เปลี่ยน Annotation (เช่น `@Scope("prototype")`) |

---

## 3. การประยุกต์ใช้ในโปรเจกต์: `ClinicConfigService`

ในระบบคลินิกสัตว์เลี้ยง PawCare ข้อมูลการตั้งค่าระบบและนโยบายราคาค่าบริการควรเป็นข้อมูลชุดเดียวกันทั่วทั้งระบบ:

### ข้อมูลที่บริหารจัดการใน `ClinicConfigService`:
1. **ข้อมูลคลินิก:** ชื่อคลินิก, ที่อยู่, เบอร์โทรศัพท์, เบอร์โทรฉุกเฉิน 24 ชม.
2. **เวลาเปิดทำการ:** วันธรรมดา และวันหยุดสุดสัปดาห์
3. **นโยบายอัตราค่าบริการกลาง (Clinic Price Policy):**
   - ค่าตรวจรักษาเบื้องต้น (`baseConsultationFee`) = 300.00 บาท
   - ค่าบริการฉีดวัคซีน (`vaccineServiceFee`) = 150.00 บาท
   - ค่าบริการผ่าตัดเริ่มต้น (`surgeryServiceFee`) = 1500.00 บาท

### โค้ดตัวอย่าง:
```java
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) // Spring Bean มีค่าเริ่มต้นเป็น Singleton อยู่แล้ว
public class ClinicConfigService {

    private String clinicName = "PawCare Pet Clinic & Vaccination";
    private BigDecimal baseConsultationFee = new BigDecimal("300.00");
    // ...
    
    // Thread-safe method
    public synchronized ClinicConfigResponseDTO getConfigDTO() {
        return new ClinicConfigResponseDTO(...);
    }
}
```

---

## 4. การทดสอบและพิสูจน์ความเป็น Singleton

1. **ผ่าน REST API:**
   - เรียก Endpoint: `GET /api/v1/config/singleton-check`
   - จะแสดงผล `instanceIdentityHashCode` ที่คงเดิมเสมอไม่ว่าจะเรียกกี่ครั้ง ยืนยันว่าเป็น Object เดียวกันในหน่วยความจำ

2. **ผ่าน Unit Test (`ClinicConfigServiceTest`):**
   - ทดสอบฉีด (Inject) `ClinicConfigService` เข้ามา 2 ตัวแปร
   - ตรวจสอบ `assertThat(instance1).isSameAs(instance2);` ผลลัพธ์คือ **ผ่าน (Pass)**
   - ทดสอบอัปเดตราคาผ่าน `instance1` แล้วตรวจสอบผ่าน `instance2` พบว่ามองเห็นข้อมูลชุดเดียวกันทันที

### ภาพหลักฐานผลการทดสอบ Singleton Pattern:
![Singleton Pattern Test Result](img/singleton-test-passed.png)

