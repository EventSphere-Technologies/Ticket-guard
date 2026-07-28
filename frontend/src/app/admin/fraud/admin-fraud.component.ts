import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-fraud',
  templateUrl: './admin-fraud.component.html',
  styleUrls: ['./admin-fraud.component.css']
})
export class AdminFraudComponent implements OnInit {
  alerts: any[] = [];
  loading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadAlerts();
  }

  loadAlerts() {
    this.loading = true;
    this.apiService.getFraudAlerts().subscribe({
      next: (data) => {
        this.alerts = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching fraud alerts', err);
        this.loading = false;
      }
    });
  }
}
