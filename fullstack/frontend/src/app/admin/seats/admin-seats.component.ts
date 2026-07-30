import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-seats',
  templateUrl: './admin-seats.component.html',
  styleUrls: ['./admin-seats.component.css']
})
export class AdminSeatsComponent implements OnInit {
  seats: any[] = [];
  filteredSeats: any[] = [];
  loading = true;
  venues: any[] = [];
  selectedVenueId = 'ALL';

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadSeats();
  }

  loadSeats() {
    this.loading = true;
    this.apiService.getAllSeatsAdmin().subscribe({
      next: (data) => {
        this.seats = data;
        this.filteredSeats = data;
        
        // Extract unique venues
        const venueMap = new Map();
        for (const seat of this.seats) {
          if (seat.venue && !venueMap.has(seat.venue.id)) {
            venueMap.set(seat.venue.id, seat.venue);
          }
        }
        this.venues = Array.from(venueMap.values());
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching seats', err);
        this.loading = false;
      }
    });
  }

  filterByVenue() {
    if (this.selectedVenueId === 'ALL') {
      this.filteredSeats = this.seats;
    } else {
      const vId = Number(this.selectedVenueId);
      this.filteredSeats = this.seats.filter(s => s.venue?.id === vId);
    }
  }

  toggleSeat(seat: any) {
    const actionStr = seat.status === 'AVAILABLE' ? 'BLOCK (Book)' : 'UNBLOCK (make Available)';
    const confirmToggle = confirm(`Are you sure you want to ${actionStr} Row ${seat.rowName} - Seat ${seat.seatNumber}?`);
    if (!confirmToggle) {
      return;
    }

    this.apiService.toggleSeatStatusAdmin(seat.id).subscribe({
      next: (updatedSeat) => {
        seat.status = updatedSeat.status;
        alert(`Seat status updated to ${updatedSeat.status}.`);
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to update seat status.');
      }
    });
  }
}
