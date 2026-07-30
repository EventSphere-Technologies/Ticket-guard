import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  events: any[] = [];
  filteredEvents: any[] = [];
  categories: string[] = ['All', 'Concerts', 'Sports', 'Theatre', 'Comedy'];
  selectedCategory = 'All';
  searchQuery = '';
  loading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.filteredEvents = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching events', err);
        this.loading = false;
      }
    });
  }

  filterCategory(category: string) {
    this.selectedCategory = category;
    this.applyFilters();
  }

  onSearch() {
    this.applyFilters();
  }

  applyFilters() {
    this.filteredEvents = this.events.filter(event => {
      const matchCategory = this.selectedCategory === 'All' || event.category === this.selectedCategory;
      const matchSearch = event.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
                          (event.artistName && event.artistName.toLowerCase().includes(this.searchQuery.toLowerCase())) ||
                          (event.venue && event.venue.venueName.toLowerCase().includes(this.searchQuery.toLowerCase()));
      return matchCategory && matchSearch;
    });
  }

  onImgError(event: any) {
    event.target.src = 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?q=80&w=600';
  }
}
