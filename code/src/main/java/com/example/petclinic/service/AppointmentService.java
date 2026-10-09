package com.example.petclinic.service;

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

@Service
@Transactional
public class AppointmentService {
    private static final List<AppointmentStatus> ACTIVE = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    private static final Set<String> SORT_FIELDS = Set.of("appointmentId", "appointmentDateTime", "status", "serviceType");
    private final AppointmentRepository appointments;
    private final PetRepository pets;
    private final AppointmentDoctorRepository doctors;
    private final AppointmentFactoryRegistry factories;
    private final AppointmentSchedulePolicy schedule;

    public AppointmentService(AppointmentRepository appointments, PetRepository pets,
            AppointmentDoctorRepository doctors, AppointmentFactoryRegistry factories,
            AppointmentSchedulePolicy schedule) {
        this.appointments = appointments;
        this.pets = pets;
        this.doctors = doctors;
        this.factories = factories;
        this.schedule = schedule;
    }

    public AppointmentResponseDTO create(AppointmentRequestDTO request) {
        requireId(request.ownerId());
        requireId(request.petId());
        // Every booking writer locks Doctor, then Pet, before checking availability.
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
        if (page < 0 || size < 1 || size > 100 || !SORT_FIELDS.contains(sort)
                || !("asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction))) {
            throw new InvalidAppointmentException("ระบุ page >= 0, size 1-100 และ sort/direction ที่รองรับ");
        }
        Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
        if (!"appointmentId".equals(sort)) ordering = ordering.and(Sort.by("appointmentId"));
        Pageable pageable = PageRequest.of(page, size, ordering);
        Page<Appointment> result = status == null ? appointments.findByPetPetOwnerOwnerId(ownerId, pageable)
            : appointments.findByPetPetOwnerOwnerIdAndStatus(ownerId, status, pageable);
        return AppointmentPageDTO.from(result.map(AppointmentResponseDTO::fromEntity));
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
