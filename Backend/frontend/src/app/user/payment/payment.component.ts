import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit {
  booking: any = null;
  paymentMethod = 'CARD';
  
  // Card Details
  cardHolder = '';
  cardNumber = '';
  cardExpiry = '';
  cardCvv = '';
  
  // UPI Details
  upiId = '';

  loading = false;
  success = false;
  error = '';
  paymentDetails: any = null;

  constructor(
    private router: Router,
    private apiService: ApiService,
    private cartService: CartService
  ) {
    const navigation = this.router.getCurrentNavigation();
    if (navigation && navigation.extras && navigation.extras.state) {
      this.booking = navigation.extras.state['booking'];
    }
  }

  ngOnInit() {
    if (!this.booking) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit() {
    this.loading = true;
    this.error = '';

    const payload: any = {
      bookingId: this.booking.bookingId,
      paymentMethod: this.paymentMethod
    };

    if (this.paymentMethod === 'CARD') {
      if (!this.cardHolder || !this.cardNumber) {
        this.error = 'Please fill out card holder name and number.';
        this.loading = false;
        return;
      }
      payload.cardHolder = this.cardHolder;
      payload.cardLastFour = this.cardNumber.slice(-4);
    } else if (this.paymentMethod === 'UPI') {
      if (!this.upiId) {
        this.error = 'Please enter your UPI ID.';
        this.loading = false;
        return;
      }
      payload.upiId = this.upiId;
    }

    this.apiService.chargePayment(payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.success = true;
        this.paymentDetails = res;
        this.cartService.removeFromCart(this.booking.bookingId);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Payment processing failed. Please try again.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
