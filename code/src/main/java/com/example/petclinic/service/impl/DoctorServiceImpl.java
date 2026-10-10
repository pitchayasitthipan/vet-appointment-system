package com.example.petclinic.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petclinic.domain.entity.Doctor;
import com.example.petclinic.dto.request.DoctorRequestDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.repository.DoctorRepository;
import com.example.petclinic.service.DoctorService;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(DoctorResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: " + id));
        return DoctorResponseDTO.fromEntity(doctor);
    }

    @Override
    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        if (doctorRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("อีเมลนี้มีอยู่ในระบบแล้ว: " + requestDTO.getEmail());
        }

        Doctor doctor = new Doctor();
        doctor.setFirstName(requestDTO.getFirstName());
        doctor.setLastName(requestDTO.getLastName());
        doctor.setSpecialization(requestDTO.getSpecialization());
        doctor.setPhone(requestDTO.getPhone());
        doctor.setEmail(requestDTO.getEmail());
        doctor.setWorkSchedule(requestDTO.getWorkSchedule());

        Doctor savedDoctor = doctorRepository.save(doctor);
        return DoctorResponseDTO.fromEntity(savedDoctor);
    }

    @Override
    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO requestDTO) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: " + id));

        if (doctorRepository.existsByEmailAndDoctorIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateResourceException("อีเมลนี้ถูกใช้งานโดยสัตวแพทย์ท่านอื่นแล้ว: " + requestDTO.getEmail());
        }

        doctor.setFirstName(requestDTO.getFirstName());
        doctor.setLastName(requestDTO.getLastName());
        doctor.setSpecialization(requestDTO.getSpecialization());
        doctor.setPhone(requestDTO.getPhone());
        doctor.setEmail(requestDTO.getEmail());
        doctor.setWorkSchedule(requestDTO.getWorkSchedule());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return DoctorResponseDTO.fromEntity(updatedDoctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: " + id);
        }
        try {
            doctorRepository.deleteById(id);
            // flush = ส่งคำสั่งลบไปฐานข้อมูลทันที ถ้ายังมีนัดหมายอ้างถึง (FK doctor_id) จะรู้ตรงนี้เลย
            doctorRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // ยังมีนัดหมายผูกกับสัตวแพทย์คนนี้ -> ไม่ให้ลบ ตอบ 409 พร้อมข้อความที่อ่านเข้าใจ
            throw new DuplicateResourceException(
                    "ไม่สามารถลบสัตวแพทย์ที่ยังมีนัดหมายในระบบได้ กรุณาย้ายหรือยกเลิกนัดหมายก่อน");
        }
    }
}
