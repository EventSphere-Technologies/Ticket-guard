import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { SidebarService } from '../../services/sidebar.service';
import { CartService, CartItem } from '../../services/cart.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: any = null;
  notifications: any[] = [];
  showNotifDropdown = false;
  
  cartItems: CartItem[] = [];
  showCartDropdown = false;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router,
    private sidebarService: SidebarService,
    private cartService: CartService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user && user.role !== 'ADMIN') {
        this.loadNotifications();
      }
    });

    this.cartService.cartItems$.subscribe(items => {
      this.cartItems = items;
    });
  }

  loadNotifications() {
    this.apiService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
      },
      error: (err) => {
        console.error('Error loading notifications', err);
      }
    });
  }

  getUnreadCount(): number {
    return this.notifications.filter(n => !n.isRead).length;
  }

  markRead(notifId: number, event: Event) {
    event.stopPropagation();
    this.apiService.markNotificationRead(notifId).subscribe({
      next: () => {
        const found = this.notifications.find(n => n.id === notifId);
        if (found) found.isRead = true;
      }
    });
  }

  toggleSidebar() {
    this.sidebarService.toggle();
  }

  toggleNotif() {
    this.showNotifDropdown = !this.showNotifDropdown;
    this.showCartDropdown = false;
  }

  toggleCart() {
    this.showCartDropdown = !this.showCartDropdown;
    this.showNotifDropdown = false;
  }

  checkoutCartItem(item: CartItem) {
    this.showCartDropdown = false;
    this.router.navigate(['/payment'], { state: { booking: item.bookingDetails } });
  }

  removeCartItem(item: CartItem, event: Event) {
    event.stopPropagation();
    this.cartService.removeFromCart(item.bookingId);
  }

  getSeatsString(item: CartItem): string {
    if (!item || !item.selectedSeats) return '';
    return item.selectedSeats.map(s => s.rowName + s.seatNumber).join(', ');
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
