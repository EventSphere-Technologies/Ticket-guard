import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'events/:id', component: EventDetailsComponent, canActivate: [AuthGuard] },
  { path: 'seats/:id', component: SeatSelectionComponent, canActivate: [AuthGuard] },
  { path: 'payment', component: PaymentComponent, canActivate: [AuthGuard] },
  { path: 'my-bookings', component: MyBookingsComponent, canActivate: [AuthGuard] },
  { path: 'my-profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'payment-methods', component: PaymentMethodsComponent, canActivate: [AuthGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard] },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/events',
    component: AdminEventsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/users',
    component: AdminUsersComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/bookings',
    component: AdminBookingsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/fraud',
    component: AdminFraudComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/payments',
    component: AdminPaymentsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/seats',
    component: AdminSeatsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/reports',
    component: AdminReportsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/notifications',
    component: AdminNotificationsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  {
    path: 'admin/settings',
    component: AdminSettingsComponent,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
