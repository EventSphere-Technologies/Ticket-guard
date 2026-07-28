import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.css']
})
export class EventDetailsComponent implements OnInit {
  event: any = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.apiService.getEventById(id).subscribe({
      next: (data) => {
        this.event = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching event details', err);
        this.loading = false;
      }
    });
  }

  bookNow() {
    if (this.event) {
      this.router.navigate(['/seats', this.event.id]);
    }
  }

  onImgError(event: any) {
    event.target.src = 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?q=80&w=1200';
  }
}
