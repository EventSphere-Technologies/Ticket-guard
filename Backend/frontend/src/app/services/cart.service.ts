import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CartItem {
  bookingId: number;
  bookingDetails: any;
  eventDetails: any;
  selectedSeats: any[];
  timeRemaining: number; // in seconds
  timerDisplay: string;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  cartItems$ = this.cartItemsSubject.asObservable();
  
  private timerInterval: any = null;

  constructor() {}

  getCartItems(): CartItem[] {
    return this.cartItemsSubject.value;
  }

  addToCart(booking: any, event: any, seats: any[]) {
    // Check if booking already exists in cart to prevent duplicates
    const currentItems = this.getCartItems();
    const exists = currentItems.some(item => item.bookingId === booking.bookingId);
    if (exists) return;

    // Create new cart item with 5 minutes (300 seconds) countdown
    const newItem: CartItem = {
      bookingId: booking.bookingId,
      bookingDetails: booking,
      eventDetails: event,
      selectedSeats: seats,
      timeRemaining: 300,
      timerDisplay: '05:00'
    };

    const updatedItems = [...currentItems, newItem];
    this.cartItemsSubject.next(updatedItems);
    this.startGlobalTimer();
  }

  removeFromCart(bookingId: number) {
    const updatedItems = this.getCartItems().filter(item => item.bookingId !== bookingId);
    this.cartItemsSubject.next(updatedItems);
    if (updatedItems.length === 0) {
      this.stopGlobalTimer();
    }
  }

  clearCart() {
    this.cartItemsSubject.next([]);
    this.stopGlobalTimer();
  }

  private startGlobalTimer() {
    if (this.timerInterval) return;

    this.timerInterval = setInterval(() => {
      const items = this.getCartItems();
      let expiredAlerted = false;
      const updatedItems: CartItem[] = [];

      for (const item of items) {
        item.timeRemaining--;
        if (item.timeRemaining <= 0) {
          if (!expiredAlerted) {
            alert(`Your reservation for "${item.eventDetails.title}" (Seats: ${item.selectedSeats.map(s => s.rowName + s.seatNumber).join(', ')}) has expired.`);
            expiredAlerted = true;
          }
          // Trigger a page reload if they are on seat selection or checkout to refresh state
          if (window.location.pathname.includes('/seats/') || window.location.pathname.includes('/payment')) {
            window.location.reload();
          }
        } else {
          item.timerDisplay = this.formatTime(item.timeRemaining);
          updatedItems.push(item);
        }
      }

      this.cartItemsSubject.next(updatedItems);

      if (updatedItems.length === 0) {
        this.stopGlobalTimer();
      }
    }, 1000);
  }

  private stopGlobalTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  private formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
}
