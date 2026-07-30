import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-payments',
  templateUrl: './admin-payments.component.html',
  styleUrls: ['./admin-payments.component.css']
})
export class AdminPaymentsComponent implements OnInit {
  payments: any[] = [];
  refunds: any[] = [];
  loading = true;
  activeTab = 'PAYMENTS'; // or 'REFUNDS'

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.apiService.getAllPaymentsAdmin().subscribe({
      next: (payData) => {
        this.payments = payData;
        this.apiService.getAllRefundsAdmin().subscribe({
          next: (refData) => {
            this.refunds = refData;
            this.loading = false;
          },
          error: (err) => {
            console.error('Error fetching refunds', err);
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error fetching payments', err);
        this.loading = false;
      }
    });
  }

  triggerRefund(bookingId: number) {
    const reason = prompt('Please enter refund reason:', 'Customer refund request');
    if (reason === null) {
      return;
    }

    this.loading = true;
    this.apiService.refundBooking(bookingId, reason).subscribe({
      next: () => {
        alert('Refund processed successfully.');
        this.loadData();
      },
      error: (err) => {
        alert(err.error?.message || 'Refund failed.');
        this.loading = false;
      }
    });
  }
}
