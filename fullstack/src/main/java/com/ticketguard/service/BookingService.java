package com.ticketguard.service;

import com.ticketguard.dto.BookingResponse;
import com.ticketguard.dto.SeatReserveRequest;
import com.ticketguard.entity.*;
import com.ticketguard.exception.BadRequestException;
import com.ticketguard.exception.ResourceNotFoundException;
import com.ticketguard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private static final String SEAT_PREFIX = "Seat ";

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private SeatReservationRepository seatReservationRepository;

    @Autowired
    private SeatLayoutRepository seatLayoutRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public BookingResponse reserveSeats(SeatReserveRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Limit check: maximum 8 tickets per user per event
        List<Booking> userBookings = bookingRepository.findByUserId(user.getId());
        long currentTicketsCount = 0;
        for (Booking b : userBookings) {
            if (b.getBookingStatus() != Booking.BookingStatus.CANCELLED && b.getEvent().getId().equals(event.getId())) {
                currentTicketsCount += bookingSeatRepository.findByBookingId(b.getId()).size();
            }
        }
        if (currentTicketsCount + request.getSeatIds().size() > 8) {
            throw new BadRequestException("Purchase limit exceeded. A single user can buy a maximum of 8 tickets for this event.");
        }

        List<SeatLayout> seats = seatLayoutRepository.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new BadRequestException("One or more selected seats do not exist");
        }

        LocalDateTime now = LocalDateTime.now();

        // Validate seats availability (check actual bookings or active reservations)
        for (SeatLayout seat : seats) {
            if (!seat.getVenue().getId().equals(event.getVenue().getId())) {
                throw new BadRequestException(
                        SEAT_PREFIX + seat.getRowName() + seat.getSeatNumber() + " is not in this event's venue");
            }
            if (seat.getStatus() == SeatLayout.SeatStatus.BOOKED) {
                throw new BadRequestException(
                        SEAT_PREFIX + seat.getRowName() + seat.getSeatNumber() + " is already booked");
            }
            boolean isLocked = seatReservationRepository.existsByEventIdAndSeatIdAndStatus(
                    event.getId(), seat.getId(), SeatReservation.ReservationStatus.LOCKED);
            if (isLocked) {
                throw new BadRequestException(SEAT_PREFIX + seat.getRowName() + seat.getSeatNumber()
                        + " is currently locked by another customer");
            }
        }

        // Lock seats for 5 minutes
        LocalDateTime expiry = now.plusMinutes(5);
        List<SeatReservation> reservations = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SeatLayout seat : seats) {
            SeatReservation reservation = SeatReservation.builder()
                    .user(user)
                    .event(event)
                    .seat(seat)
                    .reservationStart(now)
                    .reservationExpiry(expiry)
                    .status(SeatReservation.ReservationStatus.LOCKED)
                    .build();
            reservations.add(reservation);
            totalAmount = totalAmount.add(seat.getPrice());
        }
        seatReservationRepository.saveAll(reservations);

        // Generate Booking
        String bookingNum = "TG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = Booking.builder()
                .bookingNumber(bookingNum)
                .user(user)
                .event(event)
                .totalAmount(totalAmount)
                .bookingStatus(Booking.BookingStatus.PENDING)
                .paymentStatus(Booking.PaymentStatus.PENDING)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        // Link Seats to Booking
        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (SeatLayout seat : seats) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(savedBooking)
                    .seat(seat)
                    .seatPrice(seat.getPrice())
                    .build();
            bookingSeats.add(bookingSeat);
        }
        bookingSeatRepository.saveAll(bookingSeats);

        return mapToBookingResponse(savedBooking, reservations, seats);
    }

    public List<BookingResponse> getUserBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        List<BookingResponse> responses = new ArrayList<>();

        for (Booking booking : bookings) {
            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
            List<SeatLayout> seats = bookingSeats.stream().map(BookingSeat::getSeat).toList();
            responses.add(mapToBookingResponse(booking, null, seats));
        }
        return responses;
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingResponse> responses = new ArrayList<>();
        for (Booking booking : bookings) {
            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
            List<SeatLayout> seats = bookingSeats.stream().map(BookingSeat::getSeat).toList();
            responses.add(mapToBookingResponse(booking, null, seats));
        }
        return responses;
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    @Transactional
    public void confirmBookingPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bookingSeat : bookingSeats) {
            // Update Seat status in Venue to booked
            SeatLayout seat = bookingSeat.getSeat();
            seat.setStatus(SeatLayout.SeatStatus.BOOKED);
            seatLayoutRepository.save(seat);

            // Update Reservation to CONFIRMED
            seatReservationRepository.findByUserIdAndEventIdAndSeatIdAndStatus(
                    booking.getUser().getId(), booking.getEvent().getId(), seat.getId(),
                    SeatReservation.ReservationStatus.LOCKED).ifPresent(res -> {
                        res.setStatus(SeatReservation.ReservationStatus.CONFIRMED);
                        seatReservationRepository.save(res);
                    });
        }
    }

    // Cron job running every 60 seconds to release expired locks
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatReservation> expired = seatReservationRepository.findByStatusAndReservationExpiryBefore(
                SeatReservation.ReservationStatus.LOCKED, now);

        if (expired.isEmpty()) {
            return;
        }

        for (SeatReservation reservation : expired) {
            reservation.setStatus(SeatReservation.ReservationStatus.EXPIRED);
            seatReservationRepository.save(reservation);

            // Check pending bookings containing this seat
            // Normally, you look up booking seats matching the seat and event.
            // If the booking is PENDING, mark it as CANCELLED.
            // We can search through the active booking seats
            List<BookingSeat> bookingSeats = bookingSeatRepository.findAll().stream()
                    .filter(bs -> bs.getSeat().getId().equals(reservation.getSeat().getId())
                            && bs.getBooking().getEvent().getId().equals(reservation.getEvent().getId()))
                    .toList();

            for (BookingSeat bs : bookingSeats) {
                Booking booking = bs.getBooking();
                if (booking.getBookingStatus() == Booking.BookingStatus.PENDING) {
                    booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                }
            }
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking, List<SeatReservation> reservations,
            List<SeatLayout> seats) {
        LocalDateTime expiry = null;
        if (reservations != null && !reservations.isEmpty()) {
            expiry = reservations.get(0).getReservationExpiry();
        }

        List<String> seatNames = seats.stream()
                .map(s -> s.getRowName() + s.getSeatNumber())
                .toList();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .venueName(booking.getEvent().getVenue().getVenueName())
                .eventDate(booking.getEvent().getEventDate().format(dateFormatter))
                .eventTime(booking.getEvent().getEventTime().format(timeFormatter))
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus().name())
                .paymentStatus(booking.getPaymentStatus().name())
                .seatNames(seatNames)
                .reservationExpiry(expiry)
                .qrCode(booking.getQrCode())
                .build();
    }

    @Transactional
    public void releaseSeatsForCancelledBooking(Long bookingId) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bs : bookingSeats) {
            seatReservationRepository.findByUserIdAndEventIdAndSeatIdAndStatus(
                    bs.getBooking().getUser().getId(),
                    bs.getBooking().getEvent().getId(),
                    bs.getSeat().getId(),
                    SeatReservation.ReservationStatus.LOCKED
            ).ifPresent(res -> {
                res.setStatus(SeatReservation.ReservationStatus.EXPIRED);
                seatReservationRepository.save(res);
            });
        }
    }
}
