import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { DashboardComponent } from './user/dashboard/dashboard.component';
import { EventDetailsComponent } from './user/event-details/event-details.component';
import { SeatSelectionComponent } from './user/seat-selection/seat-selection.component';
import { PaymentComponent } from './user/payment/payment.component';
import { MyBookingsComponent } from './user/my-bookings/my-bookings.component';
import { ProfileComponent } from './user/profile/profile.component';
import { PaymentMethodsComponent } from './user/payment-methods/payment-methods.component';
import { NotificationsComponent } from './user/notifications/notifications.component';
import { AdminDashboardComponent } from './admin/dashboard/admin-dashboard.component';
import { AdminEventsComponent } from './admin/events/admin-events.component';
import { AdminUsersComponent } from './admin/users/admin-users.component';
import { AdminBookingsComponent } from './admin/bookings/admin-bookings.component';
import { AdminFraudComponent } from './admin/fraud/admin-fraud.component';
import { AdminPaymentsComponent } from './admin/payments/admin-payments.component';
import { AdminSeatsComponent } from './admin/seats/admin-seats.component';
import { AdminReportsComponent } from './admin/reports/admin-reports.component';
import { AdminNotificationsComponent } from './admin/notifications/admin-notifications.component';
import { AdminSettingsComponent } from './admin/settings/admin-settings.component';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { SidebarComponent } from './shared/sidebar/sidebar.component';
import { TokenInterceptor } from './services/token.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    DashboardComponent,
    EventDetailsComponent,
    SeatSelectionComponent,
    PaymentComponent,
    MyBookingsComponent,
    ProfileComponent,
    PaymentMethodsComponent,
    NotificationsComponent,
    AdminDashboardComponent,
    AdminEventsComponent,
    AdminUsersComponent,
    AdminBookingsComponent,
    AdminFraudComponent,
    AdminPaymentsComponent,
    AdminSeatsComponent,
    AdminReportsComponent,
    AdminNotificationsComponent,
    AdminSettingsComponent,
    NavbarComponent,
    SidebarComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
