package com.example.petclinic.service.impl;

import com.example.petclinic.service.*;

import java.time.*;
import java.util.*;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.request.AppointmentRequestDTO;
import com.example.petclinic.dto.request.AppointmentUpdateDTO;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.factory.AppointmentFactoryRegistry;
import com.example.petclinic.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public AppointmentResponseDTO requireCompletedForMedicalRecord(Long appointmentId) {
        // GlobalExceptionHandler also maps this to 400 for MedicalRecord callers.
        if (appointmentId == null || appointmentId < 1) {
            throw new IllegalArgumentException("รหัสนัดหมายต้องเป็นจำนวนเต็มบวก");
        }
        Appointment appointment = appointments.findLockedById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบนัดหมายสำหรับบันทึกประวัติการรักษา"));
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new DuplicateResourceException("บันทึกประวัติการรักษาได้เฉพาะนัดที่เสร็จสิ้นแล้ว (COMPLETED)");
        }
        return AppointmentResponseDTO.fromEntity(appointment);
    }
    private static final List<AppointmentStatus> ACTIVE = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    private static final Set<String> SORT_FIELDS = Set.of("appointmentId", "appointmentDateTime", "status", "serviceType");
    private final AppointmentRepository appointments;
    private final AppointmentPetRepository pets;
    private final AppointmentDoctorRepository doctors;
    private final AppointmentFactoryRegistry factories;
    private final AppointmentSchedulePolicy schedule;
    private final Clock clock;

    public AppointmentServiceImpl(AppointmentRepository appointments, AppointmentPetRepository pets,
            AppointmentDoctorRepository doctors, AppointmentFactoryRegistry factories,
            AppointmentSchedulePolicy schedule, Clock appointmentClock) {
        this.appointments = appointments;
        this.pets = pets;
        this.doctors = doctors;
        this.factories = factories;
        this.schedule = schedule;
        this.clock = appointmentClock;
    }

    public AppointmentResponseDTO create(AppointmentRequestDTO request) {
        requireId(request.ownerId());
        requireId(request.petId());
        // Every booking writer locks Doctor, then shared Pet, before checking availability.
        Doctor doctor = lockedDoctor(request.doctorId());
        Pet pet = pets.findLockedById(request.petId())
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสัตว์เลี้ยง"));
        if (!Objects.equals(pet.getPetOwner().getOwnerId(), request.ownerId())) {
            throw new ResourceNotFoundException("ไม่พบสัตว์เลี้ยงของเจ้าของที่ระบุ");
        }
        schedule.validate(doctor, request.appointmentDateTime());
        requireFree(doctor.getDoctorId(), pet.getPetId(), request.appointmentDateTime(), null);
        Appointment saved = appointments.saveAndFlush(factories.create(request, pet, doctor));
        return AppointmentResponseDTO.fromEntity(saved);
    }

    public AppointmentResponseDTO update(Long id, Long ownerId, AppointmentUpdateDTO request) {
        Appointment existing = lockedAppointment(id, ownerId);
        requireEditable(existing);
        if (request.version() == null || !Objects.equals(existing.getVersion(), request.version())) {
            throw new DuplicateResourceException("ข้อมูลนัดหมายเปลี่ยนแล้ว กรุณาโหลดข้อมูลล่าสุดก่อนแก้ไข");
        }
        Doctor doctor = lockedDoctor(request.doctorId());
        Pet pet = pets.findLockedById(existing.getPet().getPetId())
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสัตว์เลี้ยง"));
        schedule.validate(doctor, request.appointmentDateTime());
        requireFree(doctor.getDoctorId(), pet.getPetId(), request.appointmentDateTime(), id);
        Appointment replacement = factories.create(new AppointmentRequestDTO(ownerId, pet.getPetId(),
            doctor.getDoctorId(), request.appointmentDateTime(), request.serviceType(), request.symptoms()), pet, doctor);
        existing.setDoctor(doctor);
        existing.setAppointmentDateTime(replacement.getAppointmentDateTime());
        existing.setServiceType(replacement.getServiceType());
        existing.setSymptoms(replacement.getSymptoms());
        existing.setPreparationInstructions(replacement.getPreparationInstructions());
        // Keep the original identity, pet, owner and PENDING/CONFIRMED status.
        return AppointmentResponseDTO.fromEntity(appointments.saveAndFlush(existing));
    }

    public AppointmentResponseDTO cancel(Long id, Long ownerId) {
        Appointment existing = lockedAppointment(id, ownerId);
        if (existing.getStatus() == AppointmentStatus.CANCELLED) {
            return AppointmentResponseDTO.fromEntity(existing);
        }
        requireEditable(existing);
        existing.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentResponseDTO.fromEntity(appointments.saveAndFlush(existing));
    }

    private Appointment lockedAppointment(Long id, Long ownerId) {
        requireId(id); requireId(ownerId);
        return appointments.findLockedByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบนัดหมายของเจ้าของที่ระบุ"));
    }

    private void requireEditable(Appointment appointment) {
        if (!ACTIVE.contains(appointment.getStatus())) {
            throw new DuplicateResourceException("นัดที่เสร็จสิ้นหรือยกเลิกแล้วไม่สามารถแก้ไขได้");
        }
        schedule.requireFuture(appointment.getAppointmentDateTime());
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO get(Long id, Long ownerId) {
        requireId(id); requireId(ownerId);
        return AppointmentResponseDTO.fromEntity(appointments.findByAppointmentIdAndPetPetOwnerOwnerId(id, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบนัดหมายของเจ้าของที่ระบุ")));
    }

    @Transactional(readOnly = true)
    public AppointmentPageDTO list(Long ownerId, AppointmentStatus status, int page, int size, String sort, String direction) {
        requireId(ownerId);
        Pageable pageable = pageRequest(page, size, sort, direction);
        Page<Appointment> result = status == null ? appointments.findByPetPetOwnerOwnerId(ownerId, pageable)
            : appointments.findByPetPetOwnerOwnerIdAndStatus(ownerId, status, pageable);
        return AppointmentPageDTO.from(result.map(AppointmentResponseDTO::fromEntity));
    }

    private Pageable pageRequest(int page, int size, String sort, String direction) {
        if (page < 0 || size < 1 || size > 100 || !SORT_FIELDS.contains(sort)
                || !("asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction))) {
            throw new InvalidAppointmentException("ระบุ page >= 0, size 1-100 และ sort/direction ที่รองรับ");
        }
        Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
        if (!"appointmentId".equals(sort)) ordering = ordering.and(Sort.by("appointmentId"));
        return PageRequest.of(page, size, ordering);
    }

    @Transactional(readOnly = true)
    public List<LocalDateTime> availability(Long doctorId, LocalDate date) {
        requireId(doctorId);
        if (date == null) throw new InvalidAppointmentException("กรุณาระบุวันที่");
        Doctor doctor = doctors.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสัตวแพทย์"));
        Set<LocalDateTime> occupied = new HashSet<>();
        appointments.findByDoctorDoctorIdAndAppointmentDateTimeGreaterThanEqualAndAppointmentDateTimeLessThanAndStatusIn(
            doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), ACTIVE)
            .forEach(a -> occupied.add(a.getAppointmentDateTime()));
        return schedule.slots(doctor, date).stream().filter(time -> !occupied.contains(time)).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentPageDTO listClinic(AppointmentStatus status, int page, int size, String sort, String direction) {
        Pageable pageable = pageRequest(page, size, sort, direction);
        Page<Appointment> result = status == null ? appointments.findAll(pageable) : appointments.findByStatus(status, pageable);
        return AppointmentPageDTO.from(result.map(AppointmentResponseDTO::fromEntity));
    }

    public AppointmentResponseDTO changeStatus(Long id, com.example.petclinic.dto.request.AppointmentStatusUpdateDTO request) {
        requireId(id);
        Appointment existing = appointments.findLockedById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ไม่พบนัดหมาย"));
        if (request.version() == null || !Objects.equals(existing.getVersion(), request.version())) {
            throw new DuplicateResourceException("ข้อมูลนัดหมายเปลี่ยนแล้ว กรุณาโหลดข้อมูลล่าสุดก่อนเปลี่ยนสถานะ");
        }
        AppointmentStatus target = request.status();
        if (target != AppointmentStatus.CONFIRMED && target != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("เจ้าหน้าที่เปลี่ยนสถานะได้เฉพาะ CONFIRMED หรือ COMPLETED");
        }
        if (existing.getStatus() == target) return AppointmentResponseDTO.fromEntity(existing);
        if (target == AppointmentStatus.CONFIRMED && existing.getStatus() == AppointmentStatus.PENDING) {
            schedule.requireFuture(existing.getAppointmentDateTime());
        } else if (target == AppointmentStatus.COMPLETED && existing.getStatus() == AppointmentStatus.CONFIRMED) {
            if (existing.getAppointmentDateTime().isAfter(LocalDateTime.now(clock))) {
                throw new DuplicateResourceException("ยังไม่ถึงเวลานัดหมาย ไม่สามารถปิดนัดล่วงหน้าได้");
            }
        } else {
            throw new DuplicateResourceException("สถานะปัจจุบันไม่รองรับการเปลี่ยนสถานะนี้");
        }
        existing.setStatus(target);
        return AppointmentResponseDTO.fromEntity(appointments.saveAndFlush(existing));
    }

    private Doctor lockedDoctor(Long id) {
        requireId(id);
        return doctors.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("ไม่พบสัตวแพทย์"));
    }

    private void requireFree(Long doctorId, Long petId, LocalDateTime time, Long excludeId) {
        if (appointments.countConflicts(doctorId, petId, time, excludeId, ACTIVE) > 0) {
            throw new DuplicateResourceException("สัตวแพทย์หรือสัตว์เลี้ยงมีนัดในเวลานี้แล้ว กรุณาเลือกเวลาอื่น");
        }
    }

    private void requireId(Long id) {
        if (id == null || id < 1) throw new InvalidAppointmentException("รหัสต้องเป็นจำนวนเต็มบวก");
    }
}
