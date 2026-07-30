import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-seat-selection',
  templateUrl: './seat-selection.component.html',
  styleUrls: ['./seat-selection.component.css']
})
export class SeatSelectionComponent implements OnInit, OnDestroy {
  eventId!: number;
  event: any = null;
  seats: any[] = [];
  rowsMap: { [key: string]: any[] } = {};
  selectedSeats: any[] = [];
  totalPrice = 0;
  
  loading = true;
  reserving = false;
  error = '';

  // Timer fields
  timerSeconds = 300;
  timerDisplay = '05:00';
  timerInterval: any = null;
  seatsLocked = false;
  bookingDetails: any = null;

  // Telemetry fields
  pageLoadTime = 0;
  mouseMoveCount = 0;
  keystrokeCount = 0;
  failedAttemptsCount = 0;

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    this.mouseMoveCount++;
  }

  @HostListener('document:keypress', ['$event'])
  onKeyPress(event: KeyboardEvent) {
    this.keystrokeCount++;
  }

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private router: Router,
    private cartService: CartService
  ) {}

  ngOnInit() {
    this.pageLoadTime = Date.now();
    const pagesVisited = parseInt(sessionStorage.getItem('pages_visited') || '0', 10);
    sessionStorage.setItem('pages_visited', (pagesVisited + 1).toString());

    this.eventId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEventAndSeats();
  }

  ngOnDestroy() {
    this.clearTimer();
  }

  loadEventAndSeats() {
    this.apiService.getEventById(this.eventId).subscribe(eventData => {
      this.event = eventData;
      this.apiService.getEventSeats(this.eventId).subscribe({
        next: (seatData) => {
          this.seats = seatData;
          this.groupSeatsByRow();
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
        }
      });
    });
  }

  groupSeatsByRow() {
    this.rowsMap = {};
    for (const seat of this.seats) {
      if (!this.rowsMap[seat.rowName]) {
        this.rowsMap[seat.rowName] = [];
      }
      this.rowsMap[seat.rowName].push(seat);
    }
    for (const rowName in this.rowsMap) {
      this.rowsMap[rowName].sort((a, b) => a.seatNumber - b.seatNumber);
    }
  }

  getRowKeys() {
    return Object.keys(this.rowsMap).sort();
  }

  toggleSeatSelection(seat: any) {
    if (seat.status !== 'AVAILABLE' || this.seatsLocked) {
      return;
    }

    const index = this.selectedSeats.findIndex(s => s.id === seat.id);
    if (index > -1) {
      this.selectedSeats.splice(index, 1);
      this.totalPrice -= seat.price;
    } else {
      this.selectedSeats.push(seat);
      this.totalPrice += seat.price;
    }
  }

  isSelected(seat: any): boolean {
    return this.selectedSeats.some(s => s.id === seat.id);
  }

  lockSeats() {
    if (this.selectedSeats.length === 0) {
      this.error = 'Please select at least one seat to book.';
      return;
    }

    this.reserving = true;
    this.error = '';

    const timeSpent = (Date.now() - this.pageLoadTime) / 1000;
    const pagesVisited = parseInt(sessionStorage.getItem('pages_visited') || '1', 10);
    const mouseEntropy = Math.min(0.95, Math.max(0.05, 0.1 + (this.mouseMoveCount / 300.0) + (Math.random() * 0.1)));
    const keystrokeVelocity = timeSpent > 0 ? (this.keystrokeCount / timeSpent) * 60.0 : 0.0;
    const isHeadless = !!(navigator.webdriver || !navigator.languages || navigator.languages.length === 0);

    let fingerprint = localStorage.getItem('device_fingerprint');
    if (!fingerprint) {
      fingerprint = 'dfp_' + Math.random().toString(36).substring(2) + Date.now().toString(36);
      localStorage.setItem('device_fingerprint', fingerprint);
    }

    const telemetry = {
      timeSpentSeconds: parseFloat(timeSpent.toFixed(2)),
      mouseMovementEntropy: parseFloat(mouseEntropy.toFixed(3)),
      keystrokeVelocity: parseFloat(keystrokeVelocity.toFixed(2)),
      pagesVisited: pagesVisited,
      failedAttempts: this.failedAttemptsCount,
      isHeadlessBrowser: isHeadless,
      deviceFingerprint: fingerprint
    };

    const seatIds = this.selectedSeats.map(s => s.id);
    this.apiService.reserveSeats(this.eventId, seatIds, telemetry).subscribe({
      next: (res) => {
        this.reserving = false;
        this.bookingDetails = res;
        this.seatsLocked = true;
        this.startTimer();
        this.cartService.addToCart(res, this.event, this.selectedSeats);
      },
      error: (err) => {
        this.reserving = false;
        this.failedAttemptsCount++;
        this.error = err.error?.message || 'Failed to lock seats. Some seats might be locked by another user.';
      }
    });
  }

  startTimer() {
    this.timerSeconds = 300;
    this.updateTimerDisplay();
    this.timerInterval = setInterval(() => {
      this.timerSeconds--;
      this.updateTimerDisplay();
      if (this.timerSeconds <= 0) {
        this.clearTimer();
        this.handleTimerExpiry();
      }
    }, 1000);
  }

  updateTimerDisplay() {
    const mins = Math.floor(this.timerSeconds / 60);
    const secs = this.timerSeconds % 60;
    this.timerDisplay = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  clearTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  handleTimerExpiry() {
    this.seatsLocked = false;
    const expiredId = this.bookingDetails?.bookingId;
    this.selectedSeats = [];
    this.totalPrice = 0;
    this.bookingDetails = null;
    if (expiredId) {
      this.cartService.removeFromCart(expiredId);
    }
    alert('Your 5-minute seat reservation has expired. The seats have been released.');
    this.loadEventAndSeats();
  }

  proceedToPayment() {
    this.clearTimer();
    this.router.navigate(['/payment'], { state: { booking: this.bookingDetails } });
  }
}
