import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent implements OnInit {
  users: any[] = [];
  loading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.apiService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching users list', err);
        this.loading = false;
      }
    });
  }

  toggleBlockStatus(user: any) {
    const actionStr = user.status === 'BLOCKED' ? 'unblock' : 'block';
    const confirmToggle = confirm(`Are you sure you want to ${actionStr} this user?`);
    if (!confirmToggle) {
      return;
    }

    this.apiService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser) => {
        user.status = updatedUser.status;
        alert(`User successfully ${actionStr}ed.`);
      },
      error: (err) => {
        alert(err.error?.message || `Failed to ${actionStr} user.`);
      }
    });
  }
}
