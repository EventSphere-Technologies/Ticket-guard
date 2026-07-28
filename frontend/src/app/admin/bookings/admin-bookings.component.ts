import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-bookings',
  templateUrl: './admin-bookings.component.html',
  styleUrls: ['./admin-bookings.component.css']
})
export class AdminBookingsComponent implements OnInit {
  bookings: any[] = [];
  loading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.loading = true;
    this.apiService.getAllBookingsAdmin().subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching system bookings', err);
        this.loading = false;
      }
    });
  }

  cancelBooking(bookingId: number) {
    const confirmCancel = confirm('Are you sure you want to cancel this booking? This will refund the payment and release the seats.');
    if (!confirmCancel) {
      return;
    }

    this.loading = true;
    this.apiService.refundBooking(bookingId, 'Admin override cancellation').subscribe({
      next: () => {
        alert('Booking cancelled successfully.');
        this.loadBookings();
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to cancel booking.');
        this.loading = false;
      }
    });
  }
}
