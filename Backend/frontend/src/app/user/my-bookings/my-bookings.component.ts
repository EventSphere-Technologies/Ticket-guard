import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-my-bookings',
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css']
})
export class MyBookingsComponent implements OnInit {
  bookings: any[] = [];
  filteredBookings: any[] = [];
  selectedTab = 'UPCOMING';
  loading = true;

  selectedBooking: any = null;
  selectedBookingEvent: any = null;
  showTicketModal = false;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.loading = true;
    this.apiService.getMyBookings().subscribe({
      next: (data) => {
        this.bookings = data;
        this.filterBookings();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching user bookings', err);
        this.loading = false;
      }
    });
  }

  selectTab(tab: string) {
    this.selectedTab = tab;
    this.filterBookings();
  }

  filterBookings() {
    this.filteredBookings = this.bookings.filter(booking => {
      const status = booking.bookingStatus.toUpperCase();
      if (this.selectedTab === 'UPCOMING') {
        return status === 'CONFIRMED' || status === 'PENDING';
      } else if (this.selectedTab === 'CANCELLED') {
        return status === 'CANCELLED';
      } else {
        // Mocking COMPLETED (Confirmed bookings can show here as well)
        return false;
      }
    });
  }

  cancelBooking(bookingId: number) {
    const confirmCancel = confirm('Are you sure you want to cancel this booking? A refund will be issued to your payment method.');
    if (!confirmCancel) {
      return;
    }

    this.loading = true;
    this.apiService.refundBooking(bookingId, 'User cancellation').subscribe({
      next: () => {
        alert('Booking cancelled successfully. Refund processed.');
        this.loadBookings();
      },
      error: (err) => {
        alert(err.error?.message || 'Cancellation failed.');
        this.loading = false;
      }
    });
  }

  openTicketModal(booking: any, event: Event) {
    event.preventDefault();
    this.selectedBooking = booking;
    this.showTicketModal = true;
    this.selectedBookingEvent = null; // reset

    // Load full event to get the banner image/poster
    if (booking.eventId) {
      this.apiService.getEventById(booking.eventId).subscribe({
        next: (eventData) => {
          this.selectedBookingEvent = eventData;
        },
        error: (err) => {
          console.error('Error loading event details for ticket', err);
        }
      });
    }
  }

  closeTicketModal() {
    this.showTicketModal = false;
    this.selectedBooking = null;
    this.selectedBookingEvent = null;
  }

  getTicketNumber(booking: any): string {
    if (!booking || !booking.qrCode) return 'TKT-PENDING';
    const parts = booking.qrCode.split('/');
    return parts[parts.length - 1] || 'TKT-UNKNOWN';
  }

  printTicket() {
    window.print();
  }

  onImgError(event: any) {
    event.target.src = 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?q=80&w=800';
  }
}
