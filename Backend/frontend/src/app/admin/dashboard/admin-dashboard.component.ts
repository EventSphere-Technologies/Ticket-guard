import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  stats: any = null;
  fraudAlerts: any[] = [];
  loading = true;

  activePoint: any = null;
  hoveredSegment: string | null = null;
  donutCenterValue = '85%';
  donutCenterLabel = 'Success';

  confirmedPercent = 55;
  cancelledPercent = 17;
  refundedPercent = 10;
  pendingPercent = 8;
  successRate = 85;

  confirmedDashArray = '55 45';
  confirmedDashOffset = 25;
  
  cancelledDashArray = '17 83';
  cancelledDashOffset = 70;
  
  refundedDashArray = '10 90';
  refundedDashOffset = 53;

  pendingDashArray = '8 92';
  pendingDashOffset = 43;

  selectedAlert: any = null;

  constructor(private apiService: ApiService) {}

  viewAlertDetails(alert: any) {
    this.selectedAlert = alert;
  }

  closeAlertModal() {
    this.selectedAlert = null;
  }

  setDonutHover(value: string, label: string) {
    this.donutCenterValue = value;
    this.donutCenterLabel = label;
  }

  resetDonutHover() {
    this.donutCenterValue = `${this.successRate}%`;
    this.donutCenterLabel = 'Success';
  }

  calculateDonutSlices() {
    if (!this.stats) return;

    const confirmed = this.stats.confirmedBookingsCount || 0;
    const cancelled = this.stats.cancelledBookingsCount || 0;
    const refunded = this.stats.refundedBookingsCount || 0;
    const pending = this.stats.pendingBookingsCount || 0;

    const total = confirmed + cancelled + refunded + pending || 1;

    // Calculate percentages
    this.confirmedPercent = Math.round((confirmed / total) * 100);
    this.cancelledPercent = Math.round((cancelled / total) * 100);
    this.refundedPercent = Math.round((refunded / total) * 100);
    
    // To ensure they sum to exactly 100%, we make pending the remainder
    this.pendingPercent = 100 - (this.confirmedPercent + this.cancelledPercent + this.refundedPercent);

    // Calculate success rate (confirmed bookings / total active bookings)
    this.successRate = Math.round((confirmed / total) * 100);
    
    // Set default center text values
    this.donutCenterValue = `${this.successRate}%`;
    this.donutCenterLabel = 'Success';

    // Calculate SVG stroke-dasharray and stroke-dashoffset (viewBox 36 36 has circumference 100)
    // confirmed slice starts at 12 o'clock (offset 25)
    this.confirmedDashArray = `${this.confirmedPercent} ${100 - this.confirmedPercent}`;
    this.confirmedDashOffset = 25;

    // cancelled starts after confirmed: offset = 25 - confirmedPercent
    this.cancelledDashArray = `${this.cancelledPercent} ${100 - this.cancelledPercent}`;
    this.cancelledDashOffset = (25 - this.confirmedPercent + 100) % 100;

    // refunded starts after cancelled: offset = cancelledOffset - cancelledPercent
    this.refundedDashArray = `${this.refundedPercent} ${100 - this.refundedPercent}`;
    this.refundedDashOffset = (this.cancelledDashOffset - this.cancelledPercent + 100) % 100;

    // pending starts after refunded: offset = refundedOffset - refundedPercent
    this.pendingDashArray = `${this.pendingPercent} ${100 - this.pendingPercent}`;
    this.pendingDashOffset = (this.refundedDashOffset - this.refundedPercent + 100) % 100;
  }

  ngOnInit() {
    this.loadAdminData();
  }

  loadAdminData() {
    this.apiService.getAdminDashboard().subscribe({
      next: (dashboardData) => {
        this.stats = dashboardData;
        this.calculateDonutSlices();
        this.apiService.getFraudAlerts().subscribe({
          next: (alerts) => {
            this.fraudAlerts = alerts;
            this.loading = false;
          },
          error: (err) => {
            console.error('Error loading fraud logs', err);
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error loading admin stats', err);
        this.loading = false;
      }
    });
  }

  getSvgLinePath(): string {
    if (!this.stats || !this.stats.bookingOverviewCounts) {
      return '';
    }
    const counts = this.stats.bookingOverviewCounts;
    const maxVal = Math.max(...counts);
    const minVal = Math.min(...counts);
    const range = maxVal - minVal || 1;

    const points = counts.map((val: number, idx: number) => {
      const x = (idx * (400 / (counts.length - 1))) + 30;
      const y = 180 - ((val - minVal) / range) * 130;
      return `${x},${y}`;
    });
    return `M ${points.join(' L ')}`;
  }

  getSvgPoints(): any[] {
    if (!this.stats || !this.stats.bookingOverviewCounts) {
      return [];
    }
    const counts = this.stats.bookingOverviewCounts;
    const maxVal = Math.max(...counts);
    const minVal = Math.min(...counts);
    const range = maxVal - minVal || 1;

    return counts.map((val: number, idx: number) => {
      const x = (idx * (400 / (counts.length - 1))) + 30;
      const y = 180 - ((val - minVal) / range) * 130;
      return { x, y, val };
    });
  }
}
