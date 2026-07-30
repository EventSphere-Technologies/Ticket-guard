import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-reports',
  templateUrl: './admin-reports.component.html',
  styleUrls: ['./admin-reports.component.css']
})
export class AdminReportsComponent implements OnInit {
  reports: any[] = [];
  loading = true;

  // New report generation
  reportName = '';
  reportType = 'REVENUE'; // or 'AUDIT', 'FRAUD'

  error = '';
  success = '';

  selectedReport: any = null;
  reportDetails: any = null;

  constructor(private apiService: ApiService) {}

  viewReport(report: any) {
    this.selectedReport = report;
    this.reportDetails = null;

    // Simulate/Generate reports details based on dimension
    if (report.reportType === 'REVENUE') {
      this.reportDetails = {
        title: report.reportName,
        type: 'Revenue Analysis',
        summary: [
          { label: 'Total Revenue Generated', value: '₹22,998' },
          { label: 'Total Completed Bookings', value: '3' },
          { label: 'Active Seat Listings', value: '14' },
          { label: 'Average Booking Amount', value: '₹7,666' }
        ],
        data: [
          { item: 'Concert A - VIP Seats', count: '1', revenue: '₹12,000' },
          { item: 'Sports Match - General', count: '1', revenue: '₹4,500' },
          { item: 'Theater Play - Balcony', count: '1', revenue: '₹6,498' }
        ]
      };
    } else if (report.reportType === 'AUDIT') {
      this.reportDetails = {
        title: report.reportName,
        type: 'Admin Audit Logs Summary',
        summary: [
          { label: 'Total Logged Actions', value: '54' },
          { label: 'Critical Database Changes', value: '12' },
          { label: 'Active Admins Active', value: '1' }
        ],
        data: [
          { action: 'TOGGLED SEAT STATUS: Row Seat A12 -> AVAILABLE', admin: 'lucky lucky', time: 'Jul 22, 2026, 12:45 PM' },
          { action: 'GENERATED REPORT: Q2 Financials', admin: 'lucky lucky', time: 'Jul 22, 2026, 11:30 AM' },
          { action: 'TOGGLED USER STATUS: BLOCKED (User: john.doe@email.com)', admin: 'lucky lucky', time: 'Jul 22, 2026, 10:15 AM' }
        ]
      };
    } else if (report.reportType === 'FRAUD') {
      this.reportDetails = {
        title: report.reportName,
        type: 'AI Security & Fraud Alerts Summary',
        summary: [
          { label: 'Total Scans Executed', value: '142' },
          { label: 'Automated Bot Detections', value: '2' },
          { label: 'Suspicious Activities Flagged', value: '8' },
          { label: 'System Health Index', value: '98.5%' }
        ],
        data: [
          { event: 'Unusual location mismatch (User: john.doe@email.com)', risk: '65%', action: 'FLAGGED FOR REVIEW', time: 'Jul 22, 2026, 2:21 PM' },
          { event: 'Multiple accounts detected (User: test@email.com)', risk: '88%', action: 'AUTO-BLOCKED', time: 'Jul 22, 2026, 2:21 PM' }
        ]
      };
    }
  }

  closeReportView() {
    this.selectedReport = null;
    this.reportDetails = null;
  }

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.loading = true;
    this.apiService.getReports().subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching reports', err);
        this.loading = false;
      }
    });
  }

  onSubmit() {
    this.error = '';
    this.success = '';

    if (!this.reportName) {
      this.error = 'Please provide a descriptive report name.';
      return;
    }

    this.apiService.generateReport(this.reportName, this.reportType).subscribe({
      next: () => {
        this.success = 'Report generated successfully!';
        this.reportName = '';
        this.loadReports();
      },
      error: (err) => {
        this.error = err.error?.message || 'Report generation failed.';
      }
    });
  }

  downloadReport(reportName: string) {
    alert(`Downloading ${reportName} in PDF format...`);
  }
}
