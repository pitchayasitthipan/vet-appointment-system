package com.example.petclinic.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petclinic.domain.entity.MedicalRecord;
import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.repository.MedicalRecordRepository;
import com.example.petclinic.service.impl.MedicalRecordServiceImpl;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    // จำลอง Service สำหรับตรวจสถานะนัดหมาย
    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private MedicalRecord sampleMedicalRecord;
    private MedicalRecordRequestDTO sampleRequestDTO;

    @BeforeEach
    void setUp() {

        // ===== เตรียมข้อมูลตัวอย่าง MedicalRecord =====
        sampleMedicalRecord = new MedicalRecord();
        sampleMedicalRecord.setMedicalRecordId(1L);
        sampleMedicalRecord.setAppointmentId(10L);
        sampleMedicalRecord.setDiagnosis("มีอาการไข้เล็กน้อย");
        sampleMedicalRecord.setTreatment("ให้ยาและพักผ่อน");
        sampleMedicalRecord.setVaccineName(null);
        sampleMedicalRecord.setVaccineDate(null);
        sampleMedicalRecord.setNextVaccineDate(null);
        sampleMedicalRecord.setNotes("นัดติดตามอาการ");

        // ===== เตรียมข้อมูล Request DTO ตัวอย่าง =====
        sampleRequestDTO = new MedicalRecordRequestDTO();
        sampleRequestDTO.setAppointmentId(10L);
        sampleRequestDTO.setDiagnosis("มีอาการไข้เล็กน้อย");
        sampleRequestDTO.setTreatment("ให้ยาและพักผ่อน");
        sampleRequestDTO.setVaccineName(null);
        sampleRequestDTO.setVaccineDate(null);
        sampleRequestDTO.setNextVaccineDate(null);
        sampleRequestDTO.setNotes("นัดติดตามอาการ");
    }

        // ===== ส่วนทดสอบการค้นหาข้อมูล MedicalRecord =====
    @Nested
    @DisplayName("ทดสอบการดึงข้อมูลประวัติการรักษา")
    class GetMedicalRecordTests {

        @Test
        @DisplayName("getAllMedicalRecords ควรคืนค่าประวัติการรักษาทั้งหมด")
        void getAllMedicalRecords_ShouldReturnListOfMedicalRecords() {

            // Arrange: จำลองข้อมูลที่ Repository จะคืนกลับมา
            MedicalRecord record2 = new MedicalRecord();
            record2.setMedicalRecordId(2L);
            record2.setAppointmentId(11L);
            record2.setDiagnosis("ปกติ");
            record2.setTreatment("ตรวจสุขภาพทั่วไป");
            record2.setNotes("ไม่มีอาการผิดปกติ");

            when(medicalRecordRepository.findAll())
                    .thenReturn(Arrays.asList(sampleMedicalRecord, record2));

            // Act: เรียก Service
            List<MedicalRecordResponseDTO> result =
                    medicalRecordService.getAllMedicalRecords();

            // Assert: ตรวจสอบผลลัพธ์
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getMedicalRecordId()).isEqualTo(1L);
            assertThat(result.get(1).getMedicalRecordId()).isEqualTo(2L);

            verify(medicalRecordRepository).findAll();
        }

        @Test
        @DisplayName("getMedicalRecordById เมื่อพบข้อมูล ควรคืนค่าประวัติการรักษาที่ถูกต้อง")
        void getMedicalRecordById_WhenFound_ShouldReturnMedicalRecord() {

            // Arrange
            when(medicalRecordRepository.findById(1L))
                    .thenReturn(Optional.of(sampleMedicalRecord));

            // Act
            MedicalRecordResponseDTO result =
                    medicalRecordService.getMedicalRecordById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMedicalRecordId()).isEqualTo(1L);
            assertThat(result.getAppointmentId()).isEqualTo(10L);
            assertThat(result.getDiagnosis())
                    .isEqualTo("มีอาการไข้เล็กน้อย");

            verify(medicalRecordRepository).findById(1L);
        }

        @Test
        @DisplayName("getMedicalRecordById เมื่อไม่พบข้อมูล ควรโยน ResourceNotFoundException")
        void getMedicalRecordById_WhenNotFound_ShouldThrowException() {

            // Arrange
            when(medicalRecordRepository.findById(99L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(
                    () -> medicalRecordService.getMedicalRecordById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(
                            "ไม่พบประวัติการรักษาที่มีรหัส: 99");

            verify(medicalRecordRepository).findById(99L);
        }

        @Test
        @DisplayName("getMedicalRecordsByAppointmentId ควรคืนค่าประวัติการรักษาของนัดหมายที่กำหนด")
        void getMedicalRecordsByAppointmentId_ShouldReturnMedicalRecords() {

            // Arrange
            MedicalRecord record2 = new MedicalRecord();
            record2.setMedicalRecordId(2L);
            record2.setAppointmentId(10L);
            record2.setDiagnosis("อาการดีขึ้น");
            record2.setTreatment("ติดตามอาการต่อ");

            when(medicalRecordRepository
                    .findByAppointmentIdOrderByCreatedAtDesc(10L))
                    .thenReturn(Arrays.asList(record2, sampleMedicalRecord));

            // Act
            List<MedicalRecordResponseDTO> result =
                    medicalRecordService.getMedicalRecordsByAppointmentId(10L);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAppointmentId()).isEqualTo(10L);
            assertThat(result.get(1).getAppointmentId()).isEqualTo(10L);

            verify(medicalRecordRepository)
                    .findByAppointmentIdOrderByCreatedAtDesc(10L);
        }
    }

        // ===== ส่วนทดสอบการเพิ่ม MedicalRecord =====
    @Nested
    @DisplayName("ทดสอบการเพิ่มประวัติการรักษา (createMedicalRecord)")
    class CreateMedicalRecordTests {

        @Test
        @DisplayName("createMedicalRecord ควรสร้างและบันทึกประวัติการรักษาสำเร็จ")
        void createMedicalRecord_ShouldCreateMedicalRecord() {

            // Arrange: จำลองการบันทึกข้อมูลสำเร็จ
            when(medicalRecordRepository.save(any(MedicalRecord.class)))
                    .thenReturn(sampleMedicalRecord);

            // Act: เรียก Service สำหรับเพิ่มข้อมูล
            MedicalRecordResponseDTO result =
                    medicalRecordService.createMedicalRecord(sampleRequestDTO);

            // Assert: ตรวจสอบผลลัพธ์ที่ได้
            assertThat(result).isNotNull();
            assertThat(result.getMedicalRecordId()).isEqualTo(1L);
            assertThat(result.getAppointmentId()).isEqualTo(10L);
            assertThat(result.getDiagnosis())
                    .isEqualTo("มีอาการไข้เล็กน้อย");

            verify(medicalRecordRepository)
                    .save(any(MedicalRecord.class));
        }

        @Test
        @DisplayName("createMedicalRecord ควรสร้างข้อมูลครบทุกฟิลด์ด้วย Builder")
        void createMedicalRecord_ShouldBuildCorrectFields() {

            // Arrange: จำลอง Repository ให้คืนค่าข้อมูลที่ได้รับ
            when(medicalRecordRepository.save(any(MedicalRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act: เรียก Service เพื่อสร้าง MedicalRecord
            medicalRecordService.createMedicalRecord(sampleRequestDTO);

            // Assert: จับ Object ที่ส่งไปบันทึกมาตรวจสอบ
            ArgumentCaptor<MedicalRecord> captor =
                    ArgumentCaptor.forClass(MedicalRecord.class);

            verify(medicalRecordRepository).save(captor.capture());

            MedicalRecord savedRecord = captor.getValue();

            // ตรวจสอบว่า Builder กำหนดค่าตรงกับ Request DTO
            assertThat(savedRecord.getAppointmentId())
                    .isEqualTo(sampleRequestDTO.getAppointmentId());
            assertThat(savedRecord.getDiagnosis())
                    .isEqualTo(sampleRequestDTO.getDiagnosis());
            assertThat(savedRecord.getTreatment())
                    .isEqualTo(sampleRequestDTO.getTreatment());
            assertThat(savedRecord.getVaccineName())
                    .isEqualTo(sampleRequestDTO.getVaccineName());
            assertThat(savedRecord.getVaccineDate())
                    .isEqualTo(sampleRequestDTO.getVaccineDate());
            assertThat(savedRecord.getNextVaccineDate())
                    .isEqualTo(sampleRequestDTO.getNextVaccineDate());
            assertThat(savedRecord.getNotes())
                    .isEqualTo(sampleRequestDTO.getNotes());
        }
    }

    // ===== ส่วนทดสอบการแก้ไข MedicalRecord =====
    @Nested
    @DisplayName("ทดสอบการแก้ไขประวัติการรักษา (updateMedicalRecord)")
    class UpdateMedicalRecordTests {

        @Test
        @DisplayName("updateMedicalRecord เมื่อพบ ID ควรอัปเดตข้อมูลสำเร็จ")
        void updateMedicalRecord_WhenFound_ShouldUpdateMedicalRecord() {

            // Arrange: จำลองว่าพบข้อมูลเดิมในฐานข้อมูล
            when(medicalRecordRepository.findById(1L))
                    .thenReturn(Optional.of(sampleMedicalRecord));

            // จำลองการบันทึกข้อมูลที่แก้ไขแล้ว
            when(medicalRecordRepository.save(any(MedicalRecord.class)))
                    .thenReturn(sampleMedicalRecord);

            // เปลี่ยนข้อมูลบางส่วนเพื่อทดสอบการแก้ไข
            sampleRequestDTO.setDiagnosis("อาการดีขึ้น");
            sampleRequestDTO.setTreatment("ติดตามอาการต่อ");
            sampleRequestDTO.setNotes("อาการตอบสนองต่อการรักษาดี");

            // Act: เรียก Service สำหรับแก้ไขข้อมูล
            MedicalRecordResponseDTO result =
                    medicalRecordService.updateMedicalRecord(
                            1L,
                            sampleRequestDTO);

            // Assert: ตรวจสอบว่าข้อมูลถูกแก้ไขจริง
            assertThat(result).isNotNull();
            assertThat(result.getMedicalRecordId()).isEqualTo(1L);
            assertThat(sampleMedicalRecord.getDiagnosis())
                    .isEqualTo("อาการดีขึ้น");
            assertThat(sampleMedicalRecord.getTreatment())
                    .isEqualTo("ติดตามอาการต่อ");
            assertThat(sampleMedicalRecord.getNotes())
                    .isEqualTo("อาการตอบสนองต่อการรักษาดี");

            verify(medicalRecordRepository).findById(1L);
            verify(medicalRecordRepository).save(sampleMedicalRecord);
        }

        @Test
        @DisplayName("updateMedicalRecord เมื่อไม่พบ ID ควรโยน ResourceNotFoundException")
        void updateMedicalRecord_WhenNotFound_ShouldThrowException() {

            // Arrange: จำลองกรณีไม่พบข้อมูล
            when(medicalRecordRepository.findById(99L))
                    .thenReturn(Optional.empty());

            // Act & Assert: ต้องโยน Exception เมื่อไม่พบข้อมูล
            assertThatThrownBy(
                    () -> medicalRecordService.updateMedicalRecord(
                            99L,
                            sampleRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(
                            "ไม่พบประวัติการรักษาที่มีรหัส: 99");

            verify(medicalRecordRepository).findById(99L);

            // ไม่ควรบันทึกข้อมูลถ้าหา record ไม่เจอ
            verify(medicalRecordRepository, never())
                    .save(any(MedicalRecord.class));
        }
    }

        // ===== ทดสอบการเชื่อม Appointment กับ MedicalRecord =====
        @Nested
        @DisplayName("ทดสอบการตรวจนัดหมายก่อนบันทึกประวัติ")
        class AppointmentValidationTests {

            @Test
            @DisplayName("สร้างประวัติได้เมื่อ Appointment ผ่านการตรวจสอบ")
            void create_WhenAppointmentCompleted_ShouldSave() {

                // จำลองว่านัดหมายผ่านการตรวจสอบแล้ว
                when(medicalRecordRepository.save(any(MedicalRecord.class)))
                        .thenReturn(sampleMedicalRecord);

                medicalRecordService.createMedicalRecord(sampleRequestDTO);

                // ต้องตรวจนัดหมายก่อนบันทึก
                verify(appointmentService)
                        .requireCompletedForMedicalRecord(10L);
                verify(medicalRecordRepository)
                        .save(any(MedicalRecord.class));
            }

            @Test
            @DisplayName("ไม่สร้างประวัติเมื่อนัดหมายไม่มีอยู่จริง")
            void create_WhenAppointmentNotFound_ShouldNotSave() {

                // จำลองกรณีไม่พบนัดหมาย
                when(appointmentService.requireCompletedForMedicalRecord(10L))
                        .thenThrow(new ResourceNotFoundException("ไม่พบนัดหมาย"));

                assertThatThrownBy(
                        () -> medicalRecordService.createMedicalRecord(sampleRequestDTO))
                        .isInstanceOf(ResourceNotFoundException.class);

                // ต้องไม่บันทึกข้อมูลเมื่อการตรวจสอบไม่ผ่าน
                verify(medicalRecordRepository, never())
                        .save(any(MedicalRecord.class));
            }

            @Test
            @DisplayName("ไม่สร้างประวัติเมื่อนัดหมายยังไม่ COMPLETED")
            void create_WhenAppointmentNotCompleted_ShouldNotSave() {

                // จำลองกรณีนัดหมายยังไม่เสร็จสิ้น
                when(appointmentService.requireCompletedForMedicalRecord(10L))
                        .thenThrow(new DuplicateResourceException("นัดหมายยังไม่เสร็จสิ้น"));

                assertThatThrownBy(
                        () -> medicalRecordService.createMedicalRecord(sampleRequestDTO))
                        .isInstanceOf(DuplicateResourceException.class);

                verify(medicalRecordRepository, never())
                        .save(any(MedicalRecord.class));
            }

            @Test
            @DisplayName("แก้ไขประวัติได้เมื่อ Appointment ผ่านการตรวจสอบ")
            void update_WhenAppointmentCompleted_ShouldSave() {

                when(medicalRecordRepository.findById(1L))
                        .thenReturn(Optional.of(sampleMedicalRecord));
                when(medicalRecordRepository.save(any(MedicalRecord.class)))
                        .thenReturn(sampleMedicalRecord);

                medicalRecordService.updateMedicalRecord(1L, sampleRequestDTO);

                // ต้องตรวจสถานะนัดหมายก่อนแก้ไข
                verify(appointmentService)
                        .requireCompletedForMedicalRecord(10L);
                verify(medicalRecordRepository)
                        .save(any(MedicalRecord.class));
            }

            @Test
            @DisplayName("ไม่แก้ไขประวัติเมื่อนัดหมายยังไม่ COMPLETED")
            void update_WhenAppointmentNotCompleted_ShouldNotSave() {

                when(medicalRecordRepository.findById(1L))
                        .thenReturn(Optional.of(sampleMedicalRecord));

                when(appointmentService.requireCompletedForMedicalRecord(10L))
                        .thenThrow(new IllegalStateException("นัดหมายยังไม่เสร็จสิ้น"));

                assertThatThrownBy(
                        () -> medicalRecordService.updateMedicalRecord(1L, sampleRequestDTO))
                        .isInstanceOf(IllegalStateException.class);

                verify(medicalRecordRepository, never())
                        .save(any(MedicalRecord.class));
            }
        }

        // ===== ส่วนทดสอบการลบ MedicalRecord =====
    @Nested
    @DisplayName("ทดสอบการลบประวัติการรักษา (deleteMedicalRecord)")
    class DeleteMedicalRecordTests {

        @Test
        @DisplayName("deleteMedicalRecord เมื่อพบ ID ควรลบข้อมูลสำเร็จ")
        void deleteMedicalRecord_WhenExists_ShouldDelete() {

            // Arrange: จำลองว่าพบข้อมูลที่ต้องการลบ
            when(medicalRecordRepository.existsById(1L))
                    .thenReturn(true);

            // Act: เรียก Service สำหรับลบข้อมูล
            medicalRecordService.deleteMedicalRecord(1L);

            // Assert: ตรวจสอบว่ามีการตรวจสอบ ID และลบข้อมูล
            verify(medicalRecordRepository).existsById(1L);
            verify(medicalRecordRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deleteMedicalRecord เมื่อไม่พบ ID ควรโยน ResourceNotFoundException")
        void deleteMedicalRecord_WhenNotFound_ShouldThrowException() {

            // Arrange: จำลองว่าไม่พบข้อมูล
            when(medicalRecordRepository.existsById(99L))
                    .thenReturn(false);

            // Act & Assert: ต้องโยน Exception เมื่อไม่พบข้อมูล
            assertThatThrownBy(
                    () -> medicalRecordService.deleteMedicalRecord(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(
                            "ไม่พบประวัติการรักษาที่มีรหัส: 99");

            verify(medicalRecordRepository).existsById(99L);

            // ไม่ควรลบข้อมูลถ้าไม่พบ record
            verify(medicalRecordRepository, never())
                    .deleteById(any());
        }
    }
}

    