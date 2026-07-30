import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profile: any = null;
  loading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching profile', err);
        this.loading = false;
      }
    });
  }
}
