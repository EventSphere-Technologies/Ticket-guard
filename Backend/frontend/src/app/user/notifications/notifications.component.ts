import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
  notifications: any[] = [];
  loading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications() {
    this.loading = true;
    this.apiService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching notifications', err);
        this.loading = false;
      }
    });
  }

  markAsRead(notificationId: number) {
    this.apiService.markNotificationRead(notificationId).subscribe({
      next: () => {
        // Optimistically update the UI status
        const notification = this.notifications.find(n => n.id === notificationId);
        if (notification) {
          notification.isRead = true;
        }
      },
      error: (err) => {
        console.error('Error marking notification as read', err);
      }
    });
  }
}
