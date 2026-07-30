import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) { }

  // Events & Venues
  getEvents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/events`);
  }

  getEventById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/events/${id}`);
  }

  getEventSeats(eventId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/events/${eventId}/seats`);
  }

  createEvent(event: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/events`, event);
  }

  createVenue(venue: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/events/venues`, venue);
  }

  getAllEventsAdmin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/events/all`);
  }

  toggleEventStatus(eventId: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/events/${eventId}/status`, {});
  }

  reseedDatabase(): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/reseed`, {}, { responseType: 'text' as 'json' });
  }

  // Bookings
  reserveSeats(eventId: number, seatIds: number[], behaviour?: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/bookings/reserve`, { eventId, seatIds, behaviour });
  }

  getMyBookings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/bookings/my-bookings`);
  }

  // Payments & Refunds
  chargePayment(payload: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/payments/charge`, payload);
  }

  refundBooking(bookingId: number, reason: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/payments/refund/${bookingId}?reason=${encodeURIComponent(reason)}`, {});
  }

  // User Profile, Notifications & Cards
  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users/profile`);
  }

  getNotifications(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/users/notifications`);
  }

  markNotificationRead(notificationId: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/users/notifications/${notificationId}/read`, {});
  }

  getSavedPaymentMethods(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/users/payment-methods`);
  }

  savePaymentMethod(method: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/payment-methods`, method);
  }

  // Admin KPIs & Fraud Metrics
  getAdminDashboard(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/admin/dashboard`);
  }

  getFraudAlerts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/fraud-alerts`);
  }

  getAuditLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/audit-logs`);
  }

  getReports(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/reports`);
  }

  generateReport(reportName: string, reportType: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/reports?reportName=${encodeURIComponent(reportName)}&reportType=${encodeURIComponent(reportType)}`, {});
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/users`);
  }

  toggleUserStatus(userId: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/admin/users/${userId}/status`, {});
  }

  getAllBookingsAdmin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/bookings`);
  }

  getAllPaymentsAdmin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/payments`);
  }

  getAllRefundsAdmin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/refunds`);
  }

  getAllSeatsAdmin(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/seats`);
  }

  toggleSeatStatusAdmin(seatId: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/admin/seats/${seatId}/status`, {});
  }
}
