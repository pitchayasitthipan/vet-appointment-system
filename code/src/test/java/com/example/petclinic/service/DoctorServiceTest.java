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
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petclinic.domain.entity.Doctor;
import com.example.petclinic.dto.request.DoctorRequestDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.repository.DoctorRepository;
import com.example.petclinic.service.impl.DoctorServiceImpl;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Doctor sampleDoctor;
    private DoctorRequestDTO sampleRequestDTO;

    @BeforeEach
    void setUp() {
        sampleDoctor = new Doctor();
        sampleDoctor.setDoctorId(1L);
        sampleDoctor.setFirstName("นันทิดา");
        sampleDoctor.setLastName("รักษ์สัตว์");
        sampleDoctor.setSpecialization("อายุรกรรมทั่วไป");
        sampleDoctor.setPhone("0811112233");
        sampleDoctor.setEmail("nantida.r@vetclinic.com");
        sampleDoctor.setWorkSchedule("จันทร์ - ศุกร์: 09:00 - 17:00");

        sampleRequestDTO = new DoctorRequestDTO();
        sampleRequestDTO.setFirstName("นันทิดา");
        sampleRequestDTO.setLastName("รักษ์สัตว์");
        sampleRequestDTO.setSpecialization("อายุรกรรมทั่วไป");
        sampleRequestDTO.setPhone("0811112233");
        sampleRequestDTO.setEmail("nantida.r@vetclinic.com");
        sampleRequestDTO.setWorkSchedule("จันทร์ - ศุกร์: 09:00 - 17:00");
    }

    @Nested
    @DisplayName("ทดสอบการดึงข้อมูลสัตวแพทย์ (getAllDoctors & getDoctorById)")
    class GetDoctorTests {

        @Test
        @DisplayName("getAllDoctors ควรคืนค่ารายชื่อสัตวแพทย์ทั้งหมด")
        void getAllDoctors_ShouldReturnListOfDoctors() {
            // Arrange
            Doctor doc2 = new Doctor(2L, "กิตติศักดิ์", "เจริญกิจ", "ศัลยกรรม", "0822223344", "kittisak.k@vetclinic.com", "อังคาร - เสาร์");
            when(doctorRepository.findAll()).thenReturn(Arrays.asList(sampleDoctor, doc2));

            // Act
            List<DoctorResponseDTO> result = doctorService.getAllDoctors();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFirstName()).isEqualTo("นันทิดา");
            assertThat(result.get(1).getFirstName()).isEqualTo("กิตติศักดิ์");
            verify(doctorRepository).findAll();
        }

        @Test
        @DisplayName("getDoctorById เมื่อพบข้อมูล ควรคืนค่าข้อมูลสัตวแพทย์ที่ถูกต้อง")
        void getDoctorById_WhenFound_ShouldReturnDoctor() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));

            // Act
            DoctorResponseDTO result = doctorService.getDoctorById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDoctorId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("nantida.r@vetclinic.com");
            verify(doctorRepository).findById(1L);
        }

        @Test
        @DisplayName("getDoctorById เมื่อไม่พบข้อมูล ควรโยน ResourceNotFoundException")
        void getDoctorById_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: 99");

            verify(doctorRepository).findById(99L);
        }
    }

    @Nested
    @DisplayName("ทดสอบการเพิ่มข้อมูลสัตวแพทย์ (createDoctor)")
    class CreateDoctorTests {

        @Test
        @DisplayName("createDoctor เมื่ออีเมลยังไม่ซ้ำ ควรสร้างและบันทึกข้อมูลสำเร็จ")
        void createDoctor_WhenEmailNotExists_ShouldCreateDoctor() {
            // Arrange
            when(doctorRepository.existsByEmail(sampleRequestDTO.getEmail())).thenReturn(false);
            when(doctorRepository.save(any(Doctor.class))).thenReturn(sampleDoctor);

            // Act
            DoctorResponseDTO result = doctorService.createDoctor(sampleRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDoctorId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("นันทิดา");
            verify(doctorRepository).existsByEmail(sampleRequestDTO.getEmail());
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("createDoctor เมื่ออีเมลซ้ำ ควรโยน DuplicateResourceException")
        void createDoctor_WhenEmailExists_ShouldThrowException() {
            // Arrange
            when(doctorRepository.existsByEmail(sampleRequestDTO.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.createDoctor(sampleRequestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("อีเมลนี้มีอยู่ในระบบแล้ว");

            verify(doctorRepository).existsByEmail(sampleRequestDTO.getEmail());
            verify(doctorRepository, never()).save(any(Doctor.class));
        }
    }

    @Nested
    @DisplayName("ทดสอบการแก้ไขข้อมูลสัตวแพทย์ (updateDoctor)")
    class UpdateDoctorTests {

        @Test
        @DisplayName("updateDoctor เมื่อพบ ID และอีเมลไม่ซ้ำกับผู้อื่น ควรอัปเดตข้อมูลสำเร็จ")
        void updateDoctor_WhenValid_ShouldUpdateDoctor() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
            when(doctorRepository.existsByEmailAndDoctorIdNot(sampleRequestDTO.getEmail(), 1L)).thenReturn(false);
            when(doctorRepository.save(any(Doctor.class))).thenReturn(sampleDoctor);

            // Act
            DoctorResponseDTO result = doctorService.updateDoctor(1L, sampleRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDoctorId()).isEqualTo(1L);
            verify(doctorRepository).findById(1L);
            verify(doctorRepository).existsByEmailAndDoctorIdNot(sampleRequestDTO.getEmail(), 1L);
            verify(doctorRepository).save(sampleDoctor);
        }

        @Test
        @DisplayName("updateDoctor เมื่อไม่พบ ID ควรโยน ResourceNotFoundException")
        void updateDoctor_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.updateDoctor(99L, sampleRequestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: 99");

            verify(doctorRepository).findById(99L);
            verify(doctorRepository, never()).save(any(Doctor.class));
        }

        @Test
        @DisplayName("updateDoctor เมื่ออีเมลใหม่ไปซ้ำกับสัตวแพทย์ท่านอื่น ควรโยน DuplicateResourceException")
        void updateDoctor_WhenEmailUsedByOther_ShouldThrowException() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor));
            when(doctorRepository.existsByEmailAndDoctorIdNot(sampleRequestDTO.getEmail(), 1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.updateDoctor(1L, sampleRequestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("อีเมลนี้ถูกใช้งานโดยสัตวแพทย์ท่านอื่นแล้ว");

            verify(doctorRepository).findById(1L);
            verify(doctorRepository).existsByEmailAndDoctorIdNot(sampleRequestDTO.getEmail(), 1L);
            verify(doctorRepository, never()).save(any(Doctor.class));
        }
    }

    @Nested
    @DisplayName("ทดสอบการลบข้อมูลสัตวแพทย์ (deleteDoctor)")
    class DeleteDoctorTests {

        @Test
        @DisplayName("deleteDoctor เมื่อพบ ID ควรลบข้อมูลออกจากฐานข้อมูลสำเร็จ")
        void deleteDoctor_WhenExists_ShouldDelete() {
            // Arrange
            when(doctorRepository.existsById(1L)).thenReturn(true);

            // Act
            doctorService.deleteDoctor(1L);

            // Assert
            verify(doctorRepository).existsById(1L);
            verify(doctorRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deleteDoctor เมื่อไม่พบ ID ควรโยน ResourceNotFoundException")
        void deleteDoctor_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(doctorRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: 99");

            verify(doctorRepository).existsById(99L);
            verify(doctorRepository, never()).deleteById(any());
        }
    }
}
