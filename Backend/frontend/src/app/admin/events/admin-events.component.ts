import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-events',
  templateUrl: './admin-events.component.html',
  styleUrls: ['./admin-events.component.css']
})
export class AdminEventsComponent implements OnInit {
  events: any[] = [];
  loading = true;
  showForm = false;

  // Form Fields
  title = '';
  description = '';
  category = 'Concerts';
  artistName = '';
  venueId = 1;
  eventDate = '';
  eventTime = '';
  ticketPrice = 1000;
  bannerImage = '';

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.loading = true;
    this.apiService.getAllEventsAdmin().subscribe({
      next: (data) => {
        this.events = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listing events', err);
        this.loading = false;
      }
    });
  }

  toggleEventStatus(eventId: number) {
    this.apiService.toggleEventStatus(eventId).subscribe({
      next: () => {
        this.loadEvents();
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to toggle event status.');
      }
    });
  }

  syncSeederPosters() {
    this.loading = true;
    this.apiService.reseedDatabase().subscribe({
      next: (response) => {
        alert(response || 'Seeder data synchronized successfully!');
        this.loadEvents();
      },
      error: (err) => {
        console.error('Error syncing seeder data', err);
        alert('Failed to sync seeder data: ' + (err.error || err.message));
        this.loading = false;
      }
    });
  }

  onSubmit() {
    if (!this.title || !this.eventDate || !this.eventTime) {
      alert('Please fill out event title, date, and time.');
      return;
    }

    const payload = {
      title: this.title,
      description: this.description,
      category: this.category,
      artistName: this.artistName,
      venue: { id: Number(this.venueId) },
      eventDate: this.eventDate,
      eventTime: this.eventTime,
      ticketPrice: this.ticketPrice,
      bannerImage: this.bannerImage,
      status: 'ACTIVE'
    };

    this.apiService.createEvent(payload).subscribe({
      next: () => {
        alert('Event created successfully!');
        this.showForm = false;
        this.resetForm();
        this.loadEvents();
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to create event.');
      }
    });
  }

  resetForm() {
    this.title = '';
    this.description = '';
    this.category = 'Concerts';
    this.artistName = '';
    this.venueId = 1;
    this.eventDate = '';
    this.eventTime = '';
    this.ticketPrice = 1000;
    this.bannerImage = '';
  }
}
