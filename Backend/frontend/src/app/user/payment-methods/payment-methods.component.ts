import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-payment-methods',
  templateUrl: './payment-methods.component.html',
  styleUrls: ['./payment-methods.component.css']
})
export class PaymentMethodsComponent implements OnInit {
  paymentMethods: any[] = [];
  loading = true;
  showAddForm = false;

  // New payment method fields
  paymentType = 'CARD';
  cardHolder = '';
  cardLastFour = '';
  upiId = '';

  error = '';
  success = '';

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadPaymentMethods();
  }

  loadPaymentMethods() {
    this.loading = true;
    this.apiService.getSavedPaymentMethods().subscribe({
      next: (data) => {
        this.paymentMethods = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching payment methods', err);
        this.loading = false;
      }
    });
  }

  onSubmit() {
    this.error = '';
    this.success = '';

    const payload: any = {
      paymentType: this.paymentType,
      isDefault: this.paymentMethods.length === 0
    };

    if (this.paymentType === 'CARD') {
      if (!this.cardHolder || !this.cardLastFour || this.cardLastFour.length < 4) {
        this.error = 'Please fill out card holder name and 4 digit card number suffix.';
        return;
      }
      payload.cardHolder = this.cardHolder;
      payload.cardLastFour = this.cardLastFour.slice(-4);
    } else if (this.paymentType === 'UPI') {
      if (!this.upiId) {
        this.error = 'Please enter a valid UPI ID.';
        return;
      }
      payload.upiId = this.upiId;
    }

    this.apiService.savePaymentMethod(payload).subscribe({
      next: () => {
        this.success = 'Payment method saved successfully!';
        this.showAddForm = false;
        this.resetForm();
        this.loadPaymentMethods();
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to save payment method.';
      }
    });
  }

  resetForm() {
    this.cardHolder = '';
    this.cardLastFour = '';
    this.upiId = '';
  }
}
